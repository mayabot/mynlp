/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mayabot.nlp.module.pinyin;

import com.mayabot.nlp.algorithm.collection.ahocorasick.AhoCoraickDoubleArrayTrieBuilder;
import com.mayabot.nlp.algorithm.collection.ahocorasick.AhoCorasickDoubleArrayTrie;
import com.mayabot.nlp.common.logging.InternalLogger;
import com.mayabot.nlp.common.logging.InternalLoggerFactory;
import com.mayabot.nlp.module.pinyin.model.Pinyin;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 拼音的词典
 *
 * @author jimichan
 */
public class Tex2PinyinComputer {

    InternalLogger logger = InternalLoggerFactory.getInstance(Tex2PinyinComputer.class);

    private AhoCorasickDoubleArrayTrie<Pinyin[]> trie = null;

    @Nullable
    private Map<String, Pinyin[]> customPinyin;

    PinyinInnerDict systemDict;

    public Tex2PinyinComputer(PinyinInnerDict systemDict, @Nullable Map<String, Pinyin[]> customPinyin) {
        this.systemDict = systemDict;
        this.customPinyin = customPinyin;
        rebuild();
    }

    public Tex2PinyinComputer(PinyinInnerDict systemDict) {
        this.systemDict = systemDict;
        rebuild();
    }

    public void setUpCustomPinyin(Map<String, String> map) {
        this.customPinyin = convert(map);
        rebuild();
    }

    public void setCustomPinyin(
            @Nullable Map<String, Pinyin[]> customPinyin) {
        this.customPinyin = customPinyin;
        rebuild();
    }

    private void rebuild() {

        long t1 = System.currentTimeMillis();

        final TreeMap<String, Pinyin[]> map = new TreeMap<>();
        map.putAll(systemDict.getPinyinTable());
        if (customPinyin != null) {
            map.putAll(customPinyin);
        }

        AhoCoraickDoubleArrayTrieBuilder<Pinyin[]> builder = new AhoCoraickDoubleArrayTrieBuilder<>();
        this.trie = builder.build(map);

        long t2 = System.currentTimeMillis();

        logger.info("PinyinModule Dictionary rebuild use time {} ms", t2 - t1);
    }

    /**
     * 转化为拼音
     *
     * @param text 文本
     */
    public PinyinResult text2Pinyin(String text) {
        return new PinyinResult(segLongest(text.toCharArray()), text);
    }

    /**
     * @param text 原始中文文本
     * @return PinyinResult
     */
    public PinyinResult convert(String text) {
        return this.text2Pinyin(text);
    }

    /**
     * 来自Hanlp的拼音方法
     *
     * @param charArray
     * @return List<PinyinModule>
     */
    private List<Pinyin> segLongest(char[] charArray) {
        final Pinyin[][] wordNet = new Pinyin[charArray.length][];

        trie.parseText(charArray, (begin, end, value) -> {
            int length = end - begin;
            if (wordNet[begin] == null || length > wordNet[begin].length) {
                wordNet[begin] = length == 1 ? new Pinyin[]{value[0]} : value;
            }
        });
        List<Pinyin> pinyinList = new ArrayList<>(charArray.length);
        for (int offset = 0; offset < wordNet.length; ) {
            if (wordNet[offset] == null) {
                pinyinList.add(Pinyin.none5);
                ++offset;
                continue;
            }
            Collections.addAll(pinyinList, wordNet[offset]);

            offset += wordNet[offset].length;
        }
        return pinyinList;
    }

    static Pinyin[] pinyinByOrdinal;

    static {
        pinyinByOrdinal = new Pinyin[Pinyin.values().length + 1];

        Pinyin[] values = Pinyin.values();
        for (int i = values.length - 1; i >= 0; i--) {
            pinyinByOrdinal[values[i].ordinal()] = values[i];
        }
    }

    // yi1,ge4

    public static Pinyin[] parse(String text) {
        String[] values = text.split(",");

        Pinyin[] pinyins = new Pinyin[values.length];
        for (int i = 0; i < values.length; i++) {
            try {
                Pinyin pinyin = Pinyin.valueOf(values[i]);
                pinyins[i] = pinyin;

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("读取拼音词典，解析" + text + "错误");
            }
        }
        return pinyins;
    }

    public static Map<String, Pinyin[]> convert(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        HashMap<String, Pinyin[]> cs = new HashMap<>();
        for (Map.Entry<String, String> e : map.entrySet()) {
            cs.put(e.getKey(), parse(e.getValue()));
        }
        return cs;
    }

}

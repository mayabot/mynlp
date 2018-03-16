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

package com.mayabot.nlp.pinyin;

import com.google.inject.Inject;
import com.mayabot.nlp.collection.ahocorasick.AhoCorasickDoubleArrayTrie;
import com.mayabot.nlp.pinyin.model.Pinyin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */

public class Text2PinyinService {

    private final AhoCorasickDoubleArrayTrie<Pinyin[]> trie;

    @Inject
    public Text2PinyinService(PinyinDictionary pinyinDictionary) {
        trie = pinyinDictionary.getTrie();
    }

    /**
     * 转化为拼音
     * @param text 文本
     */
    public  PinyinResult text2Pinyin(String text) {
        return new PinyinResult(segLongest(text.toCharArray()), text);
    }



    /**
     * 来自Hanlp的拼音方法
     * @param charArray
     * @return
     */
    protected List<Pinyin> segLongest(char[] charArray) {
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


}

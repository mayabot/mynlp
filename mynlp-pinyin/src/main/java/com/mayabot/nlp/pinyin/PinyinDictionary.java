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

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.Setting;
import com.mayabot.nlp.caching.MynlpCacheable;
import com.mayabot.nlp.collection.ahocorasick.AhoCoraickDoubleArrayTrieBuilder;
import com.mayabot.nlp.collection.ahocorasick.AhoCorasickDoubleArrayTrie;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.pinyin.model.Pinyin;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.*;
import java.util.List;
import java.util.TreeMap;

import static com.mayabot.nlp.Setting.string;

/**
 * 拼音的词典
 *
 * @author jimichan
 */
@Singleton
public class PinyinDictionary implements MynlpCacheable {

    InternalLogger logger = InternalLoggerFactory.getInstance(PinyinDictionary.class);

    public final static Setting<String> pinyinSetting =
            string("pinyin.dict", "dictionary/pinyin.txt");

    public final static Setting<String> pinyinExtDicSetting =
            string("pinyin.ext.dict", null);

    private Mynlp mynlp;

    private AhoCorasickDoubleArrayTrie<Pinyin[]> trie = null;

    private CustomPinyin customPinyin;

    @Inject
    public PinyinDictionary(Mynlp mynlp, CustomPinyin customPinyin) throws Exception {
        this.mynlp = mynlp;
        long t1 = System.currentTimeMillis();

        this.customPinyin = customPinyin;

        this.restore();
        long t2 = System.currentTimeMillis();
        logger.info("Loaded pinyin dictionary success! " + (t2 - t1) + " ms");
    }

    @Override
    public File cacheFileName() {

        String hash = mynlp.loadResource(pinyinSetting).hash();

        NlpResource ext = mynlp.loadResource(pinyinExtDicSetting);
        if (ext != null) {
            hash += ext.hash();
        }

        hash += customPinyin.hash();

        hash = Hashing.md5().hashString(hash, Charsets.UTF_8).toString();

        return new File(mynlp.getCacheDir(), "pinyin.dict." + hash);
    }

    @Override
    public void saveToCache(OutputStream out) throws Exception {
        DataOutputStream dataOutput = new DataOutputStream(out);

        AhoCorasickDoubleArrayTrie.write(trie, dataOutput, PinyinDictionary::write);

        dataOutput.flush();
    }


    public static void write(Pinyin[] pinyins, DataOutput out) {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pinyins.length; i++) {
                sb.append(pinyins[i].ordinal()).append(",");
            }
            out.writeUTF(sb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Pinyin[] pinyinByOrdinal;

    static {
        pinyinByOrdinal = new Pinyin[Pinyin.values().length+1];

        Pinyin[] values = Pinyin.values();
        for (int i = values.length - 1; i >= 0; i--) {
            pinyinByOrdinal[values[i].ordinal()] = values[i];
        }
    }

    public static Pinyin[] read(DataInput in) {
        try {
            String line = in.readUTF();
            String[] split = line.split(",");

            Pinyin[] pinyins = new Pinyin[split.length];

            for (int i = 0; i < split.length; i++) {
                Integer xx = Ints.tryParse(split[i]);
                Pinyin pinyin = pinyinByOrdinal[xx];
                pinyins[i] = pinyin;
            }

            return pinyins;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void readFromCache(File file) throws Exception {
        try(InputStream inputStream = new BufferedInputStream(Files.asByteSource(file).openStream(), 64 * 1024)) {
            DataInput dataInput = new DataInputStream(inputStream);
            this.trie = AhoCorasickDoubleArrayTrie.read(dataInput, PinyinDictionary::read);
        }
    }

    @Override
    public void loadFromRealData() throws Exception {

        List<NlpResource> list = Lists.newArrayList();

        list.add(mynlp.loadResource(pinyinSetting));

        NlpResource ext = mynlp.loadResource(pinyinExtDicSetting);
        if (ext != null) {
            list.add(ext);
        }

        TreeMap<String, Pinyin[]> map = new TreeMap<>();
        for (NlpResource dictResource : list) {

            try (CharSourceLineReader reader = dictResource.openLineReader()) {
                while (reader.hasNext()) {
                    //降龙伏虎=xiang2,long2,fu2,hu3
                    //单=dan1,shan4,chan2
                    String line = reader.next();
                    String[] param = line.split("=");

                    String key = param[0];

                    Pinyin[] pinyins = parse(param[1]);
                    if (pinyins != null) {
                        map.put(key, pinyins);
                    }
                }
            }
        }

        customPinyin.getMap().forEach((key, value) -> {
            Pinyin[] pinyins = parse(value);
            if (pinyins != null) {
                map.put(key, pinyins);
            }
        });

        AhoCoraickDoubleArrayTrieBuilder<Pinyin[]> builder = new AhoCoraickDoubleArrayTrieBuilder<>();
        this.trie = builder.build(map);

    }

    private Pinyin[] parse(String text) {
        String[] values = text.split(",");


        Pinyin[] pinyins = new Pinyin[values.length];
        boolean error = false;
        for (int i = 0; i < values.length; i++) {
            try {
                Pinyin pinyin = Pinyin.valueOf(values[i]);
                pinyins[i] = pinyin;

            } catch (IllegalArgumentException e) {
                logger.warn("读取拼音词典，解析" + text + "错误");
                error = true;
            }
        }
        if (!error) {
            return pinyins;
        } else {
            return null;
        }
    }


    public AhoCorasickDoubleArrayTrie<Pinyin[]> getTrie() {
        return trie;
    }
}

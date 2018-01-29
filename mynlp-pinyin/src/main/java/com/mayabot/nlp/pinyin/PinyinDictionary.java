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

import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.Environment;
import com.mayabot.nlp.Setting;
import com.mayabot.nlp.caching.MynlpCacheable;
import com.mayabot.nlp.collection.ahocorasick.AhoCoraickDoubleArrayTrieBuilder;
import com.mayabot.nlp.collection.ahocorasick.AhoCorasickDoubleArrayTrie;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.pinyin.model.Pinyin;
import com.mayabot.nlp.resources.MynlpResource;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.*;
import java.util.TreeMap;

@Singleton
public class PinyinDictionary implements MynlpCacheable {

    InternalLogger logger = InternalLoggerFactory.getInstance(PinyinDictionary.class);

    public final Setting<String> pinyinSetting =
            Setting.stringSetting("pinyin.dict", "inner://dictionary/pinyin.txt");

    private final Environment environment;


    private AhoCorasickDoubleArrayTrie<Pinyin[]> trie = null;

    @Inject
    public PinyinDictionary(Environment environment) throws Exception {
        this.environment = environment;
        long t1 = System.currentTimeMillis();
        this.restore();
        long t2 = System.currentTimeMillis();
        logger.info("Loaded pinyin dictionary success! " + (t2 - t1) + " ms");
    }

    @Override
    public File cacheFileName() {
        String hash = environment.loadResource(pinyinSetting).hash();
        return new File(environment.getWorkDir(), "pinyin.dict." + hash);
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
    public void readFromCache(InputStream inputStream) throws Exception {
        DataInput dataInput = new DataInputStream(inputStream);
        this.trie = AhoCorasickDoubleArrayTrie.read(dataInput, PinyinDictionary::read);
    }

    @Override
    public void loadFromRealData() throws Exception {
        MynlpResource dictResource = environment.loadResource(pinyinSetting);

        TreeMap<String, Pinyin[]> map = new TreeMap<>();
        try (CharSourceLineReader reader = dictResource.openLineReader()) {
            while (reader.hasNext()) {
                //降龙伏虎=xiang2,long2,fu2,hu3
                //单=dan1,shan4,chan2
                String line = reader.next();
                String[] param = line.split("=");

                String key = param[0];
                String[] values = param[1].split(",");


                Pinyin[] pinyins = new Pinyin[values.length];

                for (int i = 0; i < values.length; i++) {
                    try {
                        Pinyin pinyin = Pinyin.valueOf(values[i]);
                        pinyins[i] = pinyin;
                        map.put(key, pinyins);
                    } catch (IllegalArgumentException e) {
                        logger.warn("读取拼音词典，解析" + line + "错误");
                    }
                }
            }
        }

        AhoCoraickDoubleArrayTrieBuilder<Pinyin[]> builder = new AhoCoraickDoubleArrayTrieBuilder<>();
        this.trie = builder.build(map);

    }


    public AhoCorasickDoubleArrayTrie<Pinyin[]> getTrie() {
        return trie;
    }
}

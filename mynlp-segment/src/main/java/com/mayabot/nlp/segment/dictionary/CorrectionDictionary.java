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

package com.mayabot.nlp.segment.dictionary;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.Setting;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.common.FastJson;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;


/**
 * 人工纠错词典
 * <p>
 * <p>
 * 第几套房 => 第几套/v 房
 * 词可以后面跟随 词性
 * <p>
 * 以后要做出支持多个词典的加载，然后每个项目都可以有不一样的设定
 *
 * @author jimichan
 */
@Singleton
public class CorrectionDictionary {

    static InternalLogger logger = InternalLoggerFactory.getInstance(CorrectionDictionary.class);

    private final MynlpEnv mynlp;

    private DoubleArrayTrie<AdjustWord> doubleArrayTrie;


    public static Setting<String> correctionDict = Setting.string("correction.dict", "dictionary/correction/adjust.txt");


    @Inject
    public CorrectionDictionary(MynlpEnv mynlp) throws Exception {

        this.mynlp = mynlp;

        List<String> resourceUrls = mynlp.getSettings().getAsList(correctionDict);

        if (resourceUrls.isEmpty()) {
            return;
        }

        loadFromRealData(resourceUrls);
    }

    public void loadFromRealData(List<String> resourceUrls) throws Exception {
        TreeMap<String, AdjustWord> map = new TreeMap<>();

        for (String url : resourceUrls) {
            NlpResource resource = mynlp.loadResource(url);

            try (CharSourceLineReader reader = resource.openLineReader()) {
                while (reader.hasNext()) {
                    String line = reader.next();
                    AdjustWord adjustWord = AdjustWord.parse(line
                    );
                    map.put(adjustWord.path, adjustWord);
                }
            }
        }

        if (map.isEmpty()) {
            return;
        }

        this.doubleArrayTrie = new DoubleArrayTrieBuilder<AdjustWord>().build(map);
    }

    public DoubleArrayTrie<AdjustWord> getDoubleArrayTrie() {
        return doubleArrayTrie;
    }

    public void setDoubleArrayTrie(DoubleArrayTrie<AdjustWord> doubleArrayTrie) {
        this.doubleArrayTrie = doubleArrayTrie;
    }

    public static class AdjustWord {
        String path;
        String raw;
        List<Integer> words = Lists.newArrayListWithExpectedSize(4);

        public static void write(AdjustWord a, DataOutput out) {
            try {
                out.writeUTF(a.path);
                out.writeUTF(a.raw);
                out.writeUTF(FastJson.toJson(a.words));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static AdjustWord read(DataInput in) {
            try {
                AdjustWord a = new AdjustWord();

                a.path = in.readUTF();
                a.raw = in.readUTF();
                a.words = FastJson.fromJsonListInteger(in.readUTF());

                return a;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        static Splitter splitter = Splitter.on("/").trimResults().omitEmptyStrings();

        public List<Integer> getWords() {
            return words;
        }

        public String getPath() {
            return path;
        }

        public String getRaw() {
            return raw;
        }

        public static AdjustWord parse(String line) {
            AdjustWord adjustWord = new AdjustWord();
            adjustWord.raw = line.trim();

            List<String> list = splitter.splitToList(adjustWord.raw);
            adjustWord.path = Joiner.on("").join(list);
            for (String s : list) {
                adjustWord.words.add(s.length());
            }


            return adjustWord;
        }

        @Override
        public String toString() {
            String sb = "AdjustWord{" + "path='" + path + '\'' +
                    ", raw='" + raw + '\'' +
                    ", words=" + words +
                    '}';
            return sb;
        }

    }

}

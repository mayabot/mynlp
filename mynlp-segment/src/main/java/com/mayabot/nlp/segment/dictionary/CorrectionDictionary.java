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
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.Setting;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.util.List;
import java.util.TreeMap;


/**
 * 人工纠错词典
 *
 * <p>
 * 格式 第几套/房
 *
 *
 * <p>
 *
 * @author jimichan
 */
@Singleton
public class CorrectionDictionary {

    static InternalLogger logger = InternalLoggerFactory.getInstance(CorrectionDictionary.class);
    public final static Setting<String> correctionDict = Setting.string("correction.dict", "dictionary/correction/adjust.txt");


    private DoubleArrayTrie<AdjustWord> doubleArrayTrie;


    @Inject
    public CorrectionDictionary(MynlpEnv mynlp) throws Exception {

        List<String> resourceUrls = mynlp.getSettings().getAsList(correctionDict);

        if (resourceUrls.isEmpty()) {
            return;
        }

        loadFromRealData(mynlp, resourceUrls);
    }

    public CorrectionDictionary(TreeMap<String, AdjustWord> data) {
        DoubleArrayTrie dat = new DoubleArrayTrieBuilder().build(data);
        doubleArrayTrie = dat;
    }


    public void loadFromRealData(MynlpEnv mynlp, List<String> resourceUrls) throws Exception {
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
        int[] words;

        static Splitter splitter = Splitter.on("/").trimResults().omitEmptyStrings();

        public int[] getWords() {
            return words;
        }

        public String getPath() {
            return path;
        }

        public String getRaw() {
            return raw;
        }

        /**
         * 第几套/房
         *
         * @param line
         * @return
         */
        public static AdjustWord parse(String line) {
            AdjustWord adjustWord = new AdjustWord();
            adjustWord.raw = line.trim();

            List<String> list = splitter.splitToList(adjustWord.raw);
            adjustWord.path = Joiner.on("").join(list);
            List<Integer> words = Lists.newArrayList();
            for (String s : list) {
                words.add(s.length());
            }

            adjustWord.words = Ints.toArray(words);
            return adjustWord;
        }

        @Override
        public String toString() {
            return "AdjustWord{" + "path='" + path + '\'' +
                    ", raw='" + raw + '\'' +
                    ", words=" + words +
                    '}';
        }

    }

}

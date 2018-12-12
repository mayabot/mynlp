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
import com.google.inject.ImplementedBy;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieMap;
import com.mayabot.nlp.segment.dictionary.correction.DefaultCorrectionDictionary;

import java.util.List;

/**
 * 分词纠错词典结构.
 * 对外提供一个DoubleArrayTrie
 *
 * @author jimichan
 */
@ImplementedBy(DefaultCorrectionDictionary.class)
public interface CorrectionDictionary {

    DoubleArrayTrieMap<AdjustWord> getTrie();

    class AdjustWord {
        public String path;
        public String raw;
        public int[] words;

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

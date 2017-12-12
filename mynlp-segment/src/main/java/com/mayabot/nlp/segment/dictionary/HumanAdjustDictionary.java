/*
 *  Copyright 2017 mayabot.com authors. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mayabot.nlp.segment.dictionary;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.ResourceLoader;
import com.mayabot.nlp.Settings;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

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
 */
@Singleton
public class HumanAdjustDictionary {

    InternalLogger logger = InternalLoggerFactory.getInstance(HumanAdjustDictionary.class);

    private final Settings settings;

    public static final String humanAdjustPathKey = "hunman_adjust_dict";

    private DoubleArrayTrie<AdjustWord> doubleArrayTrie;

    private final ResourceLoader resourceLoader;


    @Inject
    public HumanAdjustDictionary(Settings settings, ResourceLoader resourceLoader) {
        this.settings = settings;
        this.resourceLoader = resourceLoader;

        //加载默认的词库
        doubleArrayTrie = load(settings.get(humanAdjustPathKey, "humanadjust"));
    }


    public DoubleArrayTrie<AdjustWord> getDoubleArrayTrie() {
        return doubleArrayTrie;
    }

    public void setDoubleArrayTrie(DoubleArrayTrie<AdjustWord> doubleArrayTrie) {
        this.doubleArrayTrie = doubleArrayTrie;
    }


    public DoubleArrayTrie<AdjustWord> load(String path)

    {
        TreeMap<String, AdjustWord> wordMap = new TreeMap<>();
        ByteSource file = resourceLoader.loadDictionary(path);

        // 这里忽略了对默认词性的解析

        boolean success = load(file, wordMap);

        if (success == false || wordMap.isEmpty()) {
            return null;
        }


        return new DoubleArrayTrieBuilder<AdjustWord>().build(wordMap);
    }

    /**
     * 外部程序可以通过这个方法，直接设置纠错词库信息。
     *
     * @param wordMap
     */
    public void load(TreeMap<String, AdjustWord> wordMap) {
        doubleArrayTrie = new DoubleArrayTrieBuilder<AdjustWord>().build(wordMap);
    }

    private boolean load(ByteSource path, TreeMap<String, AdjustWord> map) {
        try {

            logger.info("xxx {} ",path.asCharSource(Charsets.UTF_8).read());

            path.asCharSource(Charsets.UTF_8)
                    .readLines().stream()
                    .forEach(line -> {
                        AdjustWord adjustWord = AdjustWord.parse(line
                        );
                        map.put(adjustWord.path, adjustWord);
                    });

            return true;
        } catch (Exception e) {
            logger.warn("read adjust {} dictionary occurs error", path);
        }

        return false;
    }

    public static class AdjustWord {
        String path;
        String raw;
        List<Integer> words = Lists.newArrayListWithExpectedSize(4);
        //List<Nature> nature = Lists.newArrayListWithExpectedSize(4);

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

        // line = 第几套/房
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
            final StringBuilder sb = new StringBuilder("AdjustWord{");
            sb.append("path='").append(path).append('\'');
            sb.append(", raw='").append(raw).append('\'');
            sb.append(", words=").append(words);
            sb.append('}');
            return sb.toString();
        }

    }

    public static void main(String[] args) {
        System.out.println(AdjustWord.parse("第几套/房"));
    }
}

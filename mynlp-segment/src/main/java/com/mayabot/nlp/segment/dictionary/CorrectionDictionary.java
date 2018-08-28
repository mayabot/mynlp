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

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpIOC;
import com.mayabot.nlp.caching.MynlpCacheable;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.*;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;


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
public class CorrectionDictionary implements MynlpCacheable {

    static InternalLogger logger = InternalLoggerFactory.getInstance(CorrectionDictionary.class);
    private final MynlpIOC mynlp;

    private DoubleArrayTrie<AdjustWord> doubleArrayTrie;

    private List<String> resourceUrls;

//    public final Setting<String> humanAdjustKey =
//            Setting.string("correction.dict", "dictionary/core/CoreNatureDictionary.txt");


    @Inject
    public CorrectionDictionary(MynlpIOC mynlp) throws Exception {

        this.mynlp = mynlp;

        List<String> resourceUrls = mynlp.getSettings().getAsList(
                "correction.dict", "dictionary/correction/adjust.txt");

        if (resourceUrls.isEmpty()) {
            return;
        }

        this.resourceUrls = resourceUrls;

        restore();

    }

    @Override
    public File cacheFileName() {
        if (resourceUrls.isEmpty()) {
            return null;
        }
        TreeSet<String> set = new TreeSet<>();

        for (String url : resourceUrls) {
            NlpResource resource = mynlp.loadResource(url);

            set.add(resource.hash());
        }

        String hash = Hashing.md5().hashString(set.toString(), Charsets.UTF_8).toString();

        return new File(mynlp.getCacheDir(), hash + ".correction.dict");
    }

    @Override
    public void saveToCache(OutputStream out) throws Exception {
        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();

        DoubleArrayTrie.write(doubleArrayTrie, dataOutput, AdjustWord::write);

        out.write(dataOutput.toByteArray());
    }

    @Override
    public void readFromCache(File file) throws Exception {
        try (InputStream inputStream = new BufferedInputStream(Files.asByteSource(file).openStream(), 64 * 1024)) {
            DataInput dataInput = new DataInputStream(inputStream);
            this.doubleArrayTrie = DoubleArrayTrie.read(dataInput, AdjustWord::read);
        }

    }

    @Override
    public void loadFromRealData() throws Exception {
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

    public static void main(String[] args) {
        JSON.toJSONString("[3,1]");
    }

    public static class AdjustWord {
        String path;
        String raw;
        List<Integer> words = Lists.newArrayListWithExpectedSize(4);

        public static void write(AdjustWord a, DataOutput out) {
            try {
                out.writeUTF(a.path);
                out.writeUTF(a.raw);
                out.writeUTF(JSON.toJSONString(a.words));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static AdjustWord read(DataInput in) {
            try {
                AdjustWord a = new AdjustWord();

                a.path = in.readUTF();
                a.raw = in.readUTF();
                a.words = JSON.parseArray(in.readUTF(), Integer.class);

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

//    public static void main(String[] args) {
//        System.out.println(AdjustWord.parse("第几套/房"));
//    }
}

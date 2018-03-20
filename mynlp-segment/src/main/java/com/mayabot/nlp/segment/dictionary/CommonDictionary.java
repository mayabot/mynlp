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

import com.google.common.base.Splitter;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.mayabot.nlp.Environment;
import com.mayabot.nlp.caching.MynlpCacheable;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.MynlpResource;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.*;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * 通用的词典，对应固定格式的词典，但是标签可以泛型化
 *
 * @author hankcs
 * @author jimichan
 */
public abstract class CommonDictionary<V> implements MynlpCacheable {

    protected InternalLogger logger = InternalLoggerFactory.getInstance(this.getClass());


    private final Environment environment;

    private DoubleArrayTrie<V> trie;

    public CommonDictionary(Environment environment) throws Exception {
        this.environment = environment;

        this.restore();
    }

    @Override
    public File cacheFileName() {
        String hash = environment.getMynlpResourceFactory().load(dicFilePath()).hash();

        return new File(environment.getWorkDir(), hash + "." + this.getClass().getSimpleName());
    }

    protected abstract String dicFilePath();

    protected abstract V parseLine(List<String> pars);

    protected abstract void writeItem(V a, DataOutput out);

    protected abstract V readItem(DataInput in);

    protected void filtermap(TreeMap<String, V> map) {

    }

    @Override
    public void saveToCache(OutputStream out) throws Exception {
        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();

        DoubleArrayTrie.write(trie, dataOutput, this::writeItem);

        out.write(dataOutput.toByteArray());
    }

    @Override
    public void readFromCache(File file) throws Exception {
        try(InputStream inputStream = new BufferedInputStream(Files.asByteSource(file).openStream(), 64 * 1024)){
            DataInput dataInput = new DataInputStream(inputStream);
            this.trie = DoubleArrayTrie.read(dataInput, this::readItem);
        }
    }

    @Override
    public void loadFromRealData() throws Exception {
        TreeMap<String, V> map = new TreeMap<>();
        long t1 = System.currentTimeMillis();

        MynlpResource resource = environment.getMynlpResourceFactory().load(dicFilePath());

        Splitter splitter = Splitter.on(Pattern.compile("\\s"));
        try (CharSourceLineReader reader = resource.openLineReader()) {
            while (reader.hasNext()) {
                String line = reader.next();
                List<String> params = splitter.splitToList(line);
                if (!params.isEmpty()) {
                    List<String> attrs = params.subList(1, params.size());
                    V attribute = parseLine(attrs);
                    map.put(params.get(0), attribute);
                }
            }
        }

        filtermap(map);

        DoubleArrayTrieBuilder<V> builder = new DoubleArrayTrieBuilder<>();
        this.trie = builder.build(map);
    }


    /**
     * 查询一个单词
     *
     * @param key
     * @return 单词对应的条目
     */
    public V get(String key) {
        return trie.get(key);
    }

    public V get(String key, int offset, int len) {
        return trie.get(key, offset, len);
    }

    public V get(char[] text, int offset, int len) {
        return trie.get(text, offset, len);
    }


    /**
     * 是否含有键
     *
     * @param key
     * @return
     */
    public boolean contains(String key) {
        return get(key) != null;
    }

    /**
     * 词典大小
     *
     * @return
     */
    public int size() {
        return trie.size();
    }
}

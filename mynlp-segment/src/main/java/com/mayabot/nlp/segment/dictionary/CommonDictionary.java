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
import com.google.common.base.Splitter;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.mayabot.nlp.Environment;
import com.mayabot.nlp.ResourceLoader;
import com.mayabot.nlp.Settings;
import com.mayabot.nlp.collection.ValueSerializer;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieSerializer;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

import java.io.File;
import java.util.List;
import java.util.TreeMap;

/**
 * 通用的词典，对应固定格式的词典，但是标签可以泛型化
 *
 * @author hankcs
 * @author jimichan
 */
public abstract class CommonDictionary<V> {

    protected InternalLogger logger = InternalLoggerFactory.getInstance(this.getClass());

    private final ResourceLoader resourceLoader;

    private final Environment environment;

    private DoubleArrayTrie<V> trie;

    abstract public V parseLine(List<String> pars);

    abstract public String dicFilePath();

    abstract public ValueSerializer<V> valueSerializer();

    Settings setting;

    public CommonDictionary( Settings setting, ResourceLoader resourceLoader
            , Environment environment) {
        this.setting = setting;
        this.resourceLoader = resourceLoader;
        this.environment = environment;

        try {
            this.init(setting);
        } catch (Throwable e) {
            logger.error("e",e);

            throw new RuntimeException(e);
        }
    }

    private void init(Settings setting) throws Exception {
        Splitter splitter = Splitter.on(' ').omitEmptyStrings();

        // 如果存在bin文件
        ByteSource source = resourceLoader.loadDictionary(dicFilePath());

        File binfile = new File(environment.getWorkDirPath(), source.hash(Hashing.murmur3_128()).toString());


        if (binfile.exists() && binfile.canRead()) {
            long t1 = System.currentTimeMillis();
            // load from bin
            DoubleArrayTrieSerializer<V> ds = new DoubleArrayTrieSerializer<>();
            ds.setSerializer(valueSerializer());
            this.trie = ds.read(binfile);
            long t2 = System.currentTimeMillis();

            logger.info("核心词典开始加载:" + binfile.getAbsolutePath() + " use time " + (t2 - t1) + " ms");
        } else if (!source.isEmpty()) {
            // load from txt filed
            TreeMap<String, V> map = new TreeMap<>();
            long t1 = System.currentTimeMillis();

            source.asCharSource(Charsets.UTF_8).readLines().stream().
                    map(splitter::splitToList).
                    filter(params -> !params.isEmpty())
                    .forEach(params -> {
                        List<String> attrs = params.subList(1, params.size());
                        V attribute = parseLine(attrs);
                        map.put(params.get(0), attribute);
                    });

            filtermap(map);
            long t2 = System.currentTimeMillis();

            System.out.println((this.getClass().getSimpleName() + " load tree map use time " + (t2 - t1)));
            System.out.println(this.getClass().getSimpleName() + " tree map size " + map.size());

            DoubleArrayTrieBuilder<V> builder = new DoubleArrayTrieBuilder<>();
            this.trie = builder.build(map);

            long t3 = System.currentTimeMillis();
            System.out.println(this.getClass().getSimpleName() + " build dat trie use time " + (t3 - t2));

            DoubleArrayTrieSerializer<V> ds = new DoubleArrayTrieSerializer<>();
            ds.setBatchSize(5000);
            ds.setSerializer(valueSerializer());
            ds.write(this.trie, binfile);

            long t4 = System.currentTimeMillis();

            System.out.println(this.getClass().getSimpleName() + " write tire to bin format use time " + (t4 - t3));
        } else {
            throw new RuntimeException("not found dir file " + dicFilePath());
        }
    }

    protected void filtermap(TreeMap<String, V> map) {

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

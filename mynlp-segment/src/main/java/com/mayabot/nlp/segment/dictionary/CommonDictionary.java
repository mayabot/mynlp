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
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieMap;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResouceExternalizable;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionaryAbsWords;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.*;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * 通用的词典，对应固定格式的词典，但是标签可以泛型化
 * <p>
 * 由于抽象词变化了，加载原来的语料需要对应关系转换.
 *
 * @author hankcs
 * @author jimichan
 */
public abstract class CommonDictionary<V> extends NlpResouceExternalizable {

    protected InternalLogger logger = InternalLoggerFactory.getInstance(this.getClass());


    private DoubleArrayTrieMap<V> trie;

    public CommonDictionary(MynlpEnv mynlp) throws Exception {
        this.restore(mynlp);
    }

    protected abstract String dicFilePath();

    protected abstract V parseLine(List<String> pars);

    protected abstract void writeItem(V a, DataOutput out);

    protected abstract V readItem(DataInput in);

    protected void filtermap(TreeMap<String, V> map) {

    }

    @Override
    public String sourceVersion(MynlpEnv mynlp) {
        return mynlp.loadResource(dicFilePath()).hash().substring(0, 8);
    }

    private final Splitter splitter = Splitter.on(Pattern.compile("\\s"));


    /**
     * hanlp里面的label会和mynlp中的不一样。暂时统一在这里处理。
     *
     * @param label
     * @return
     */
    private String labelMap(String label) {
        switch (label) {
            case "始##始":
                return CoreDictionaryAbsWords.BIGIN_TAG;
            case "末##末":
                return CoreDictionaryAbsWords.TAG_END;
            case "未##团":
                return CoreDictionaryAbsWords.TAG_GROUP;
            case "未##数":
                return CoreDictionaryAbsWords.TAG_NUMBER;
            case "未##地":
                return CoreDictionaryAbsWords.TAG_PLACE;
            case "未##量":
                return CoreDictionaryAbsWords.TAG_QUANTIFIER;
            case "未##专":
                return CoreDictionaryAbsWords.TAG_NX;
            case "未##时":
                return CoreDictionaryAbsWords.TAG_TIME;
            case "未##串":
                return CoreDictionaryAbsWords.TAG_CLUSTER;
            case "未##人":
                return CoreDictionaryAbsWords.TAG_PEOPLE;
            default:
                return label;
        }
    }


    @Override
    public void loadFromSource(MynlpEnv mynlp) throws Exception {

        TreeMap<String, V> map = new TreeMap<>();

        NlpResource resource = mynlp.loadResource(dicFilePath());

        try (CharSourceLineReader reader = resource.openLineReader()) {
            while (reader.hasNext()) {
                String line = reader.next();
                List<String> params = splitter.splitToList(line);
                if (!params.isEmpty()) {
                    List<String> attrs = params.subList(1, params.size());
                    V attribute = parseLine(attrs);
                    map.put(labelMap(params.get(0)), attribute);
                }
            }
        }

        filtermap(map);

        this.trie = new DoubleArrayTrieMap<>(map);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        trie.save(out, this::writeItem);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        this.trie = new DoubleArrayTrieMap<>(in, this::readItem);

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

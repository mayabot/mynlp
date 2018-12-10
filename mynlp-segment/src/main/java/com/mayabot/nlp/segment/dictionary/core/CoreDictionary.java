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
package com.mayabot.nlp.segment.dictionary.core;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieStringIntMap;
import com.mayabot.nlp.resources.NlpResouceExternalizable;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.dictionary.DictionaryAbsWords;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.TreeMap;

/**
 * 使用DoubleArrayTrie实现的核心词典。
 * 核心词典文件CoreNatureDictionary.txt
 * HanLP的核心词典训练自人民日报2014语料，语料不是完美的，总会存在一些错误。
 * <pre>
 * 核心词性词频词典
 * 比如你在data/dictionary/CoreNatureDictionary.txt中发现了一个不是词的词，或者词性标注得明显不对，那么你可以修改它，然后删除缓存文件使其生效。
 * 目前CoreNatureDictionary.ngram.txt的缓存依赖于CoreNatureDictionary.txt的缓存，修改了后者之后必须同步删除前者的缓存，否则可能出错
 * 核心二元文法词典
 * 二元文法词典data/dictionary/CoreNatureDictionary.ngram.txt储存的是两个词的接续，如果你发现不可能存在这种接续时，删掉即可。
 * 你也可以添加你认为合理的接续，但是这两个词必须同时在核心词典中才会生效。
 * </pre>
 *
 * @author jimichan
 */
@Singleton
public class CoreDictionary extends NlpResouceExternalizable {
    /**
     * 现在总词频25146057
     */
    public static int MAX_FREQUENCY = -1;

//    private InternalLogger logger = InternalLoggerFactory.getInstance(CoreDictionary.class);

    public final String path = "dictionary/CoreDict.txt";

    /**
     * 词频总和
     */
    public int totalFreq;

    private DoubleArrayTrieStringIntMap trie;

    @Inject
    public CoreDictionary(MynlpEnv mynlp) throws Exception {

        this.restore(mynlp);

        MAX_FREQUENCY = this.totalFreq;
    }

    @Override
    @SuppressWarnings(value = "rawtypes")
    public void loadFromSource(MynlpEnv mynlp) throws Exception {
        NlpResource dictResource = mynlp.loadResource(path);

        //词和词频
        TreeMap<String, Integer> map = new TreeMap<>();

        int maxFreq = 0;

        try (CharSourceLineReader reader = dictResource.openLineReader()) {
            while (reader.hasNext()) {
                String line = reader.next();

                String[] param = line.split("\\s");

                Integer count = Integer.valueOf(param[1]);
                map.put(param[0], Integer.valueOf(count));
                maxFreq += count;
            }
        }

        this.totalFreq = maxFreq;

        //补齐，确保ID顺序正确
        for (String label : DictionaryAbsWords.allLabel()) {
            if (!map.containsKey(label)) {
                map.put(label, 1);
            }
        }

        if (map.isEmpty()) {
            throw new RuntimeException("not found core dict file ");
        }

        this.trie = new DoubleArrayTrieStringIntMap(map);
    }

    @Override
    public String sourceVersion(MynlpEnv mynlp) {
        return Hashing.md5().newHasher().
                putString(mynlp.loadResource(path).hash(), Charsets.UTF_8).
                putString("v2", Charsets.UTF_8)
                //如果Nature有任何变化
                .putString(Joiner.on(",").join(Nature.values()), Charsets.UTF_8)
                .hash().toString()
                .substring(0, 5);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(totalFreq);
        trie.save(out);
        out.flush();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        this.totalFreq = in.readInt();
        this.trie = new DoubleArrayTrieStringIntMap(in);
    }

    /**
     * 获取条目
     *
     * @param key
     * @return
     */
    public Integer get(String key) {
        return trie.get(key);
    }

    /**
     * 获取条目
     *
     * @param wordID
     * @return
     */
    public Integer get(int wordID) {
        return trie.get(wordID);
    }


    public int indexOf(CharSequence key) {
        return trie.indexOf(key);
    }

    public int indexOf(CharSequence key, int pos, int len, int nodePos) {
        return trie.indexOf(key, pos, len, nodePos);
    }

    public int indexOf(char[] chars, int pos, int len) {
        return trie.indexOf(chars, pos, len);
    }

    public int indexOf(char[] keyChars, int pos, int len, int nodePos) {
        return trie.indexOf(keyChars, pos, len, nodePos);
    }

    /**
     * 获取词频
     *
     * @param term
     * @return
     */
    public int getTermFrequency(String term) {
        Integer attribute = get(term);
        if (attribute == null) {
            return 0;
        }
        return attribute.intValue();
    }

    /**
     * 是否包含词语
     *
     * @param key
     * @return
     */
    public boolean contains(String key) {
        return trie.indexOf(key) >= 0;
    }

    /**
     * 获取词语的ID
     *
     * @param word
     * @return
     */
    public int getWordID(String word) {
        return trie.indexOf(word);
    }

    public DoubleArrayTrieStringIntMap.DATMapMatcherInt match(char[] text, int offset) {
        return trie.match(text, offset);
    }

    public int size() {
        return trie.size();
    }
}

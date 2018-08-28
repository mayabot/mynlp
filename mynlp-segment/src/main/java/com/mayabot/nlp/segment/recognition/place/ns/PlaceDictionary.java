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
package com.mayabot.nlp.segment.recognition.place.ns;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpIOC;
import com.mayabot.nlp.Setting;
import com.mayabot.nlp.collection.ahocorasick.AhoCoraickDoubleArrayTrieBuilder;
import com.mayabot.nlp.collection.ahocorasick.AhoCorasickDoubleArrayTrie;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.dictionary.EnumTransformMatrix;
import com.mayabot.nlp.segment.recognition.place.NSTag;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.io.IOException;
import java.util.TreeMap;

/**
 * 地名识别用的词典，实际上是对两个词典的包装
 *
 * @author hankcs
 */
@Singleton
public class PlaceDictionary {

    InternalLogger logger = InternalLoggerFactory.getInstance(Wordpath.class);


    /**
     * 转移矩阵词典
     */
    private EnumTransformMatrix<NSTag> transformMatrixDictionary;


    /**
     * 人名词典
     */
    private NSDictionary dictionary;

    /**
     * AC算法用到的Trie树
     */
    private AhoCorasickDoubleArrayTrie<NSPattern> trie;

    final Setting<String> orgTrDict = Setting.string("org.dict.tr", "dictionary/place/ns.tr.txt");

    @Inject
    public PlaceDictionary(NSDictionary dictionary, MynlpIOC mynlp) throws IOException {
        this.dictionary = dictionary;

        long start = System.currentTimeMillis();

        NlpResource resource = mynlp.loadResource(orgTrDict);

        //转移矩阵
        transformMatrixDictionary = new EnumTransformMatrix<>(resource);

        // AC tree
        {
            TreeMap<String, NSPattern> map = new TreeMap<>();
            for (NSPattern pattern : NSPattern.values()) {
                map.put(pattern.toString(), pattern);
            }

            AhoCoraickDoubleArrayTrieBuilder<NSPattern> acdaBuilder = new AhoCoraickDoubleArrayTrieBuilder<>();
            this.trie = acdaBuilder.build(map);
        }

        logger.info("PlaceDictionary 加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
    }


    /**
     * 地名识别模式串
     *
     * @author hankcs
     */
    public enum NSPattern {
        CH,
        CDH,
        CDEH,
        GH
    }

    public EnumTransformMatrix<NSTag> getTransformMatrixDictionary() {
        return transformMatrixDictionary;
    }

    public AhoCorasickDoubleArrayTrie<NSPattern> getTrie() {
        return trie;
    }


    public NSDictionary getDictionary() {
        return dictionary;
    }
}

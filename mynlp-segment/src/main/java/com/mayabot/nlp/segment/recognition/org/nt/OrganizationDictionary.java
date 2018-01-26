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
package com.mayabot.nlp.segment.recognition.org.nt;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.Environment;
import com.mayabot.nlp.collection.ahocorasick.AhoCoraickDoubleArrayTrieBuilder;
import com.mayabot.nlp.collection.ahocorasick.AhoCorasickDoubleArrayTrie;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.MynlpResource;
import com.mayabot.nlp.segment.corpus.tag.NTTag;
import com.mayabot.nlp.segment.dictionary.EnumTransformMatrix;

import java.io.IOException;
import java.util.TreeMap;

/**
 * 组织名识别用的词典，实际上是对两个词典的包装
 *
 * @author jimichan
 */
@Singleton
public class OrganizationDictionary {

    private InternalLogger logger = InternalLoggerFactory.getInstance(this.getClass());

    /**
     * 词典
     */
    private NTDictionary dictionary;

    /**
     * 转移矩阵词典
     */
    private EnumTransformMatrix<NTTag> transformMatrixDictionary;

    /**
     * AC算法用到的Trie树
     */
    private AhoCorasickDoubleArrayTrie<String> trie;

    @Inject
    public OrganizationDictionary(NTDictionary dictionary, Environment environment) throws IOException {
        this.dictionary = dictionary;

        long start = System.currentTimeMillis();

        MynlpResource resource = environment.loadResource("org.dict.tr", "inner://dictionary/organization/nt.tr.txt");


        //转移矩阵
        transformMatrixDictionary = new EnumTransformMatrix<>(resource);

        // AC tree
        {
            TreeMap<String, String> map = new TreeMap<>();
            for (String pattern : NTPattern.patterns) {
                map.put(pattern, pattern);
            }

            AhoCoraickDoubleArrayTrieBuilder<String> acdaBuilder = new AhoCoraickDoubleArrayTrieBuilder<>();
            this.trie = acdaBuilder.build(map);
        }


        logger.info("PlaceDictionary 加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
    }


    public EnumTransformMatrix<NTTag> getTransformMatrixDictionary() {
        return transformMatrixDictionary;
    }

    public AhoCorasickDoubleArrayTrie<String> getTrie() {
        return trie;
    }


    public NTDictionary getDictionary() {
        return dictionary;
    }
}

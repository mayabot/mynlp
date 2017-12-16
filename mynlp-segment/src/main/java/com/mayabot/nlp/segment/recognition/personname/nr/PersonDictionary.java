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
package com.mayabot.nlp.segment.recognition.personname.nr;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.Environment;
import com.mayabot.nlp.collection.ahocorasick.AhoCoraickDoubleArrayTrieBuilder;
import com.mayabot.nlp.collection.ahocorasick.AhoCorasickDoubleArrayTrie;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.MynlpResource;
import com.mayabot.nlp.segment.corpus.tag.NRTag;
import com.mayabot.nlp.segment.dictionary.EnumTransformMatrix;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

/**
 * 人名识别用的词典，实际上是对两个词典的包装
 *
 * @author hankcs
 */
@Singleton
public class PersonDictionary {

    final static String tsfile = "person" + File.separator + "nr.tr.txt";


    protected InternalLogger logger = InternalLoggerFactory.getInstance(this.getClass());

    /**
     * 转移矩阵词典
     */
    private EnumTransformMatrix<NRTag> transformMatrixDictionary;

    /**
     * AC算法用到的Trie树
     */
    private AhoCorasickDoubleArrayTrie<NRPattern> trie;

    @Inject
    public PersonDictionary(NRDictionary dictionary, Environment environment) throws IOException {
        this.dictionary = dictionary;

        long start = System.currentTimeMillis();

        MynlpResource resource = environment.loadResource("org.dict.tr", "inner://dictionary/person/nr.tr.txt");

        //转移矩阵
        transformMatrixDictionary = new EnumTransformMatrix<>(resource);

        // AC tree
        {
            TreeMap<String, NRPattern> map = new TreeMap<>();
            for (NRPattern pattern : NRPattern.values()) {
                map.put(pattern.toString(), pattern);
            }

            AhoCoraickDoubleArrayTrieBuilder<NRPattern> acdaBuilder = new AhoCoraickDoubleArrayTrieBuilder<>();
            this.trie = acdaBuilder.build(map);
        }


        logger.info("PlaceDictionary 加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
    }


    /**
     * 人名识别模式串
     *
     * @author hankcs
     */
    public enum NRPattern {
        BBCD,
        BBE,
        BBZ,
        BCD,
        BEE,
        BE,
        BC,
        BEC,
        BG,
        DG,
        EG,
        BXD,
        BZ,
        //    CD,
        EE,
        FE,
        FC,
        FB,
        FG,
        Y,
        XD,
//    GD,
    }

    public EnumTransformMatrix<NRTag> getTransformMatrixDictionary() {
        return transformMatrixDictionary;
    }

    public AhoCorasickDoubleArrayTrie<NRPattern> getTrie() {
        return trie;
    }

    /**
     * 人名词典
     */
    private NRDictionary dictionary;

    public NRDictionary getDictionary() {
        return dictionary;
    }
}

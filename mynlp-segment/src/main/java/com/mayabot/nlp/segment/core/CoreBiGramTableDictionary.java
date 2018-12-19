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

package com.mayabot.nlp.segment.core;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.TreeBasedTable;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.common.matrix.CSRSparseMatrix;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResouceExternalizable;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.common.ResourceLastVersion;
import com.mayabot.nlp.utils.CharSourceLineReader;
import com.mayabot.nlp.utils.DataInOutputUtils;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

/**
 * 核心词典的二元接续词典，采用整型储存，高性能。
 * 表示一个词接着另外一个词的概率次数
 * TODO 查询时，大部分组合不在内。使用BloomFilter
 * @author jimichan
 */
@Singleton
public class CoreBiGramTableDictionary extends NlpResouceExternalizable {

    private CSRSparseMatrix matrix;

    public final String path = "dictionary/CoreDict.bigram.txt";

    protected InternalLogger logger = InternalLoggerFactory.getInstance(this.getClass());

    private final CoreDictionary coreDictionary;

    @Inject
    public CoreBiGramTableDictionary(CoreDictionary coreDictionary, MynlpEnv mynlp) throws
            Exception {
        this.coreDictionary = coreDictionary;

        this.restore(mynlp);
    }

    @Override
    public String sourceVersion(MynlpEnv mynlp) {
        return Hashing.murmur3_32().newHasher().
                putString(mynlp.loadResource(path).hash(), Charsets.UTF_8).
                putString("v1", Charsets.UTF_8)
                .hash().toString();
    }

    @Override
    public void loadFromSource(MynlpEnv mynlp) throws Exception {

        NlpResource source = mynlp.loadResource(path);

        if (source == null) {
            logger.warn("Not Found Resource " + path);
            logger.warn(ResourceLastVersion.show(ResourceLastVersion.coreDict));
            System.exit(0);
        }

        TreeBasedTable<Integer, Integer, Integer> table = TreeBasedTable.create();

        Splitter splitter = Splitter.on(" ").omitEmptyStrings().trimResults();

        String firstWord = null;
        try (CharSourceLineReader reader = source.openLineReader()) {
            while (reader.hasNext()) {
                String line = reader.next();

                if (line.startsWith("\t")) {
                    int firstWh = line.indexOf(" ");
                    String numString = line.substring(1, firstWh);
                    int num = Ints.tryParse(numString);
                    List<String> words = splitter.splitToList(line.substring(firstWh) + 1);

                    String wordA = firstWord;

                    for (String wordB : words) {
                        int idA = coreDictionary.wordId(wordA);
                        if (idA >= 0) {
                            int idB = coreDictionary.wordId(wordB);
                            if (idB >= 0) {
                                table.put(idA, idB, num);
                            }
                        }
                    }

                } else {
                    firstWord = line;
                }
//
//
//                Matcher matcher = pattern.matcher(line);
//                if (matcher.find()) {
//                    String wordA = matcher.group(1);
//                    String wordB = matcher.group(2);
//                    String num = matcher.group(3);
//                    int idA = coreDictionary.wordId(wordA);
//                    if (idA >= 0) {
//                        int idB = coreDictionary.wordId(wordB);
//                        if (idB >= 0) {
//                            table.put(idA, idB, Ints.tryParse(num));
//                        }
//                    }
//                }
            }
        }

//        bloomFilter = TwoIntBloomFilter.create(table.size());
//
//        for (Table.Cell<Integer, Integer, Integer> cell : table.cellSet()) {
//            bloomFilter.put(cell.getRowKey(),cell.getColumnKey());
//        }


        this.matrix = new CSRSparseMatrix(table, coreDictionary.size());
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        DataInOutputUtils.writeIntArray(matrix.getColumnIndices(), out);
        DataInOutputUtils.writeIntArray(matrix.getRowOffset(), out);
        DataInOutputUtils.writeIntArray(matrix.getValues(), out);
//        bloomFilter.writeTo(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        int[] columnIndices = DataInOutputUtils.readIntArray(in);
        int[] rowOffset = DataInOutputUtils.readIntArray(in);
        int[] values = DataInOutputUtils.readIntArray(in);
        this.matrix = new CSRSparseMatrix(rowOffset, columnIndices, values);
//        this.bloomFilter = TwoIntBloomFilter.readFrom(in);
    }

    /**
     * 获取共现频次
     *
     * @param a 第一个词
     * @param b 第二个词
     * @return 第一个词@第二个词出现的频次
     */
    public int getBiFrequency(String a, String b) {


        int idA = coreDictionary.getWordID(a);
        if (idA < 0) {
            return 0;
        }
        int idB = coreDictionary.getWordID(b);
        if (idB < 0) {
            return 0;
        }
        return matrix.get(idA, idB);
    }

    /**
     * 获取共现频次
     *
     * @param idA 第一个词的id
     * @param idB 第二个词的id
     * @return 共现频次, 不存在就返回0
     */
    public int getBiFrequency(int idA, int idB) {
//        if (idA < 10) {
//            if (bloomFilter.mightContain(idA, idB)) {
//                return matrix.wordFreq(idA, idB);
//            }else{
//                return 0;
//            }
//        }else{
        return matrix.get(idA, idB);
//        }
    }
}

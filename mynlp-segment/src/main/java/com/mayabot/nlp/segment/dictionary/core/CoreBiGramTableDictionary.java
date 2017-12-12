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

package com.mayabot.nlp.segment.dictionary.core;

import com.google.common.base.Charsets;
import com.google.common.collect.TreeBasedTable;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.Environment;
import com.mayabot.nlp.ResourceLoader;
import com.mayabot.nlp.Settings;
import com.mayabot.nlp.collection.matrix.CSRSparseMatrix;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 核心词典的二元接续词典，采用整型储存，高性能。
 * 表示一个词接着另外一个词的概率次数
 *
 * @author hankcs
 * @author jimichan
 */
@Singleton
public class CoreBiGramTableDictionary {

    private CSRSparseMatrix matrix;

    private Settings setting;

    final String BiGramDictionaryPath = "core" + File.separator + "CoreNatureDictionary.ngram.txt";

    protected InternalLogger logger = InternalLoggerFactory.getInstance(this.getClass());

    private CoreDictionary coreDictionary;

    @Inject
    public CoreBiGramTableDictionary(Settings setting,
                                     CoreDictionary coreDictionary, ResourceLoader resourceLoader
            , Environment environment) throws
            IOException {
        this.setting = setting;
        this.coreDictionary = coreDictionary;
        logger.info("开始加载核心二元接续词典" + BiGramDictionaryPath);


        ByteSource source = resourceLoader.loadDictionary(BiGramDictionaryPath);

        File binCacheFile = new File(environment.getWorkDir(), "core.nature.ngram." + source.hash(Hashing.murmur3_128()).toString() + ".bin");

        long t1 = System.currentTimeMillis();
        if (binCacheFile.exists() && binCacheFile.canRead()) {
            try {
                matrix = this.loadFromBinFile(Files.asByteSource(binCacheFile));
                logger.info("加载核心二元接续词典完成[From Cache]，用时" + (System.currentTimeMillis() - t1) + " ms");
                return;
            } catch (Exception e) {
                logger.warn( "", e);
            }
        }
        if (matrix == null) {
            matrix = loadFromTxt(source);
            saveBinFile(matrix, Files.asByteSink(binCacheFile));
        }

        logger.info("加载核心二元接续词典完成，用时" + (System.currentTimeMillis() - t1) + " ms");
    }


    private CSRSparseMatrix loadFromTxt(ByteSource source) throws IOException {

        TreeBasedTable<Integer, Integer, Integer> table = source.asCharSource(Charsets.UTF_8).readLines(new LineProcessor<TreeBasedTable<Integer, Integer, Integer>>() {

            TreeBasedTable<Integer, Integer, Integer> table = TreeBasedTable.create();

            Pattern pattern = Pattern.compile("^(.+)@(.+)\\s+(\\d+)$");
            //——@费县	1

            @Override
            public boolean processLine(String line) throws IOException {

                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String wordA = matcher.group(1);
                    String wordB = matcher.group(2);
                    String num = matcher.group(3);
                    int idA = coreDictionary.indexOf(wordA);
                    if (idA >= 0) {
                        int idB = coreDictionary.indexOf(wordB);
                        if (idB >= 0) {
                            table.put(idA, idB, Ints.tryParse(num));
                        }
                    }
                }
                return true;
            }

            @Override
            public TreeBasedTable<Integer, Integer, Integer> getResult() {
                return table;
            }
        });

        CSRSparseMatrix matrix = new CSRSparseMatrix(table, coreDictionary.size());

        return matrix;

    }

//
//    private void saveBinFile(CSRSparseMatrix matrix,ByteSink outSink) {
//
//        FSTConfiguration fstConfiguration = FSTConfiguration.createStructConfiguration();
//
//        try (OutputStream out = outSink.openBufferedStream()) {
//            FSTObjectOutput objectOutput = fstConfiguration.getObjectOutput(out);
//            objectOutput.writeObject(matrix);
//            objectOutput.flush();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//    }
//
//    private CSRSparseMatrix loadFromBinFile(ByteSource source) throws IOException {
//
//        FSTConfiguration fstConfiguration = FSTConfiguration.createStructConfiguration();
//
//        try (FSTObjectInput objectInput = fstConfiguration.getObjectInput(source.openBufferedStream())) {
//
//            CSRSparseMatrix matrix = (CSRSparseMatrix) objectInput.readObject();
//
//            return matrix;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    private void saveBinFile(CSRSparseMatrix matrix, ByteSink outSink) {


        try (OutputStream out = outSink.openBufferedStream()) {
            ObjectOutputStream outputStream = new ObjectOutputStream(out);

            outputStream.writeObject(matrix);

            outputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private CSRSparseMatrix loadFromBinFile(ByteSource source) throws Exception {

        try (ObjectInputStream objectInput = new ObjectInputStream(source.openBufferedStream())) {

            CSRSparseMatrix matrix = (CSRSparseMatrix) objectInput.readObject();

            return matrix;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        if (idA == -1) {
            return 0;
        }
        int idB = coreDictionary.getWordID(b);
        if (idB == -1) {
            return 0;
        }
        return matrix.get(idA, idB);
    }

    /**
     * 获取共现频次
     *
     * @param idA 第一个词的id
     * @param idB 第二个词的id
     * @return 共现频次
     */
    public int getBiFrequency(int idA, int idB) {
        return matrix.get(idA, idB);
    }
}

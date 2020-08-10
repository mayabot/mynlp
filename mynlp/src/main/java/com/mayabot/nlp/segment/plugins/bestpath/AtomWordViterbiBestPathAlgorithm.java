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

package com.mayabot.nlp.segment.plugins.bestpath;

import com.mayabot.nlp.common.injector.Singleton;
import com.mayabot.nlp.segment.common.VertexHelper;
import com.mayabot.nlp.segment.lexer.core.BiGramTableDictionary;
import com.mayabot.nlp.segment.lexer.core.CoreDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.VertexRow;
import com.mayabot.nlp.segment.wordnet.Wordnet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jimichan
 */
@Singleton
public final class AtomWordViterbiBestPathAlgorithm {

    private BiGramTableDictionary coreBiGramTableDictionary;

    /**
     * 平滑参数
     */
    public final double dSmoothingPara = 0.1;
    /**
     * Smoothing 平滑因子
     */
    public final double dTemp;

    /**
     * 来自Hanlp里面的算法
     *
     * @param from
     * @param to
     * @return
     */
    private final double partA = (1 - dSmoothingPara);
    private final double partB;
    private final double PARTA_PARTB;
    private final double PARTTA_Dtemp;
    private final double PartZ;
    final double value1;
    final double value2;
    final double value3;
    final double value4;
    final double value5;
    final double[] values = new double[21];

    public AtomWordViterbiBestPathAlgorithm(
            BiGramTableDictionary coreBiGramTableDictionary,
            CoreDictionary coreDictionary) {
        this.coreBiGramTableDictionary = coreBiGramTableDictionary;
        dTemp = (double) 1 / coreDictionary.totalFreq() + 0.00001;
        partB = (1 - dTemp);
        PARTA_PARTB = partA * partB;
        PARTTA_Dtemp = partA * dTemp;
        PartZ = dSmoothingPara / coreDictionary.totalFreq();

        for (int i = 0; i < 21; i++) {
            values[i] = Math.abs(-Math.log(PartZ * i + PARTTA_Dtemp));
        }

        value1 = values[1];
        value2 = values[2];
        value3 = values[3];
        value4 = values[4];
        value5 = values[5];
    }

    /**
     * 使用viterbi比算法在wordnet指定区间内选择字词的最优路径
     *
     * @param wordnet
     * @param from
     * @param len
     * @return List<Vertex>
     */
    public List<Vertex> selectSub(Wordnet wordnet, int from, int len) {
        int theEndIndex = from + len;
        // 第一行的From肯定来自Start节点
        Vertex startNode = VertexHelper.newBegin();
        Vertex endNode = VertexHelper.newEnd();

        Vertex wStart = wordnet.getBeginRow().first();

        boolean u = true;
        for (Vertex v = wordnet.getRow(from).first(); v != null; v = v.next()) {
            if (v.length >= len) {
                continue;
            }
            updateFrom(v, startNode);
            u = false;
        }

        if (u) {
            return null;
        }

        for (int i = 0; i < len; i++) {

            final VertexRow row = wordnet.row(i + from);

            if (row.isEmpty()) {
                continue;
            }

            for (Vertex node = row.first(); node != null; node = node.next()) {

                if (node.from == null || node.length == len) {
                    continue;
                }

                if (i + node.length > len) {

                } else {
                    if (i + node.length == len) {
                        updateFrom(endNode, node);
                    } else {
                        final VertexRow toRow = wordnet.row(from + i + node.length);

                        if (toRow.first() != null) {
                            for (Vertex to = toRow.first(); to != null; to = to.next()) {
                                int iend = to.getRowNum() + to.length;
                                if (iend <= theEndIndex) {
                                    updateFrom(to, node);
                                }
                            }
                        }
                    }
                }
            }
        }

        //TODO 这个算法还可以优化性能
        //从后到前，获得完整的路径
        ArrayList<Vertex> result = new ArrayList<>(4);

        Vertex point = null;
        if (from + len > wordnet.getCharSizeLength()) {
            point = wordnet.getEndRow().first();
        } else {
            point = endNode;
        }
        final Vertex end = point;

        boolean notOne = false;

        while (point != null) {
            if (point != end && point != startNode && point != wStart) {
                if (point.getRowNum() < from) {
                    break;
                }
                result.add(point);
                if (point.length > 1) {
                    notOne = true;
                }
            }
            point = point.from;
        }

        if (result.size() == 1 || result.isEmpty() || !notOne) {
            return null;
        }

        Collections.reverse(result);

        return result;
    }


    private void updateFrom(Vertex the, Vertex from) {

        //是权重越小越好 距离越短
        double weight = from.weight + calculateWeight(from, the);
        if (the.from == null || the.weight > weight) {
            the.from = from;
            the.weight = weight;
        }
    }


    private double calculateWeight(Vertex from, Vertex to) {
        int frequency = from.freq;
        if (frequency == 0) {
            // 防止发生除零错误
            frequency = 1;
        }

        // TODO CHECKME
//		if(to.wordID<0){
//			// 自定义词典，会强行插入一些非核心词典里面的词汇. 这里故意让得分变高，让他成为必须，即使再次执行viterbi选择
//			return -1000;
//		}


        int nTwoWordsFreq = coreBiGramTableDictionary.getBiFrequency(from.wordID, to.wordID);
//        double value = -Math
//                .log(Predefine.dSmoothingPara * frequency / (Predefine.totalFreq) +
//                        partA* (partB * nTwoWordsFreq / frequency + Predefine.dTemp));
//        System.out.println(from.realWord()+"->"+to.realWord()+"="+nTwoWordsFreq);
        double value = 0;

        if (nTwoWordsFreq > 0) {
            value = -Math
                    .log(PartZ * frequency
                            +
                            PARTA_PARTB * nTwoWordsFreq / frequency

                            + PARTTA_Dtemp
                    );

        } else {
            if (frequency == 1) {
                value = value1;
            } else if (frequency == 2) {
                value = value1;
            } else if (frequency == 3) {
                value = value3;
            } else if (frequency == 4) {
                value = value4;
            } else if (frequency == 5) {
                value = value5;
            } else {
                value = -Math
                        .log(PartZ * frequency + PARTTA_Dtemp
                        );

            }
        }

        if (value < 0.0) {
            value = -value;
        }


        return value;
    }


}

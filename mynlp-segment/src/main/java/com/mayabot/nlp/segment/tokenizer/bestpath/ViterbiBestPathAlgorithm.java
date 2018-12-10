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

package com.mayabot.nlp.segment.tokenizer.bestpath;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.segment.dictionary.core.CoreBiGramTableDictionary;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.wordnet.*;

/**
 * 基于核心词典的bi词之前的共出现的次数，采用viterbi选择出一个概率最大的path
 *
 * @author jimichan
 */
@Singleton
public class ViterbiBestPathAlgorithm implements BestPathAlgorithm {

    protected CoreBiGramTableDictionary coreBiGramTableDictionary;


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
    protected final double partA = (1 - dSmoothingPara);
    protected final double partB;
    protected final double PARTA_PARTB;
    protected final double PARTTA_Dtemp;
    protected final double PartZ;


    @Inject
    public ViterbiBestPathAlgorithm(CoreBiGramTableDictionary coreBiGramTableDictionary,
                                    CoreDictionary coreDictionary) {
        this.coreBiGramTableDictionary = coreBiGramTableDictionary;
        dTemp = (double) 1 / coreDictionary.totalFreq + 0.00001;
        partB = (1 - dTemp);
        PARTA_PARTB = partA * partB;
        PARTTA_Dtemp = partA * dTemp;
        PartZ = dSmoothingPara / coreDictionary.totalFreq;
    }


    /**
     * 在原因的path基础上。多个识别器做了修改。
     * 1. 合成词
     * 2. 截断+合成
     *
     * @param wordnet
     * @return
     */
    @Override
    public Wordpath select(Wordnet wordnet) {

        //从第二个字符节点开始，一直到最后一个字符
        final int charSize = wordnet.getCharSizeLength();


        // 第一行的From肯定来自Start节点

        for (Vertex v = wordnet.getRow(0).first(); v != null; v = v.next()) {
            updateFrom(wordnet, v, wordnet.getBeginRow().getFirst());
        }


        for (int i = 0; i < charSize; i++) {

            final VertexRow row = wordnet.row(i);

            if (row.isEmpty()) {
                continue;
            }

            for (Vertex node = row.first(); node != null; node = node.next()) {

                if (node.from == null) {
                    continue;
                }

                final VertexRow toRow = wordnet.row(i + node.length);

                if (toRow.first() != null) {
                    for (Vertex to = toRow.first(); to != null; to = to.next()) {
                        updateFrom(wordnet, to, node);
                    }
                }
            }

        }

        return buildPath(wordnet);
    }


    protected void updateFrom(Wordnet wordnet, Vertex the, Vertex from) {

        //是权重越小越好 距离越短
        double weight = from.weight + calculateWeight(from, the);
        if (the.from == null || the.weight > weight) {
            the.from = from;
            the.weight = weight;
        }
    }


    protected double calculateWeight(Vertex from, Vertex to) {
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
        double value = -Math
                .log(PartZ * frequency
                        +
                        PARTA_PARTB * nTwoWordsFreq / frequency

                        + PARTTA_Dtemp
                );
        if (value < 0.0) {
            value = -value;
        }


        return value;
    }


    /**
     * 从后到前。根据权重获取最优路径
     *
     * @param wordnet
     * @return
     */
    protected Wordpath buildPath(Wordnet wordnet) {
        //从后到前，获得完整的路径
        Wordpath wordPath = new Wordpath(wordnet);

        Vertex last = null;

        Vertex point = wordnet.getEndRow().first();

        while (point != null) {
            last = point;
            wordPath.combine(point);
            point = point.from;
        }

        //System.out.println(wordnet.toMoreString());

        // 最后一个point必定指向start节点

        Preconditions.checkState(last == wordnet.getBeginRow().first(), "非完整路径,有可能wordnet初始化的时候就路径不完整");

        return wordPath;
    }

}

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

package com.mayabot.nlp.segment.wordnet;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.segment.common.Predefine;
import com.mayabot.nlp.segment.dictionary.core.CoreBiGramTableDictionary;

/**
 * 基于核心词典的bi词之前的共出现的次数，采用viterbi选择出一个概率最大的path
 *
 * @author jimichan
 */
@Singleton
public class ViterbiBestPathComputer implements BestPathComputer {

    public final static String NAME = "viterbi";


    private CoreBiGramTableDictionary coreBiGramTableDictionary;

    @Inject
    public ViterbiBestPathComputer(CoreBiGramTableDictionary coreBiGramTableDictionary) {
        this.coreBiGramTableDictionary = coreBiGramTableDictionary;
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

        final boolean optimizeNet = wordnet.isOptimizeNet();

        if (optimizeNet) {
            // AB C D => A BCD
            // AB CD => ABC D
            // A B CD => ABC D
            // AB C DE => A BCD E
            // TODO 没有覆盖 AB C DE => A BCD E 这个情况
            // TODO  AB CD => A BC D
            for (int i = 0; i < charSize; i++) {

                final VertexRow row = wordnet.row(i);

                for (Vertex node = row.first(); node != null; node = node.next()) {

                    final VertexRow toRow = wordnet.row(i + node.length);
                    boolean hasOptimizeNode = false;
                    boolean hasOptimizeNewNode = false;

                    for (Vertex n = toRow.first(); n != null; n = n.getNext()) {
                        if (n.isOptimize()) {
                            hasOptimizeNode = true;
                        }
                        if (n.isOptimizeNewNode()) {
                            hasOptimizeNewNode = true;
                        }
                    }

                    if (node.isOptimize()) {
                        if (node.isOptimizeNewNode()) {
                            //龚学 平等  => 龚学平 等
                            //如果被跳转后，不是优化网络节点
                            if (!hasOptimizeNode) {
                                for (Vertex n = toRow.first(); n != null; n = n.getNext()) {
                                    if (!n.isOptimize()) {
                                        n.setOptimize(true);
                                        n.setOptimizeNewNode(true);
                                    }
                                }
                            }
                        }

                    } else {
                        // 有关 天 陪 => 有 关天培
                        // 当前不是优化节点。但是去调整到有优化新节点的
                        if (hasOptimizeNewNode) {
                            node.setOptimize(true);
                            node.setOptimizeNewNode(true);
                        }
                    }
                }
            }
        }

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

                if (node.from == null || (optimizeNet && !node.isOptimize())) {
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


    private void updateFrom(Wordnet wordnet, Vertex the, Vertex from) {

        //是权重越小越好 距离越短
        double weight = from.weight + calculateWeight(from, the);
        if (the.from == null || the.weight > weight) {
            the.from = from;
            the.weight = weight;
        }
    }


    /**
     * 来自Hanlp里面的算法
     *
     * @param from
     * @param to
     * @return
     */
    private double calculateWeight(Vertex from, Vertex to) {
        int frequency = from.natureAttribute.getTotalFrequency();
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
        double value = -Math
                .log(Predefine.dSmoothingPara * frequency / (Predefine.MAX_FREQUENCY) + (1 - Predefine.dSmoothingPara)
                        * ((1 - Predefine.dTemp) * nTwoWordsFreq / frequency + Predefine.dTemp));
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
    private Wordpath buildPath(Wordnet wordnet) {
        //从后到前，获得完整的路径
        Wordpath wordPath = new Wordpath(wordnet, this);

        Vertex last = null;

        Vertex point = wordnet.getEndRow().first();

        while (point != null) {
            last = point;
            wordPath.combine(point);
            point = point.from;
        }

        // 最后一个point必定指向start节点
        Preconditions.checkState(last == wordnet.getBeginRow().first(), "非完整路径,有可能wordnet初始化的时候就路径不完整");

        return wordPath;
    }

}

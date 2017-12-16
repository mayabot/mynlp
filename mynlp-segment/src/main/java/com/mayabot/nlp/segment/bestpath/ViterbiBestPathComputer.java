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

package com.mayabot.nlp.segment.bestpath;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.segment.corpus.tag.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreBiGramTableDictionary;
import com.mayabot.nlp.segment.wordnet.*;
import com.mayabot.nlp.utils.Predefine;

/**
 * 基于核心词典的bi词之前的共出现的次数，采用viterbi选择出一个概率最大的path
 *
 * @author jimichan
 */
@Singleton
public class ViterbiBestPathComputer implements BestPathComputer {


    private CoreBiGramTableDictionary coreBiGramTableDictionary;

    @Inject
    public ViterbiBestPathComputer(CoreBiGramTableDictionary coreBiGramTableDictionary) {
        this.coreBiGramTableDictionary = coreBiGramTableDictionary;
    }

    @Override
    public Wordpath select(Wordnet wordnet) {

        // 第一行的From肯定来自Start节点
        for (Vertex v : wordnet.getRow(0)) {
            updateFrom(wordnet, v, wordnet.getBeginRow().getFirst());
        }

        //从第二个字符节点开始，一直到最后一个字符
        int charSize = wordnet.getCharSizeLength();
        for (int i = 0; i < charSize; i++) {
            VertexRow row = wordnet.row(i);
            if (row.isEmpty()) {
                continue;
            }

            boolean isOptimizeNet = wordnet.isOptimizeNet();

            Vertex _f = row.first();
            do {
                final Vertex node = _f;
                _f = _f.next();


                if (node.from == null) {

                    if (isOptimizeNet && node.isOptimize() && row.getRowNum() > -1) {
                        //这种情况很难发生
                        final int rowNum = row.getRowNum();
                        double weight = Double.MAX_VALUE;
                        Vertex from = null;
                        int count = 0;
                        for (int k = row.getRowNum() - 1; k >= 0; k--) {
                            VertexRow pre = wordnet.getRow(k);
                            if (!pre.isEmpty()) {
                                Vertex f = pre.getFirst();
                                while (f != null) {
                                    if (k + f.length() == rowNum) {
                                        if (f.weight < weight) {
                                            weight = f.weight;
                                            from = f;
                                        }
                                    }
                                    f = f.next();
                                }

                            }
                            count++;
                            if (count > 10) { // 最大不要超过10
                                break;
                            }
                        }
                        node.from = from;
                    }

                    if (node.from == null) {
                        continue;
                    }
                    continue;
                }

                // 如果在优化网络的模式下，非优化节点，就不要去计算了，当他们不存在。
                // 不知道会不会有问题
                if (isOptimizeNet && !node.isOptimize()) {
                    // 优化网络 少去计算
//                     System.out.println("--"+node.realWord());
                    continue;
                }

                //检查后驱


                VertexRow toRow = wordnet.row(i + node.getLength());

                //自动弥补
                if (toRow.isEmpty()) {
                    Vertex one = new Vertex(1, -1, null, NatureAttribute.create(Nature.x, 1));
                    toRow.put(one);
                }


                if (!toRow.isEmpty()) {
                    Vertex ___f = toRow.first();
                    do {
                        final Vertex to = ___f;
                        ___f = ___f.next();


                        // 表明是优化网络中新增的节点
                        if (isOptimizeNet) {// 处理新节点无后的情况,保证连贯性
                            if (node.isOptimizeNewNode() && !to.isOptimize()) {
                                to.setOptimize(true);
                            }

                            if (!to.isOptimize()) {
                                // 优化网络 少去计算
                                continue;
                            }
                        }

                        updateFrom(wordnet, to, node);

                    } while (___f != null);
                }


            } while (_f != null);
        }

        //从后到前，获得完整的路径
        Wordpath wordPath = new Wordpath(wordnet,this);
        Vertex point = wordnet.getEndRow().getFirst();
        while (point != null) {
            wordPath.combine(point);
            point = point.from;
        }

        return wordPath;
    }


    private void updateFrom(Wordnet wordnet, Vertex the, Vertex from) {
        //FIXME 检查adjust权重在这里add是否正确
        //提高效率。利用之前的权重。也算为优化网络计算出了力
//		if(from.getRowNum() >=0 && (!(the.isOptimize() || from.change))){
//			if (wordnet.isOptimizeNet()) {
//				System.out.println(the.theChar());
//			}
//			return;
//		}

        //是权重越小越好 距离越短
        double weight = from.weight + calculateWeight(from, the);
        if (the.from == null || the.weight > weight) {
            the.from = from;
            the.weight = weight;
//			if(wordnet.isOptimizeNet()){
//				the.change = true;
//			}
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
            frequency = 1; // 防止发生除零错误
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

}

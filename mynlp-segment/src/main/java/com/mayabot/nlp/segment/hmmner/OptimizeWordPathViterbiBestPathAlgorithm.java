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

package com.mayabot.nlp.segment.hmmner;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.segment.dictionary.core.CoreBiGramTableDictionary;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.tokenizer.bestpath.ViterbiBestPathAlgorithm;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.VertexRow;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

/**
 * 基于核心词典的bi词之前的共出现的次数，采用viterbi选择出一个概率最大的path
 *
 * @author jimichan
 */
@Singleton
public class OptimizeWordPathViterbiBestPathAlgorithm extends ViterbiBestPathAlgorithm {

    @Inject
    public OptimizeWordPathViterbiBestPathAlgorithm(CoreBiGramTableDictionary coreBiGramTableDictionary,
                                                    CoreDictionary coreDictionary) {
        super(coreBiGramTableDictionary, coreDictionary);
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


}

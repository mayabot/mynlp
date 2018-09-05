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

/**
 * 选择最佳路径。viterbi 维特比 viterbi算法 dijkstra算法 NShort算法 最笨的前向最大路径算法
 *
 * @author jimichan
 */
public interface BestPathComputer {

    /**
     * 从词图网络中选择一条从头到尾的路径
     *
     * @param wordnet
     * @return
     */
    Wordpath select(Wordnet wordnet);


    /**
     * 前向最大路径算法
     */
    BestPathComputer longpath = new BestPathComputer() {
        @Override
        public Wordpath select(Wordnet wordnet) {
            //从后到前，获得完整的路径
            final Wordpath wordPath = new Wordpath(wordnet, this);

            int point = 0;
            final int len = wordnet.length() - 1;

            while (point <= len) {

                VertexRow row = wordnet.row(point);

                int wordLen = row.lastLen();
                if (wordLen == 0) {
                    wordLen = 1;
                }

                wordPath.combine(point, wordLen);

                point += wordLen;
            }

            // 最后一个point必定指向start节点
            Preconditions.checkState(point != len, "非完整路径,有可能wordnet初始化的时候就路径不完整");
            return wordPath;
        }

    } ;
}

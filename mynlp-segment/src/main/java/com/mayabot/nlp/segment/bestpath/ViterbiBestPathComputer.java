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

package com.mayabot.nlp.segment.bestpath;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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

    public final static String name = "viterbi";


    private CoreBiGramTableDictionary coreBiGramTableDictionary;

    @Inject
    public ViterbiBestPathComputer(CoreBiGramTableDictionary coreBiGramTableDictionary) {
        this.coreBiGramTableDictionary = coreBiGramTableDictionary;
    }

    /**
     * -1	¤ 	|		BEGIN
     * 0	这 	|		这 {rzv=54791}(4.070759440936601)		[Y]这里 {rzs=3843}(4.579405126197817)
     * 1	里 	|		里 {f=16722}(12.46164245387843)
     * 2	有 	|		有 {vyou=85663}(7.544684832432196)		[Y]有关 {vn=5733, v=2741}(15.203678100804673)
     * 3	关 	|		关 {n=1388}(15.503126753728335)		[Y]关天[未##人] {nr=607718, nrf=113445}		[Y]关天培[未##人] {nr=607718, nrf=113445}
     * 4	天 	|		[Y]天 {qt=8660}(25.26417248000554)
     * 5	培 	|		[Y]培 {v=78}(5.8510305753092355)
     * 6	的 	|		[Y]的 {ude1=857740}(3.1812404559835485)
     * 7	烈 	|		烈 {a=43}(52.56616577195564)		[Y]烈士 {nnt=146}(8.849262104347634)
     * 8	士 	|		士 {ng=132}(64.16173487214445)
     * 9	. 	|		[Y]. {w=10000}(20.401302810184667)
     * 10	龚 	|		[Y]龚 {nz=33}(30.329012216428193)		[Y]龚学[未##人] {nr=607718, nrf=113445}(30.329012216428193)		[Y]龚学平[未##人] {nr=607718, nrf=113445}(30.329012216428193)
     * 11	学 	|		[Y]学 {v=1824}(41.928910055744744)
     * 12	平 	|		[Y]平 {v=1007}(36.18004279173743)		[Y]平等 {a=542}(36.18004279173743)
     * 13	等 	|		[Y]等 {udeng=59855}(34.67881357497878)
     * 14	领 	|		领 {v=721}(108.06111850914337)		[Y]领导 {n=7055}(40.92071359722565)
     * 15	导 	|		导 {vg=216, v=74}(119.39983526854556)
     * 16	, 	|		[Y], {w=10000}(51.12282609430713)
     * 17	  	|		[Y]  {w=10000}(61.05053550055065)
     * 18	邓 	|		[Y]邓 {nz=285}(70.97824490679417)		[Y]邓颖[未##人] {nr=607718, nrf=113445}(70.97824490679417)		[Y]邓颖超[未##人] {nr=607718, nrf=113445}(70.97824490679417)
     * 19	颖 	|		[Y]颖 {nz=35}(82.47439493108307)
     * 20	超 	|		[Y]超 {v=2397}(76.82927548210341)		[Y]超生 {vi=449, vn=34}(76.82927548210341)
     * 21	生 	|		[Y]生 {v=2977, vn=431}(76.82927548210341)		[Y]生前[未##时] {t=757118}(75.13792938558636)
     * 22	前 	|		前 {f=16954}(172.6354017910788)
     * 23	¶ 	|		END
     *
     * @param wordnet
     * @return
     */

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

                    for(Vertex n = toRow.first();n!=null;n = n.getNext()) {
                        if(n.isOptimize()){
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

                if(node.from == null || (optimizeNet && !node.isOptimize())){
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

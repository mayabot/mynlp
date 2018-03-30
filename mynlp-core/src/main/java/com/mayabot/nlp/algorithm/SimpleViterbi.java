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

package com.mayabot.nlp.algorithm;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;

/**
 * 一次性对象
 * 一个通用版本
 * 一个序列，每个节点都会对应一个Map Tag,Integer的结构。
 * 选择出来，然后通知每个节点去处理或保存。
 * 简化的快速的viterbi版本
 *
 * @author jimichan
 */
public class SimpleViterbi<TagType, ObjType> {


    private final BiConsumer<ObjType, TagType> consumer;
    private final ToDoubleBiFunction scorer;
    private final Function<ObjType, Map<TagType, Integer>> mapFunction;


    /**
     * @param scorer      打分算法
     * @param mapFunction 一个Node，获得tag-score健值对
     * @param consumer    set选定的标签
     */
    public SimpleViterbi(
            ToDoubleBiFunction<Map.Entry<TagType, Integer>,
                    Map.Entry<TagType, Integer>> scorer,
            Function<ObjType, Map<TagType, Integer>> mapFunction,
            BiConsumer<ObjType, TagType> consumer
    ) {
        this.consumer = consumer;
        this.scorer = scorer;
        this.mapFunction = mapFunction;
    }

    /**
     * 面向迭代流
     *
     * @param pathWithBE
     */
    public void viterbi(Iterable<ObjType> pathWithBE) {

        double[] row0 = new double[8];// 最多一个位置有64个标记
        double[] row1 = new double[8];

        Iterator<ObjType> iterator = pathWithBE.iterator();

        final ObjType begin = iterator.next();


        //第一个是Start节点
        Map.Entry<TagType, Integer> firstSelect = mapFunction.apply(begin).entrySet().iterator().next();
        consumer.accept(begin, firstSelect.getKey());


        ObjType preObj = begin;


        //第二个节点
        // Map<TagType, Integer> preTagSet = null;
        {
            ObjType the = iterator.next();
            Map<TagType, Integer> item = mapFunction.apply(the);
            int j = 0;

            if (item.size() > row0.length) {
                row0 = new double[item.size()];
            }

            for (Map.Entry<TagType, Integer> cur : item.entrySet()) {
                row0[j] = scorer.applyAsDouble(firstSelect, cur);
                j++;
            }

            //preTagSet = item;
            preObj = the;
        }

        double[] theCost = row0;
        double[] preCost = row1;

        //第三个节点
        ObjType the = null;
        while (iterator.hasNext()) {
            the = iterator.next();

            //交换 cost
            double[] _t = theCost;
            theCost = preCost; //第一次 theCost == 1
            preCost = _t;

            clear(theCost);

            Map<TagType, Integer> item = mapFunction.apply(the);

            {
                if (item.size() > theCost.length) {
                    theCost = new double[item.size()];
                }
            }

            double perfect_cost_line = Double.MAX_VALUE;
            Map.Entry<TagType, Integer> pre = null;
            int k = 0;

            for (Map.Entry<TagType, Integer> cur : item.entrySet()) {
                theCost[k] = Double.MAX_VALUE;
                int j = 0;
                for (Map.Entry<TagType, Integer> p : mapFunction.apply(preObj).entrySet()) {
                    double now = preCost[j] + scorer.applyAsDouble(p, cur);
                    if (now < theCost[k]) {
                        theCost[k] = now;
                        if (now < perfect_cost_line) {
                            perfect_cost_line = now;
                            pre = p;
                        }
                    }
                    ++j;
                }
                ++k;
            }

            if (pre == null) {
                consumer.accept(preObj, null);
            } else {
                consumer.accept(preObj, pre.getKey());
            }

            preObj = the;
        }

        //END节点也需要通知
        consumer.accept(preObj, mapFunction.apply(preObj).keySet().iterator().next());

    }

    /**
     * 提供一个面向Array的算法
     *
     * @param array
     */
    public void viterbi(ObjType[] array) {

        double[] row0 = new double[64];// 最多一个位置有64个标记
        double[] row1 = new double[64];
        //double[][] cost = new double[2][64];

        //第一个是Start节点
        final ObjType begin = array[0];
        Map.Entry<TagType, Integer> firstSelect = mapFunction.apply(begin).entrySet().iterator().next();
        consumer.accept(begin, firstSelect.getKey());


        //第二个节点
        {
            //cost[0] = new double[item.size()];
            int j = 0;
            //clear(cost[0]);
            for (Map.Entry<TagType, Integer> cur : mapFunction.apply(array[1]).entrySet()) {
                //noinspection unchecked
                row0[j] = scorer.applyAsDouble(firstSelect, cur);
                j++;
            }
        }

        double[] theCost = row0;
        double[] preCost = row1;
        //第三个节点
        for (int i = 2; i < array.length; i++) {

            //交换 cost
            double[] _t = theCost;
            theCost = preCost; //第一次 theCost == 1
            preCost = _t;

            Map<TagType, Integer> item = mapFunction.apply(array[i]);
            clear(theCost);

            double perfect_cost_line = Double.MAX_VALUE;
            int k = 0;
            Map.Entry<TagType, Integer> pre = null;
            for (Map.Entry<TagType, Integer> cur : item.entrySet()) {
                theCost[k] = Double.MAX_VALUE;
                int j = 0;
                for (Map.Entry<TagType, Integer> p : mapFunction.apply(array[i - 1]).entrySet()) {
                    double now = preCost[j] + scorer.applyAsDouble(p, cur);
                    if (now < theCost[k]) {
                        theCost[k] = now;
                        if (now < perfect_cost_line) {
                            perfect_cost_line = now;
                            pre = p;
                        }
                    }
                    ++j;
                }
                ++k;
            }

            consumer.accept(array[i - 1], pre.getKey());
        }

        consumer.accept(array[array.length - 1], mapFunction.apply(array[array.length - 1]).keySet().iterator().next());

    }

    private void clear(double[] doubles) {
        for (int i = doubles.length - 1; i >= 0; i--) {
            doubles[i] = 0;
        }
    }

}

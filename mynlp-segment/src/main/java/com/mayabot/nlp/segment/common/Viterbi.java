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

package com.mayabot.nlp.segment.common;

import com.mayabot.nlp.segment.dictionary.EnumTransformMatrix;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 提供通用的二阶Viterbi实现.
 * 滚动的计算了两层，没有全局地计算。
 * 从Hanlp代码中重构而来。
 * @author jimichan
 */
public class Viterbi {

    /**
     * 仅仅利用了转移矩阵的“维特比”算法
     * 作者 hancs
     *
     * @param roleTagList               观测序列
     * @param transformMatrixDictionary 转移矩阵
     * @param <E>                       EnumItem的具体类型
     * @return 预测结果
     */
    public static <E extends Enum<E>> List<E> computeEnumSimply(List<EnumFreqPair<E>> roleTagList, EnumTransformMatrix<E> transformMatrixDictionary) {
        int length = roleTagList.size() - 1;
        List<E> tagList = new LinkedList<E>();
        Iterator<EnumFreqPair<E>> iterator = roleTagList.iterator();
        EnumFreqPair<E> start = iterator.next();

        E pre = start.oneKey();
        E perfect_tag = pre;

        // 第一个是确定的
        tagList.add(pre);
        for (int i = 0; i < length; ++i) {
            double perfect_cost = Double.MAX_VALUE;
            EnumFreqPair<E> item = iterator.next();
            for (E cur : item.keySet()) {
                double now = transformMatrixDictionary.getFrequency(pre, cur) - Math.log((item.getFrequency(cur) + 1e-8) / transformMatrixDictionary.getTotalFrequency(cur));
                if (perfect_cost > now) {
                    perfect_cost = now;
                    perfect_tag = cur;
                }
            }
            pre = perfect_tag;
            tagList.add(pre);
        }

        return tagList;
    }

    public interface ObjReadEnumFreqPair<Obj, E extends Enum<E>> {
        EnumFreqPair<E> read(Obj e);
    }

    public interface ConfirmTag<Obj, E> {
        void confirm(Obj obj, E tag);
    }

    public static <Obj, E extends Enum<E>>
    void computeEnumSimply2(Obj[] roleTagList, ObjReadEnumFreqPair<Obj, E> map, EnumTransformMatrix<E> transformMatrixDictionary, ConfirmTag<Obj, E> processer) {

        // int length = roleTagList.length - 1;
        //List<E> tagList = new LinkedList<E>();
        Obj first = roleTagList[0];
        EnumFreqPair<E> start = map.read(first);

        E pre = start.oneKey();
        E perfect_tag = pre;

        // 第一个是确定的
        processer.confirm(first, pre);

        for (int i = 1; i < roleTagList.length; ++i) {
            double perfect_cost = Double.MAX_VALUE;
            final Obj obj = roleTagList[i];
            final EnumFreqPair<E> item = map.read(obj);
            for (E cur : item.keySet()) {
                double now = transformMatrixDictionary.getTP(pre, cur) - Math.log((item.getFrequency(cur) + 1e-8) / transformMatrixDictionary.getTotalFrequency(cur));

                if (perfect_cost > now) {
                    perfect_cost = now;
                    perfect_tag = cur;
                }
            }
            pre = perfect_tag;

            processer.confirm(obj, pre);
        }
    }
}

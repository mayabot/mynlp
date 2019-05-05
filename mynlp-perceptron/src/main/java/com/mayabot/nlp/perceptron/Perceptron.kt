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
package com.mayabot.nlp.perceptron


import com.mayabot.nlp.hppc.IntArrayList

import java.io.File


typealias FeatureVector = IntArrayList

typealias FeatureVectorSequence = List<FeatureVector>

/**
 * 感知机模型。
 *
 * 这个感知机面向的是向量进行计算的，提供最最底层的实现。
 *
 * FeatureSet中保存了所有的特征，每个特征都有自己的数组下标。
 * 系统中存在一共N个特征。
 *
 * 感知机处理的向量是一个特征向量的长度是N，但是是觉得稀疏的向量，只有
 * 某几位是1，其他的为0，所以使用有限的int[]来保存为1的向量的下标。
 *
 * decode中使用了int[]中的，最后一位是留个转移特征使用的。
 *
 * 所以抽取特征向量的时候需要注意。
 * //// 最后一列留给转移特征
 * result.add(0)
 *
 * @author jimichan
 */
interface Perceptron {

    /**
     * 特征集合
     *
     * @return 特征集合
     */
    fun featureSet(): FeatureSet


    fun makeSureParameter(featureId: Int)

    /**
     * 在线学习版本的update
     * @param data
     */
    fun onlineLearn(data: TrainSample)

    /**
     * 压缩模型大小。删除权重不重要的特征
     * @param ratio 压缩比，0.1表示压缩去掉10%的特征
     * @param threshold 特征最小得分,得分小于这个阈值就删除。
     */
    fun compress(ratio: Double, threshold: Double)

    /**
     * 解码
     *
     * @param featureSequence FeatureSequence 为一个浓缩的特征向量，最后一位是留给转移特征。
     * @param guessLabel 结构保存在这个数组里面去
     */
    fun decode(featureSequence: FeatureVectorSequence, guessLabel: IntArray)

    /**
     * 解码
     *
     * @param featureSequence FeatureSequence 稀疏特征向量的简短表示，最后一位是留给转移特征。
     * @return label对应的ID数组
     */

    fun decode(featureSequence: FeatureVectorSequence): IntArray

    /**
     * 保存感知机模型实例到二进制文件。
     * 模型讲
     * @param dir File 一个空的文件夹
     */
    fun save(dir: File)

}

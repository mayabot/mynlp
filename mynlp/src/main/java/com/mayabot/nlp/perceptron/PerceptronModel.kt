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


import com.mayabot.nlp.MynlpEnv
import com.mayabot.nlp.common.hppc.IntArrayList
import java.io.DataInputStream
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer

/**
 * FeatureVector最后多一位是留个转移特征使用
 */
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
 * 最后一列留给转移特征
 * result.add(0)
 *
 * @author jimichan
 */
interface PerceptronModel {

    /**
     * 返回特征集合[FeatureSet]
     */
    fun featureSet(): FeatureSet

    /**
     * 为在线学习检测[featureId]是否已经存在，如果不存在，那么自动增加一个
     */
    fun makeSureParameter(featureId: Int)

    /**
     * 在线学习版本的update。学习一个[data]的[TrainSample]实例
     */
    fun onlineLearn(data: TrainSample)

    /**
     * 压缩模型
     * 要求FeatureSet的文本key不能为空，在训练阶段的时候使用
     * 压缩模型大小。删除权重不重要的特征。
     * - [ratio]是压缩比，0.1表示压缩去掉10%的特征。
     * - [threshold]特征最小得分,得分小于这个阈值就删除。
     */
    fun compress(ratio: Double, threshold: Double): PerceptronModel

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
    fun decode(featureSequence: FeatureVectorSequence): IntArray {
        val result = IntArray(featureSequence.size)

        if (result.isEmpty()) {
            return result
        }

        decode(featureSequence, result)

        return result
    }

    /**
     * 保存感知机模型实例到二进制文件。
     * 模型讲
     * @param dir File 一个空的文件夹
     */
    fun save(dir: File)

    fun decodeQuickMode(quick: Boolean)

    fun parameterAt(index: Int): Float

    companion object{
        @JvmStatic
        @JvmOverloads
        fun loadFromClasspath(prefix: String, loader: ClassLoader = Thread.currentThread().contextClassLoader): PerceptronModel {
            val parameter = loader.getResourceAsStream("$prefix/parameter.bin")
            val feature = loader.getResourceAsStream("$prefix/feature.dat")
                ?: loader.getResourceAsStream("$prefix/feature.txt")

            check(parameter != null && feature != null)

            val isDat = loader.getResource("$prefix/feature.dat") != null

            return if (isDat) {
                loadWithFeatureBin(parameter, feature)
            } else {
                loadWithFeatureTxt(parameter, feature)
            }

        }

        @JvmStatic
        fun loadFromNlpResource(prefix: String, nlpEnv: MynlpEnv): PerceptronModel {

            val parameter = nlpEnv.loadResource("$prefix/parameter.bin")
            val fd = nlpEnv.tryLoadResource("$prefix/feature.dat")
            val feature = fd ?: nlpEnv.loadResource("$prefix/feature.txt")

            check(parameter != null && feature != null)

            val isDat = fd != null

            return if (isDat) {
                loadWithFeatureBin(parameter.inputStream(), feature.inputStream())
            } else {
                loadWithFeatureTxt(parameter.inputStream(), feature.inputStream())
            }

        }

        @JvmStatic
        fun loadWithFeatureBin(parameterBin: InputStream, featureBin: InputStream): PerceptronModel {
            return load(parameterBin, featureBin, null)
        }

        @JvmStatic
        fun loadWithFeatureTxt(parameterBin: InputStream, featureTxt: InputStream): PerceptronModel {
            return load(parameterBin, null, featureTxt)
        }

        /**
         * 正常训练完成的模型，是保存在文件夹。
         * 里面包含featureBin和featureTxt
         * 我们发布的模型bin和txt是二选一的
         * 使用这种加载方式，是可以对模型进行压缩的。
         */
        @JvmStatic
        fun load(dir: File): PerceptronModel {
            fun loadIfExit(name: String): File? {
                val f = File(dir, name)
                return if (f.exists()) f else null
            }

            fun load(parameterFile: File, featureBin: File?, featureText: File?): PerceptronModel {
                return load(
                    parameterFile.inputStream().buffered(),
                    featureBin?.inputStream()?.buffered(),
                    featureText?.inputStream()?.buffered()
                )
            }

            return load(
                File(dir, "parameter.bin"),
                loadIfExit("feature.dat"),
                loadIfExit("feature.txt")
            )
        }

        private fun load(parameterBin: InputStream, featureBin: InputStream?,
                         featureText: InputStream?): PerceptronModel {

            check(!(featureBin == null && featureText == null)) { "featureBin不可以同时为空" }

            var labelCount = 0
            var parameter = FloatArray(0)

            parameterBin.use { x ->

                val input = DataInputStream(x)
                labelCount = input.readInt()

                val pSize = input.readInt()

                parameter = FloatArray(pSize)

                val buffer = ByteArray(1024 * 1024 * 4)
                val wrap = ByteBuffer.wrap(buffer)

                var point = 0

                while (true) {
                    val n = input.read(buffer)
                    if (n == -1) {
                        break
                    }
                    if (n % 4 != 0) {
                        throw java.lang.RuntimeException("Error Size $n")
                    }

                    wrap.flip()
                    wrap.limit(n)

                    for (i in 0 until n / 4) {
                        parameter[point++] = wrap.float
                    }

                }
            }

            val fs = if (featureBin != null) {
                if (featureText != null) {
                    FeatureSet.read(featureBin, featureText)
                } else {
                    FeatureSet.read(featureBin)
                }
            } else {
                if (featureText != null) {
                    FeatureSet.readFromTextButNotSave(featureText)
                } else {
                    throw RuntimeException("featureText featureBin 不可以同时为空")
                }
            }

            return PerceptronModelImpl(fs, labelCount, parameter)
        }
    }
}

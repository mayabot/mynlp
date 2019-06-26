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

import com.mayabot.nlp.common.FastStringBuilder
import com.mayabot.nlp.hppc.IntArrayList
import com.mayabot.nlp.utils.CharNormUtils
import java.io.File

/**
 * 一个被训练的原始文本。解析为(X,Label)序列。
 *
 * 比如 分词语料， "世界 你好" => 世/B 界/E 你/B 好/E
 */
abstract class PerceptronDefinition<E, Label, InputSequence> {

    /**
     * 返回标签列表
     */
    abstract val labels: List<Label>

    /**
     * [label]对应的在[labels]里面的下标
     */
    abstract fun labelIndex(label: Label): Int

    /**
     * 对标注的文本进行解析
     */
    abstract fun annotateText(text: String): List<Pair<E, Label>>

    /**
     * 把列表转换为InputSequence实际的容器对象，有些是原生char数组，有些就是list
     */
    abstract fun inputList2InputSeq(list: List<E>): InputSequence

    /**
     * 特征Buffer，预订最大特征字符串的长度
     */
    abstract fun buffer(): FastStringBuilder

    /**
     * 特征工程函数
     *
     * 每次[buffer]在使用之前需要调用[buffer].clear()。
     * 每次填充完buffer后，需要调用[emit]进行发射。
     *
     */
    abstract fun featureFunction(sentence: InputSequence,
                                 size: Int,
                                 position: Int,
                                 buffer: FastStringBuilder,
                                 emit: () -> Unit)

    /**
     * 加载训练语料的时候可以预处理InputSequence
     *
     * 子类可以覆盖实现.
     */
    open fun preProcessInputSequence(sentence: InputSequence) {
        if (sentence is CharArray) {
            CharNormUtils.convert(sentence)
        }
    }

    fun learn(model: Perceptron, sample: String) {
        val id = makeSureFeatureSet(sample, model.featureSet())
        model.makeSureParameter(id)
        val x = sampleText2TrainSample(sample, model.featureSet())
        model.onlineLearn(x)
    }

    private fun oriInputFromSample(sample: String): InputSequence {
        return inputList2InputSeq(annotateText(sample).map { it.first })
    }

    private fun makeSureFeatureSet(sample: String, featureSet: FeatureSet): Int {
        var max = 0
        val input = oriInputFromSample(sample)
        preProcessInputSequence(input)
        if (input is CharArray) {
            val size = input.size
            val buffer = buffer()
            for (i in 0 until input.size) {
                featureFunction(input, size, i, buffer) {
                    val fid = featureSet.featureId(buffer)
                    if (fid < 0) {
                        val id = featureSet.newExtId(buffer.toString())
                        if (id > max) {
                            max = id
                        }
                    }
                }
            }
        } else if (input is List<*>) {
            val size = input.size
            val buffer = buffer()
            for (i in 0 until input.size) {
                featureFunction(input, size, i, buffer) {
                    val fid = featureSet.featureId(buffer)
                    if (fid < 0) {
                        val id = featureSet.newExtId(buffer.toString())
                        if (id > max) {
                            max = id
                        }
                    }
                }
            }
        }
        return max

    }

    fun decodeModel(model: Perceptron, sentence: InputSequence): List<Label> {
        preProcessInputSequence(sentence)
        val vectorSequence = inputSeq2VectorSequence(sentence, model.featureSet())
        val pre = model.decode(vectorSequence)
        return pre.map { labels[it] }
    }

    fun files(file: File) = if (file.isFile) listOf(file) else file.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.toList()

    /**
     * 默认简单的评估实现
     */
    open fun evaluate(id: Int, model: Perceptron, sample: List<String>): EvaluateResult {
        val testSamples = sample.map { sampleText2TrainSample(it, model.featureSet()) }
        return simpleEvaluate(model, testSamples)
    }

    /**
     * 训练一个感知机模型
     */
    fun train(trainFile: File,
              evaluateFile: File?,
              iter: Int,
              threadNum: Int,
              quickDecode: Boolean = false,
              evaluateBlock: (id: Int, model: Perceptron, sample: List<String>) -> EvaluateResult = ::evaluate)
            : Perceptron {
        val trainFiles = files(trainFile)

        //构建FeatureSet
        println("开始构建FeatureSet")
        val t1 = System.currentTimeMillis()
        val featureSet = buildFeatureSet(trainFiles.asSequence().map { it.readLines() })
        println("构建FeatureSet耗时 ${System.currentTimeMillis() - t1} MS, 包含${featureSet.size()}个特征")

        //计算有多少行
        var lineCountLocal = 0
        trainFiles.forEach { file ->
            file.forEachLine { line ->
                if (line.isNotBlank()) {
                    lineCountLocal++
                }
            }
        }
        val lineCount = lineCountLocal

        val sampleList = ArrayList<TrainSample>(lineCount)
        // 加载样例
        val t2 = System.currentTimeMillis()
        trainFiles.forEach { file ->
            file.forEachLine { line ->
                sampleList += sampleText2TrainSample(line, featureSet)
                if (sampleList.size % 2000 == 0) {
                    println("Load ${sampleList.size}/$lineCount")
                }
            }
        }
        println("加载TrainSample耗时 ${System.currentTimeMillis() - t2} MS, 包含${sampleList.size}个样例")

        val evaluateSampleList = if (evaluateFile == null) emptyList() else files(evaluateFile).flatMap { it.readLines() }

        println("Start train ...")

        val trainer = PerceptronTrainer(
                featureSet,
                labels.size,
                sampleList,
                { id, it ->
                    if (evaluateSampleList.isNotEmpty()) {
                        val r = evaluateBlock(id, it, evaluateSampleList)
                        println("Evaluate Iter $id $r")
                    }
                }, iter, quickDecode)

        return trainer.train(threadNum)
    }

    private fun buildFeatureSet(sampleBlock: Sequence<List<String>>): FeatureSet {
        val builder = DATFeatureSetBuilder(labels.size)

        sampleBlock.forEach { samples ->
            samples.forEach { sample ->
                val seq = oriInputFromSample(sample)
                preProcessInputSequence(seq)
                inputSeq2FeatureSet(seq, builder)
            }
        }

        return builder.build()
    }

    private fun sampleText2TrainSample(text: String, featureSet: FeatureSet): TrainSample {
        val list = annotateText(text)
        val inputList = inputList2InputSeq(list.map { it.first })
        preProcessInputSequence(inputList)

        val labelList = list.map { labelIndex(it.second) }.toIntArray()

        return TrainSample(
                inputSeq2VectorSequence(inputList, featureSet),
                labelList
        )

    }

    private fun inputSeq2VectorSequence(input: InputSequence, featureSet: FeatureSet): FeatureVectorSequence {
        if (input is CharArray) {
            val buffer = buffer()
            val size = input.size
            val out = ArrayList<FeatureVector>(input.size)

            for (i in 0 until input.size) {
                val vector = IntArrayList()
                featureFunction(input, size, i, buffer) {
                    val id = featureSet.featureId(buffer)
                    if (id >= 0) {
                        vector.add(featureSet.featureId(buffer))
                    }
                }
                vector.add(0)
                out += vector
            }
            return out
        } else if (input is List<*>) {
            val buffer = buffer()
            val size = input.size
            val out = ArrayList<FeatureVector>(input.size)

            for (i in 0 until input.size) {
                val vector = IntArrayList()
                featureFunction(input, size, i, buffer) {
                    val id = featureSet.featureId(buffer)
                    if (id >= 0) {
                        vector.add(featureSet.featureId(buffer))
                    }
                }
                vector.add(0)
                out += vector
            }
            return out
        }
        throw RuntimeException()
    }

    private fun inputSeq2FeatureSet(input: InputSequence, builder: DATFeatureSetBuilder) {
        if (input is CharArray) {
            val size = input.size
            val buffer = buffer()
            for (i in 0 until input.size) {
                featureFunction(input, size, i, buffer) {
                    builder.put(buffer.toString())
                }
            }
        } else if (input is List<*>) {
            val size = input.size
            val buffer = buffer()
            for (i in 0 until input.size) {
                featureFunction(input, size, i, buffer) {
                    builder.put(buffer.toString())
                }
            }
        }
    }
}


/**
 * 评估结果
 */
data class EvaluateResult(
        /**
         * 正确率
         */
        val precision: Float,
        /**
         * 召回率
         */
        val recall: Float
) {

    constructor(goldTotal: Int, predTotal: Int, correct: Int) : this(
            (correct * 100.0 / predTotal).toFloat(),
            (correct * 100.0 / goldTotal).toFloat()
    )

    /**
     * F1综合指标
     */
    val f1: Float
        get() = (2.0 * precision * recall / (precision + recall)).toFloat()

    override fun toString(): String {
        return "正确率(P) %.2f , 召回率(R) %.2f , F1 %.2f".format(precision, recall, f1)
    }
}
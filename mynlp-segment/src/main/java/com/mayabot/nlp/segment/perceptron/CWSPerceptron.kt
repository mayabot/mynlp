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
package com.mayabot.nlp.segment.perceptron

import com.carrotsearch.hppc.IntArrayList
import com.google.common.base.Splitter
import com.mayabot.nlp.common.FastStringBuilder
import com.mayabot.nlp.perceptron.*
import com.mayabot.nlp.segment.perceptron.CWSPerceptron.Companion.B
import com.mayabot.nlp.segment.perceptron.CWSPerceptron.Companion.E
import com.mayabot.nlp.segment.perceptron.CWSPerceptron.Companion.M
import com.mayabot.nlp.segment.perceptron.CWSPerceptron.Companion.S
import com.mayabot.nlp.segment.perceptron.CWSPerceptron.Companion.tagList
import com.mayabot.nlp.segment.perceptron.CWSPerceptronSample.forOnlineLearn
import com.mayabot.nlp.segment.perceptron.CWSPerceptronSample.loadSamples
import com.mayabot.nlp.segment.perceptron.CWSPerceptronSample.sentenceToSample
import com.mayabot.nlp.utils.CharNormUtils
import java.io.File
import java.io.InputStream
import java.util.function.Consumer
import kotlin.streams.toList

/**
 * 用B M E S进行分词的感知机模型
 * @author jimichan
 */
class CWSPerceptron(val model: Perceptron) {

    /**
     * 保存分词模型
     */
    fun save(dir: File) {
        dir.mkdirs()
        model.save(dir)
    }

    fun compress(ratio: Double, threshold: Double) {
        model.compress(ratio, threshold)
    }

    /**
     * 在线学习一个句子
     * 句子,词用空格分开
     */
    fun learn(learn: String) {
        val sentence = learn.replace(" ", "﹍")
        val id = forOnlineLearn(sentence, model.featureSet())
        model.makeSureParameter(id)
        val sample = sentenceToSample(sentence, model.featureSet())
        model.updateForOnlineLearn(sample)
    }

    fun decodeToWordList(sentence: String): List<String> {
        val result = ArrayList<String>()
        val decode = decode(sentence.toCharArray(), true)
        var p = 0

        for (i in 0 until decode.size) {
            val f = decode[i]
            if (f == S || f == E) {
                result += sentence.substring(p, i + 1)
                p = i + 1
            }
        }

        if (p < sentence.length) {
            result += sentence.substring(p, sentence.length)
        }
        return result
    }

    fun decode(sentence: CharArray, convert: Boolean): IntArray {

        if (convert) {
            CharNormUtils.convert(sentence)
        }

        val buffer = FastStringBuilder(4)

        val featureList = ArrayList<IntArrayList>(sentence.size)
        for (i in 0 until sentence.size) {
            featureList += CWSPerceptronFeature.extractFeatureVector(sentence, sentence.size, i, model.featureSet(), buffer)
        }
        return model.decode(featureList)
    }

    companion object {

        const val B = 0
        const val M = 1
        const val E = 2
        const val S = 3

        @JvmStatic
        val tagList = listOf("B", "M", "E", "S")


        @JvmStatic
        fun load(parameterBin: InputStream, featureBin: InputStream): CWSPerceptron {
            val model = PerceptronModel.load(parameterBin, featureBin, true)
            return CWSPerceptron(model)
        }

        @JvmStatic
        fun load(dir: File): CWSPerceptron {
            return load(File(dir, "parameter.bin").inputStream().buffered(),
                    File(dir, "feature.dat").inputStream().buffered())
        }

    }
}

object CWSPerceptronFeature {

    private const val CHAR_NULL = '\u0000'


    fun extractFeatureVector(sentence: CharArray, size: Int, position: Int, features: FeatureSet, buffer: FastStringBuilder): IntArrayList {
        val vector = IntArrayList(8)

        val lastIndex = size - position - 1

        val pre2Char = if (position > 1) sentence[position - 2] else CHAR_NULL
        val preChar = if (position > 0) sentence[position - 1] else CHAR_NULL
        val curChar = sentence[position]
        val nextChar = if (lastIndex > 0) sentence[position + 1] else CHAR_NULL
        val next2Char = if (lastIndex > 1) sentence[position + 2] else CHAR_NULL

        buffer.set2(curChar, '2')
        addFeature(features, vector, buffer)

        if (position > 0) {
            buffer.set2(preChar, '1')
            addFeature(features, vector, buffer)

            buffer.set4(preChar, '/', curChar, '5')
            addFeature(features, vector, buffer)

            if (position > 1) {
                buffer.set4(pre2Char, '/', preChar, '4')
                addFeature(features, vector, buffer)
            }
        }

        if (lastIndex > 0) {
            buffer.set2(nextChar, '3')
            addFeature(features, vector, buffer)

            buffer.set4(curChar, '/', nextChar, '6')
            addFeature(features, vector, buffer)

            if (lastIndex > 1) {
                buffer.set4(nextChar, '/', next2Char, '7')
                addFeature(features, vector, buffer)
            }
        }


        vector.add(0)
        return vector
    }

    private fun addFeature(features: FeatureSet, vector: IntArrayList, stringBuilder: FastStringBuilder) {

        val id = features.featureId(stringBuilder)

        stringBuilder.clear()
        if (id >= 0) {
            vector.add(id)
        }
    }

    /**
     * 在制作特征大集合的时候使用
     */
    fun extractFeature(sentence: CharArray, size: Int, position: Int, callBack: Consumer<String>) {
        val lastIndex = size - position - 1

        val pre2Char = if (position > 1) sentence[position - 2] else CHAR_NULL
        val preChar = if (position > 0) sentence[position - 1] else CHAR_NULL
        val curChar = sentence[position]
        val nextChar = if (lastIndex > 0) sentence[position + 1] else CHAR_NULL
        val next2Char = if (lastIndex > 1) sentence[position + 2] else CHAR_NULL

        if (position > 0) callBack.accept(preChar + "1")
        callBack.accept(curChar + "2")
        if (lastIndex > 0) callBack.accept(nextChar + "3")

        if (position > 1) callBack.accept(pre2Char + "/" + preChar + "4")
        if (position > 0) callBack.accept(preChar + "/" + curChar + "5")

        if (lastIndex > 0) callBack.accept(curChar + "/" + nextChar + "6")
        if (lastIndex > 1) callBack.accept(nextChar + "/" + next2Char + "7")
    }
}

object CWSPerceptronSample {

    fun forOnlineLearn(ineInput: String, featureSet: FeatureSet): Int {
        val sentence = ineInput.replace("﹍", "").toCharArray()

        CharNormUtils.convert(sentence)

        var max: Int = 0
        for (i in 0 until sentence.size) {
            CWSPerceptronFeature.extractFeature(sentence, sentence.size, i, Consumer { feature ->
                var fid = featureSet.featureId(feature)
                if (fid < 0) {
                    val id = featureSet.newExtId(feature)
                    if (id > max) {
                        max = id
                    }
                }
            })
        }

        return max

    }


    fun loadSamples(allFiles: List<File>, featureSet: FeatureSet): List<TrainSample> {
        //统计有多少样本
        var sampleSize = 0

        allFiles.forEach { file ->
            file.useLines { lines ->
                lines.forEach { if (it.isNotBlank()) sampleSize++ }
            }
        }

        println("Sample Size $sampleSize")

        //预先分配好空间
        val sampleList = ArrayList<TrainSample>(sampleSize)

        // 解析语料库为数字化TrainSample
        allFiles.forEach { file ->
            file.useLines { lines ->
                sampleList.addAll(lines.filter { it.isNotBlank() }.toList().parallelStream().map {
                    CWSPerceptronSample.sentenceToSample(it, featureSet)
                }.toList())
            }
        }

        return sampleList
    }


    /**
     * 把一个句子，变化为TrainSample
     * 一个用空格分隔的句子.
     */
    fun sentenceToSample(lineInput: String, featureSet: FeatureSet): TrainSample {

        val buffer = FastStringBuilder(4)

        val line = CharNormUtils.convert(lineInput)

        val juzi = CharArray(line.length)
        val split = BooleanArray(line.length)
        var len = 0
        line.forEach { c ->
            if (c != '﹍') {
                juzi[len++] = c
            } else {
                split[len - 1] = true
            }
        }
        split[len - 1] = true

        val list = mutableListOf<IntArrayList>()
        val tagList = IntArray(len)

        var from = 0
        for (i in 0 until len) {
            val vec = CWSPerceptronFeature.extractFeatureVector(juzi, len, i, featureSet, buffer)
            list.add(vec)

            if (split[i]) {
                val wordLen = i - from + 1
                if (wordLen == 1) {
                    tagList[i] = S //S
                } else {
                    tagList[from] = B //B

                    if (wordLen >= 3) {
                        for (x in from + 1 until i) {
                            tagList[x] = M//M
                        }
                    }

                    tagList[i] = E//E
                }
                from = i + 1
            }
        }
        return TrainSample(list, tagList)
    }
}

/**
 * 分词感知机的训练
 */
class CWSPerceptronTrainer {

    lateinit var featureSet: FeatureSet


    fun train(trainFileDir: File, evaluateFile: File, maxIter: Int, threadNumber: Int): CWSPerceptron {

        val allFiles = if (trainFileDir.isFile) listOf(trainFileDir) else trainFileDir.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.toList()

//        val workDir = File("data.work/pcws")
//        val featureSetFile = File(workDir, "feature.dat")
//
//        if (featureSetFile.exists()) {
//            featureSet = FeatureSet.read(
//                    featureSetFile.inputStream().buffered(),
//                    File(workDir, "feature.txt").inputStream().buffered()
//            )
//        } else {
        prepareFeatureSet(allFiles)
//            featureSet.save(File(workDir, "feature.dat"), File(workDir, "feature.txt"))
//        }

        println("FeatureSet Size ${featureSet.size()}")

        val sampleList = loadSamples(allFiles, featureSet)

        //验证集合
        val evaluateSample = (if (evaluateFile.isFile) listOf(evaluateFile) else evaluateFile.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.toList())
                .flatMap { it.readLines() }.map { CharNormUtils.convert(it) }

        println("Start train ...")

        val trainer = PerceptronTrainer(featureSet, tagList.size, sampleList,
                EvaluateRunner { k, it ->
                    CWSEvaluate.evaluate(evaluateSample, CWSPerceptron(it))
                }, maxIter, false)

        return CWSPerceptron(trainer.train(threadNumber))

    }

    /**
     * 制作FeatureSet。
     * 扫描所有语料库，为每一个特征进行编码
     */
    fun prepareFeatureSet(files: List<File>) {
        println("开始构建FeatureSet")
        val t1 = System.currentTimeMillis()
        val builder = DATFeatureSetBuilder(4)
        val fit = Consumer<String> { f ->
            builder.put(f)
        }
        files.forEach { dictFile ->
            println(dictFile.absolutePath)
            val lines = dictFile.readLines()

            lines.forEach { line ->
                val line2 = CharNormUtils.convert(line).filter { it != '﹍' }.toCharArray()
                val len = line2.size
                for (i in 0 until len) {
                    CWSPerceptronFeature.extractFeature(line2, len, i, fit)
                }
            }
        }

        featureSet = builder.build()

        println("FeatureSet构建完成,用时${System.currentTimeMillis() - t1} ms")
    }

}

object CWSEvaluate {

    /**
    正确率 = 正确识别的个体总数 / 识别出的个体总数
    召回率 = 正确识别的个体总数 / 测试集中存在的个体总数
    F值 = 正确率 * 召回率 * 2 / (正确率 + 召回率)
     */
    fun evaluate(evaluateSample: List<String>, segmenter: CWSPerceptron): DoubleArray {
        // int goldTotal = 0, predTotal = 0, correct = 0;
        var goldTotal = 0
        var predTotal = 0
        var correct = 0

        val splitter = Splitter.on("﹍").omitEmptyStrings()

        System.out.print("Evaluating 0%")

        val t1 = System.currentTimeMillis()

        var count = 0
        for (line in evaluateSample) {
            val wordArray = splitter.splitToList(CharNormUtils.convert(line))
            goldTotal += wordArray.size

            val text = wordArray.joinToString(separator = "")
            val predArray = segmenter.decodeToWordList(text)
            predTotal += predArray.size

            var goldIndex = 0
            var predIndex = 0
            var goldLen = 0
            var predLen = 0

            while (goldIndex < wordArray.size && predIndex < predArray.size) {
                if (goldLen == predLen) {
                    if (wordArray[goldIndex] == predArray[predIndex]) {
                        correct++
                        goldLen += wordArray[goldIndex].length
                        predLen += wordArray[goldIndex].length
                        goldIndex++
                        predIndex++
                    } else {
                        goldLen += wordArray[goldIndex].length
                        predLen += predArray[predIndex].length
                        goldIndex++
                        predIndex++
                    }
                } else if (goldLen < predLen) {
                    goldLen += wordArray[goldIndex].length
                    goldIndex++
                } else {
                    predLen += predArray[predIndex].length
                    predIndex++
                }
            }

            count++

            if (count % 2000 == 0) {
                System.out.print("\rEvaluating ${"%.2f".format(count * 100.0 / evaluateSample.size)}%")
            }

        }

        fun prf(goldTotal: Int, predTotal: Int, correct: Int): DoubleArray {
            val precision = correct * 100.0 / predTotal
            val recall = correct * 100.0 / goldTotal
            val performance = DoubleArray(3)
            performance[0] = precision
            performance[1] = recall
            performance[2] = 2.0 * precision * recall / (precision + recall)
            return performance
        }

        val result = prf(goldTotal, predTotal, correct)

        System.out.print("\r")

        val t2 = System.currentTimeMillis()

        System.out.println("正确率(P) %.2f , 召回率(R) %.2f , F1 %.2f".format(result[0], result[1], result[2]))
        println("Evaluate use time ${t2 - t1} ms")

        return result
    }
}

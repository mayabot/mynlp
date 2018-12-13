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
import com.mayabot.nlp.perceptron.*
import com.mayabot.nlp.segment.perceptron.NRPerceptron.Companion.O
import com.mayabot.nlp.segment.perceptron.NRPerceptron.Companion.tagList
import com.mayabot.nlp.segment.perceptron.NRPerceptronFeature.extractFeature
import com.mayabot.nlp.segment.perceptron.NRPerceptronFeature.extractFeatureVector
import com.mayabot.nlp.segment.perceptron.NRPerceptronSample.sentenceToSample
import com.mayabot.nlp.utils.CharNormUtils
import java.io.File
import java.io.InputStream
import java.util.function.Consumer

/**
 * 用B M E O
 */
class NRPerceptron(val model: Perceptron) {

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

//    /**
//     * 在线学习一个句子
//     * 句子 词用空格分开
//     */
//    fun learn(sentence: String) {
//
//        val id = forOnlineLearn(sentence, model.featureSet())
//        model.makeSureParameter(id)
//        val sample = sentenceToSample(sentence, model.featureSet())
//        model.update(sample)
//    }

    fun decodeToWordList(sentence: String): List<String> {
        val result = ArrayList<String>()
        val decode = decode(sentence.toCharArray(), true)
        var p = -1

        for (i in 0 until decode.size) {
            val f = decode[i]
            when (f) {
                B -> p = i
                E -> if (p != -1) {
                    result += sentence.substring(p, i + 1)
                }
                O -> p = -1
            }
        }

        return result
    }

    fun decode(sentence: CharArray, convert: Boolean): IntArray {

        if (convert) {
            CharNormUtils.convert(sentence)
        }

        val buffer = StringBuilder()

        val featureList = ArrayList<IntArrayList>(sentence.size)
        for (i in 0 until sentence.size) {
            featureList += extractFeatureVector(sentence, sentence.size, i, model.featureSet(), buffer)
        }
        return model.decode(featureList)
    }

    companion object {

        const val B = 0
        const val M = 1
        const val E = 2
        const val O = 3

        @JvmStatic
        val tagList = listOf("B", "M", "E", "O")


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

object NRPerceptronFeature {

    private const val CHAR_BEGIN = '\u0001'

    private const val CHAR_END = '\u0002'

    fun extractFeatureVector(sentence: CharArray, size: Int, position: Int, features: FeatureSet, buffer: StringBuilder): IntArrayList {
        val vector = IntArrayList(8)

        buffer.clear()


        val pre2Char = if (position >= 2) sentence[position - 2] else CHAR_BEGIN
        val preChar = if (position >= 1) sentence[position - 1] else CHAR_BEGIN
        val curChar = sentence[position]
        val nextChar = if (position < size - 1) sentence[position + 1] else CHAR_END
        val next2Char = if (position < size - 2) sentence[position + 2] else CHAR_END

        addFeature(features, vector, buffer, preChar, '1')
        addFeature(features, vector, buffer, curChar, '2')
        addFeature(features, vector, buffer, nextChar, '3')

        addFeature(features, vector, buffer, pre2Char, '/', preChar, '4')
        addFeature(features, vector, buffer, preChar, '/', curChar, '5')
        addFeature(features, vector, buffer, curChar, '/', nextChar, '6')
        addFeature(features, vector, buffer, nextChar, '/', next2Char, '7')

        vector.add(0)
        return vector
    }

    private fun addFeature(features: FeatureSet, vector: IntArrayList, stringBuilder: StringBuilder, vararg parts: Any) {
        for (x in parts) {
            stringBuilder.append(x)
        }
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
        val pre2Char = if (position >= 2) sentence[position - 2] else CHAR_BEGIN
        val preChar = if (position >= 1) sentence[position - 1] else CHAR_BEGIN
        val curChar = sentence[position]
        val nextChar = if (position < size - 1) sentence[position + 1] else CHAR_END
        val next2Char = if (position < size - 2) sentence[position + 2] else CHAR_END

        callBack.accept(preChar + "1")
        callBack.accept(curChar + "2")
        callBack.accept(nextChar + "3")

        callBack.accept(pre2Char + "/" + preChar + "4")
        callBack.accept(preChar + "/" + curChar + "5")
        callBack.accept(curChar + "/" + nextChar + "6")
        callBack.accept(nextChar + "/" + next2Char + "7")
    }
}

object NRPerceptronSample {

    fun forOnlineLearn(ineInput: String, featureSet: FeatureSet): Int {
        val sentence = ineInput.replace(" ", "").toCharArray()

        CharNormUtils.convert(sentence)

        var max: Int = 0
        for (i in 0 until sentence.size) {
            NRPerceptronFeature.extractFeature(sentence, sentence.size, i, Consumer { feature ->
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

    /**
     * 把一个句子，变化为TrainSample
     * 一个用空格分隔的句子.
     * 国家/n 领导人/n 江泽民/nr ,/w
     */
    fun sentenceToSample(line: List<PkuWord>, featureSet: FeatureSet): TrainSample {
        val wordsBuffer = java.lang.StringBuilder()

        val tagList = IntArrayList()

        for (x in line) {
            if (x.hasSub()) {
                for (y in x.subWord) {
                    for (c in y.word) {
                        wordsBuffer.append(c)
                        tagList.add(O)
                    }
                }
            } else {
                if (x.pos == "nr") {
                    val name = x.word
                    wordsBuffer.append(name.first())
                    tagList.add(NRPerceptron.B)

                    for (i in 1 until name.length - 1) {
                        wordsBuffer.append(name[i])
                        tagList.add(NRPerceptron.M)
                    }

                    wordsBuffer.append(name.last())
                    tagList.add(NRPerceptron.E)

                } else {
                    for (c in x.word) {
                        wordsBuffer.append(c)
                        tagList.add(O)
                    }
                }
            }
        }

        val poss = tagList.toArray()!!

        val featureMatrix = ArrayList<IntArrayList>(wordsBuffer.length)
        val sentence = wordsBuffer.toString().toCharArray()
        val buffer = java.lang.StringBuilder()
        for (i in 0 until wordsBuffer.length) {

            featureMatrix += NRPerceptronFeature.extractFeatureVector(sentence, sentence.size, i, featureSet, buffer)
        }


        return TrainSample(featureMatrix, poss)
    }

}

/**
 * 人名感知机的训练
 */
class NRPerceptronTrainer {

    lateinit var featureSet: FeatureSet


    fun train(trainFileDir: File, evaluateFile: File, maxIter: Int, threadNumber: Int): CWSPerceptron {

        val allFiles = if (trainFileDir.isFile) listOf(trainFileDir) else trainFileDir.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.toList()


        prepareFeatureSet(allFiles)


        println("FeatureSet Size ${featureSet.size()}")

        val sampleList = loadSamples(allFiles)

//        //验证集合
//        val evaluateSample = (if (evaluateFile.isFile) listOf(evaluateFile) else evaluateFile.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.toList())
//                .flatMap { it.readLines() }.map { CharNormUtils.convert(it) }

        println("Start train ...")

        val trainer = PerceptronTrainer(featureSet, tagList.size, sampleList,
                EvaluateRunner { it ->
                    //CWSEvaluate.evaluate(evaluateSample, CWSPerceptron(it))
                }, maxIter, false)

        return CWSPerceptron(trainer.train(threadNumber))

    }

    private fun loadSamples(allFiles: List<File>): List<TrainSample> {
        //统计有多少样本
        var sampleSize = 0

        allFiles.forEach { file ->
            file.useLines { it.forEach { if (it.isNotBlank()) sampleSize++ } }
        }

        println("Sample Size $sampleSize")

        //预先分配好空间
        val sampleList = ArrayList<TrainSample>(sampleSize / 5)


        // 解析语料库为数字化TrainSample
        allFiles.forEach { file ->
            file.useLines { lines ->
                lines.forEach { line ->
                    if (line.isNotBlank()) {
                        val words = line.parseToWords()
                        var hasNR = false
                        words.forEach {
                            if (it.hasSub()) {
                                it.subWord.forEach { w -> w.word = CharNormUtils.convert(w.word) }
                            } else {
                                it.word = CharNormUtils.convert(it.word)
                                if ("nr" == it.pos) {
                                    hasNR = true
                                }
                            }
                        }
                        if (hasNR) {
                            sampleList += sentenceToSample(words, featureSet)
                        }


                    }
                }
            }
        }

        return sampleList
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
                val words = line.parseToWords()
                var hasNR = false
                words.forEach {
                    if (it.hasSub()) {
                        it.subWord.forEach { w -> w.word = CharNormUtils.convert(w.word) }
                    } else {
                        it.word = CharNormUtils.convert(it.word)
                        if ("nr" == it.pos) {
                            hasNR = true
                        }
                    }
                }

                if (hasNR) {
                    val sentence = words.map {
                        if (it.hasSub()) it.subWord.map { it.word }.joinToString(separator = "")
                        else it.word
                    }.joinToString(separator = "")

                    val ss = sentence.toCharArray()
                    val len = sentence.length
                    for (i in 0 until len) {
                        extractFeature(ss, len, i, fit)
                    }
                }


            }
        }

//        val f = File("data/pcws/features.txt").bufferedWriter()
//
//        builder.keys.forEach { key ->
//            f.write("$key\n")
//        }
//
//        f.close()

        featureSet = builder.build()

        println("FeatureSet构建完成,用时${System.currentTimeMillis() - t1} ms")
    }

}


//
//object NREvaluate {
//
//    /**
//    正确率 = 正确识别的个体总数 / 识别出的个体总数
//    召回率 = 正确识别的个体总数 / 测试集中存在的个体总数
//    F值 = 正确率 * 召回率 * 2 / (正确率 + 召回率)
//     */
//    fun evaluate(evaluateSample: List<String>, segmenter: CWSPerceptron): DoubleArray {
//        // int goldTotal = 0, predTotal = 0, correct = 0;
//        var goldTotal = 0
//        var predTotal = 0
//        var correct = 0
//
//        val splitter = Splitter.on(" ").omitEmptyStrings().trimResults()
//
//        System.out.print("Evaluating 0%")
//
//        val t1 = System.currentTimeMillis()
//
//        var count = 0
//        for (line in evaluateSample) {
//            val wordArray = splitter.splitToList(CharNormUtils.convert(line))
//            goldTotal += wordArray.size
//
//            val text = wordArray.joinToString(separator = "")
//            val predArray = segmenter.decodeToWordList(text)
//            predTotal += predArray.size
//
//            var goldIndex = 0
//            var predIndex = 0
//            var goldLen = 0
//            var predLen = 0
//
//            while (goldIndex < wordArray.size && predIndex < predArray.size) {
//                if (goldLen == predLen) {
//                    if (wordArray[goldIndex] == predArray[predIndex]) {
//                        correct++
//                        goldLen += wordArray[goldIndex].length
//                        predLen += wordArray[goldIndex].length
//                        goldIndex++
//                        predIndex++
//                    } else {
//                        goldLen += wordArray[goldIndex].length
//                        predLen += predArray[predIndex].length
//                        goldIndex++
//                        predIndex++
//                    }
//                } else if (goldLen < predLen) {
//                    goldLen += wordArray[goldIndex].length
//                    goldIndex++
//                } else {
//                    predLen += predArray[predIndex].length
//                    predIndex++
//                }
//            }
//
//            count++
//
//            if (count % 2000 == 0) {
//                System.out.print("\rEvaluating ${"%.2f".format(count * 100.0 / evaluateSample.size)}%")
//            }
//
//        }
//
//        fun prf(goldTotal: Int, predTotal: Int, correct: Int): DoubleArray {
//            val precision = correct * 100.0 / predTotal
//            val recall = correct * 100.0 / goldTotal
//            val performance = DoubleArray(3)
//            performance[0] = precision
//            performance[1] = recall
//            performance[2] = 2.0 * precision * recall / (precision + recall)
//            return performance
//        }
//
//        val result = prf(goldTotal, predTotal, correct)
//
//        System.out.print("\r")
//
//        val t2 = System.currentTimeMillis()
//
//        System.out.println("正确率(P) %.2f , 召回率(R) %.2f , F1 %.2f".format(result[0], result[1], result[2]))
//        println("Evaluate use time ${t2 - t1} ms")
//
//        return result
//    }
//}

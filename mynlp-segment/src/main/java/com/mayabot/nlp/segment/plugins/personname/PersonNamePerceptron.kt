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
package com.mayabot.nlp.segment.plugins.personname

import com.mayabot.nlp.common.FastStringBuilder
import com.mayabot.nlp.hppc.IntArrayList
import com.mayabot.nlp.perceptron.*
import com.mayabot.nlp.segment.common.allFiles
import com.mayabot.nlp.segment.common.parseToFlatWords
import com.mayabot.nlp.segment.plugins.personname.NRPerceptronFeature.extractFeature
import com.mayabot.nlp.segment.plugins.personname.NRPerceptronFeature.extractFeatureVector
import com.mayabot.nlp.segment.plugins.personname.NRPerceptronSample.forOnlineLearn
import com.mayabot.nlp.segment.plugins.personname.NRPerceptronSample.sample2Juzi
import com.mayabot.nlp.segment.plugins.personname.NRPerceptronSample.sentenceToSample
import com.mayabot.nlp.segment.plugins.personname.PersonNamePerceptron.Companion.tagList
import com.mayabot.nlp.utils.CharNormUtils
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList


data class PersonName(val name: String, val offset: Int)

/**
 * 感知机人名识别
 *
 */
class PersonNamePerceptron(val model: Perceptron) {

    private val featureSet = model.featureSet()!!

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
     * 在线学习一个人名样例。
     *
     * 例子： "与 令计划 妻子"
     *
     * 除了名字其他都是单字，用空格分开。
     *
     */
    fun learn(pre: String?, name: String, last: String?) {

        fun String.bindTag(tag: String): List<String> {
            return this.toCharArray().map { "$it$tag" }.toList()
        }

        fun String.nrBindTag(): List<String> {
            val list = ArrayList<String>()
            list.add(this.first() + "B")
            if (this.length > 2) {
                for (j in 1 until length - 1) {
                    list.add(this[j] + "M")
                }
            }
            list.add(this.last() + "E")
            return list
        }

        val list = ArrayList<String>()

        if (pre != null) {
            list.addAll(pre.bindTag("X"))
        }

        list += name.nrBindTag()

        if (last != null) {
            list.addAll(last.bindTag("Y"))
        }


        val sentence = list.joinToString(separator = "﹍")

        val id = forOnlineLearn(sentence, model.featureSet())
        model.makeSureParameter(id)
        val sample = sentenceToSample(sentence, model.featureSet())
        model.updateForOnlineLearn(sample)
    }

    /**
     * 计算返回人名
     */
    fun findPersonName(sentence: CharArray): List<PersonName> {
        val result = ArrayList<PersonName>()
        val decode = decode(sentence, false)

        var p = -1
        for (i in 0 until decode.size) {
            when (decode[i]) {
                B -> p = i
                E -> if (p != -1) {
                    result += PersonName(String(sentence, p, i - p + 1), p)
                    p = -1
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

        val buffer = FastStringBuilder(4)
        val size = sentence.size

        val featureList = ArrayList<IntArrayList>(size)

        for (i in 0 until size) {
            featureList += extractFeatureVector(sentence, size, i, featureSet, buffer)
        }

        return model.decode(featureList)
    }

    companion object {

        const val B = 0
        const val E = 2
        const val O = 3

        @JvmStatic
        val tagList = listOf("B", "M", "E", "O", "X", "Y", "Z")

        @JvmStatic
        fun load(parameterBin: InputStream, featureBin: InputStream): PersonNamePerceptron {
            val model = PerceptronModel.load(parameterBin, featureBin, true)
            return PersonNamePerceptron(model)
        }

        @JvmStatic
        fun load(dir: File): PersonNamePerceptron {
            return load(File(dir, "parameter.bin").inputStream().buffered(),
                    File(dir, "feature.dat").inputStream().buffered())
        }

    }
}


object NRPerceptronSample {

    fun forOnlineLearn(ineInput: String, featureSet: FeatureSet): Int {
        val sentence = sample2Juzi(ineInput).toCharArray()

        CharNormUtils.convert(sentence)

        var max = 0
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
     * 训练的样本，提取全出的句子
     */
    fun sample2Juzi(line: String): String {
        return line.split("﹍").map { it[0] }.joinToString(separator = "")
    }

    fun loadSamples(allFiles: List<File>, featureSet: FeatureSet): List<TrainSample> {
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
                        sampleList += sentenceToSample(line, featureSet)
                    }
                }
            }
        }

        return sampleList
    }


    fun sample2JuziAndTag(text: String): Pair<String, IntArray> {
        val stringBuilder = StringBuilder()
        val labels = IntArrayList()
        text.split("﹍").forEach { item ->
            val zi = item[0]
            val lab = item[1].toString()
            stringBuilder.append(zi)
            labels.add(idOf(lab))
        }
        return stringBuilder.toString() to labels.toArray()!!
    }

    /**
     * 把一个句子，变化为TrainSample
     *
     * 记/XB 者/XE 王/B 黎/E )/XS
     */
    fun sentenceToSample(text: String, featureSet: FeatureSet): TrainSample {

        val buffer = FastStringBuilder(4)

        val (juziString, tagList) = sample2JuziAndTag(text)
        val juzi = juziString.toCharArray()

        val len = juzi.size
        val list = mutableListOf<IntArrayList>()

        for (i in 0 until len) {
            val vec = extractFeatureVector(juzi, len, i, featureSet, buffer)
            list.add(vec)
        }

        return TrainSample(list, tagList)
    }


    private fun idOf(tag: String): Int {
        return when (tag) {
            "B" -> 0
            "M" -> 1
            "E" -> 2
            "O" -> 3
            "X" -> 4
            "Y" -> 5
            "Z" -> 6
            else -> 0
        }
    }


}

/**
 * 人名感知机的训练
 */
class NRPerceptronTrainer {

    lateinit var featureSet: FeatureSet


    fun train(trainFileDir: File, evaluateFile: File, maxIter: Int, threadNumber: Int): PersonNamePerceptron {

        val allFiles = if (trainFileDir.isFile) listOf(trainFileDir) else trainFileDir.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.toList()


        prepareFeatureSet(allFiles)


        println("FeatureSet Size ${featureSet.size()}")

        val sampleList = NRPerceptronSample.loadSamples(allFiles, featureSet)

        //验证集合
        val evaluateSample = (if (evaluateFile.isFile) listOf(evaluateFile) else evaluateFile.allFiles()).flatMap { it.readLines() }.map {
            NREvaluate.text2EvaluateSample(it)
        }

        println("Start train ...")

        val trainer = PerceptronTrainer(featureSet, tagList.size, sampleList,
                EvaluateRunner { _, it ->
                    val model = PersonNamePerceptron(it)
                    NREvaluate.evaluate(evaluateSample, model)
                }, maxIter, false)

        return PersonNamePerceptron(trainer.train(threadNumber))

    }


    /**
     * 制作FeatureSet。
     * 扫描所有语料库，为每一个特征进行编码
     */
    private fun prepareFeatureSet(files: List<File>) {
        println("开始构建FeatureSet")
        val t1 = System.currentTimeMillis()
        val builder = DATFeatureSetBuilder(tagList.size)

        val fit = Consumer<String> { f ->
            builder.put(f)
        }

        files.forEach { dictFile ->
            println(dictFile.absolutePath)
            val lines = dictFile.readLines()

            lines.forEach { line ->
                val juzi = sample2Juzi(line)
                val len = juzi.length
                var chars = juzi.toCharArray()
                for (i in 0 until len) {
                    extractFeature(chars, len, i, fit)
                }
            }
        }

        featureSet = builder.build()
        println("FeatureSet构建完成,用时${System.currentTimeMillis() - t1} ms")
    }

}

object NREvaluate {

    fun text2EvaluateSample(text: String): EvaluateSample {
        val juzi = NRPerceptronSample.sample2Juzi(text)

        var offset = 0
        val list = ArrayList<PersonName>()
        var p = -1
        var buffer = StringBuilder()
        for (s in text.split("﹍")) {
            val word = s[0].toString()
            val label = s[1].toString()
            if (label == "B") {
                p = offset
            }
            when (label) {
                "B" -> {
                    p = offset
                    buffer.append(word)
                }
                "M" -> if (buffer.isNotEmpty()) buffer.append(word)
                "E" -> {
                    buffer.append(word)
                    list += PersonName(buffer.toString(), p)
                    p = -1
                    buffer.clear()
                }
                else -> {
                    p = -1
                    buffer.clear()
                }
            }

            offset++
        }
        return EvaluateSample(juzi, list.filter { it.name.length > 1 })
    }


    data class EvaluateSample(val juzi: String, val goldNames: List<PersonName>)

    /**
    正确率 = 正确识别的个体总数 / 识别出的个体总数
    召回率 = 正确识别的个体总数 / 测试集中存在的个体总数
    F值 = 正确率 * 召回率 * 2 / (正确率 + 召回率)
     */
    fun evaluate(evaluateSample: List<EvaluateSample>, segmenter: PersonNamePerceptron): DoubleArray {

        //样本名字总数
        var goldTotal = 0

        // 预测出来多少个名字
        var predTotal = 0

        //正确的数量
        var correct = 0

        val t1 = System.currentTimeMillis()

        for (ex in evaluateSample) {
            val text = ex.juzi
            val goldNames = ex.goldNames
            val predNames = segmenter.findPersonName(text.toCharArray())
            goldTotal += goldNames.size

            predTotal += predNames.size

            for (x in predNames) {
                if (goldNames.contains(x)) {
                    correct++
                } else {
//                    println(text)
//                    println("错误的人名"+x)
                }
            }
//            for (x in goldNames) {
//                if (predNames.contains(x)) {
//                    //correct++
//                }else{
//                    println(text)
//                    println("没事别的人名"+x)
//                }
//            }
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

        val t2 = System.currentTimeMillis()

        System.out.println("正确率(P) %.2f , 召回率(R) %.2f , F1 %.2f\n".format(result[0], result[1], result[2]))
        println("Evaluate use time ${t2 - t1} ms")

        return result
    }
}

object NRPerceptronFeature {


    private const val CHAR_NULL = '\u0000'


    fun extractFeatureVector(sentence: CharArray, size: Int, position: Int, features: FeatureSet, buffer: FastStringBuilder): IntArrayList {
        val vector = IntArrayList(8)

//        buffer.clear()
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

/**
 * PKU格式的语料，转换为简单的格式。
 * 期间可以抛弃一些多余的上下文。
 *
 * 偶然/d 知道/v 法拉第/nr 是/p 电磁/n 感应/v
 * 转换为
 * 记X﹏者X﹏吕B﹏志M﹏星E﹏报Y﹏道Y
 * 这种简单文本的格式
 *
 */
object Pku2NrFormat {

    fun String.bindTag(tag: String): List<String> {
        return this.toCharArray().map { "$it$tag" }.toList()
    }

    fun String.nrBindTag(): List<String> {
        val list = ArrayList<String>()
        list.add(this.first() + "B")
        if (this.length > 2) {
            for (j in 1 until length - 1) {
                list.add(this[j] + "M")
            }
        }
        list.add(this.last() + "E")
        return list
    }

    @JvmStatic
    fun main(args: Array<String>) {
        convert(listOf(File("data.work/corpus/pku"),
                File("data.work/corpus/cncorpus")),
                File("data.work/nercorpus")
        )
    }

    fun convert(from: List<File>, toDir: File) {
        val random = Random(0)
        val random2 = Random(0)

        val num = 20
        val writerList = (0 until num).map { File(toDir, "hr_$it.txt").bufferedWriter() }

        from.flatMap { it.allFiles() }.forEach { file ->
            file.useLines { lines ->

                lines.forEach { line ->

                    val words = line.parseToFlatWords()

                    val hashNR = words.find { it.pos == "nr" } != null

                    if (hashNR) {
                        for (w in words) {
                            w.word = CharNormUtils.convert(w.word)
                        }

                        // 一定的概率把，。、变化为空格。这样应该能让更好的处理空格的问题
                        for (w in words) {
                            if (w.word == "," || w.word == "。") {
                                if (random2.nextFloat() < 0.5) {
                                    w.word = " "
                                }
                            }
                        }

                        val list = ArrayList<String>()


                        val len = words.size
                        val lenIndex = len - 1
                        for (i in 0 until len) {
                            val w = words[i]
                            val next = if (i < lenIndex) words[i + 1] else null
                            val pre = if (i > 0) words[i - 1] else null

                            val word = w.word
                            if (w.pos == "nr" && word.length > 1) {
                                list.addAll(word.nrBindTag())
                            } else {
                                if ((next != null && next.pos == "nr") && (pre != null && pre.pos == "nr")) {
                                    list.addAll(word.bindTag("Z"))
                                } else if ((next != null && next.pos == "nr")) {
                                    list.addAll(word.bindTag("X"))
                                } else if (pre != null && pre.pos == "nr") {
                                    list.addAll(word.bindTag("Y"))
                                } else {
                                    list.addAll(word.bindTag("O"))
                                }
                            }
                        }

                        writerList[random.nextInt(num)].append(list.joinToString(separator = "﹍", postfix = "\n"))
                    }
                }

            }
        }

        writerList.forEach { it.flush();it.close() }

    }
}
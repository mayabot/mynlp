package com.mayabot.nlp.segment.plugins.personname

import com.mayabot.nlp.perceptron.*
import com.mayabot.nlp.segment.common.allFiles
import com.mayabot.nlp.segment.common.parseToFlatWords
import com.mayabot.nlp.segment.plugins.personname.NRPerceptronFeature.extractFeature
import com.mayabot.nlp.segment.plugins.personname.NRPerceptronSample.sample2Juzi
import com.mayabot.nlp.segment.plugins.personname.PersonNamePerceptron.Companion.tagList
import com.mayabot.nlp.utils.CharNormUtils
import java.io.File
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList

/**
 * 人名感知机的训练
 */
class NRPerceptronTrainer {

    lateinit var featureSet: FeatureSet


    fun train(trainFileDir: File, evaluateFile: File, maxIter: Int, threadNumber: Int): PersonNamePerceptron {

        val allFiles = if (trainFileDir.isFile) listOf(trainFileDir) else trainFileDir.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.toList()


        prepareFeatureSet(allFiles)


        println("FeatureSet Size ${featureSet.size()}")

        val sampleList = loadSamples(allFiles, featureSet)

        //验证集合
        val evaluateSample = (if (evaluateFile.isFile) listOf(evaluateFile) else evaluateFile.allFiles()).flatMap { it.readLines() }.map {
            NREvaluate.text2EvaluateSample(it)
        }

        println("Start train ...")

        val trainer = PerceptronTrainer(featureSet, tagList.size, sampleList,
                 { _, it ->
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
                }
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

        val t2 = System.currentTimeMillis()

        System.out.println("正确率(P) %.2f , 召回率(R) %.2f , F1 %.2f\n".format(result[0], result[1], result[2]))
        println("Evaluate use time ${t2 - t1} ms")

        return result
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
                    sampleList += NRPerceptronSample.sentenceToSample(line, featureSet)
                }
            }
        }
    }

    return sampleList
}
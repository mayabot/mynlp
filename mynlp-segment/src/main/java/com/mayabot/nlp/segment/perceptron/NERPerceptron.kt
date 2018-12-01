package com.mayabot.nlp.perceptron.solution.ner

import com.carrotsearch.hppc.IntArrayList
import com.mayabot.nlp.perceptron.*
import com.mayabot.nlp.segment.WordTerm
import com.mayabot.nlp.segment.dictionary.Nature
import com.mayabot.nlp.segment.perceptron.PkuWord
import com.mayabot.nlp.segment.perceptron.allFiles
import com.mayabot.nlp.segment.perceptron.parseToFlatWords
import com.mayabot.nlp.segment.perceptron.parseToWords
import com.mayabot.nlp.utils.CharNormUtils
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList

class NERPerceptron(val model: Perceptron, private val labels: List<String>) {

    private val featureSet = model.featureSet()


    fun decode(sentence: List<WordTerm>): List<String> {
        val featureList = ArrayList<IntArrayList>(sentence.size)
        for (i in 0 until sentence.size) {
            featureList += NERPerceptronFeature.extractFeatureVector(sentence, i, featureSet)
        }

        val result = model.decode(featureList)

        return result.map { labels[it] }
    }

    /**
     * 保存NER感知机模型到File dir
     */
    fun save(dir: File) {
        dir.mkdirs()

        model.save(dir)

        val out = File(dir, "label.txt").bufferedWriter()
        out.use {
            labels.forEach { label ->
                out.write(label + "\n")
            }
        }
    }


    companion object {

        /**
         * 加载NER模型
         */
        @JvmStatic
        fun load(dir: File): NERPerceptron {
            val parameterBin = File(dir, "parameter.bin").inputStream().buffered()
            val featureBin = File(dir, "feature.dat").inputStream().buffered()
            val labelText = File(dir, "label.txt").inputStream().buffered()

            return load(parameterBin, featureBin, labelText)
        }

        /**
         * 加载NER模型
         * @param parameterBin 参数的BIN文件
         * @param featureBin feature的DAT格式文件
         * @param labelText label文本文件
         */
        @JvmStatic
        fun load(parameterBin: InputStream, featureBin: InputStream, labelText: InputStream): NERPerceptron {
            val model = PerceptronModel.load(parameterBin, featureBin, true)
            val labelList = labelText.use { it.bufferedReader().readLines() }
            return NERPerceptron(model, labelList)
        }
    }

}

/**
 * NER的特征工程函数
 *
 * @author jimichan
 */
object NERPerceptronFeature {

    private const val B = "_B_"

    private const val E = "_E_"

    fun extractFeatureVector(sentence: List<WordTerm>, position: Int, features: FeatureSet): IntArrayList {

        val size = sentence.size
        var pre3Word = B
        var pre2Word = B
        var pre2Pos = B
        if (position >= 2) {
            val x = sentence[position - 2]
            pre2Word = x.word
            pre2Pos = x.natureString

            if (position > 2) {
                pre3Word = sentence[position - 3].word
            }
        }

        var preWord = B
        var prePos = B
        if (position >= 1) {
            val x = sentence[position - 1]
            preWord = x.word
            prePos = x.natureString
        }

        val cur = sentence[position]
        val curWord = cur.word
        val curPos = cur.natureString

        var nextWord = E
        var nextPos = E
        if (position <= size - 2) {
            val x = sentence[position + 1]
            nextWord = x.word
            nextPos = x.natureString
        }

        var next2Word = E
        var next2Pos = E
        if (position <= size - 3) {
            val x = sentence[position + 2]
            next2Word = x.word
            next2Pos = x.natureString
        }

        val vector = IntArrayList(15)


        addFeature(features, vector, "${pre2Word}1")
        addFeature(features, vector, "${preWord}2")
        addFeature(features, vector, "${curWord}3")
        addFeature(features, vector, "${nextWord}4")
        addFeature(features, vector, "${next2Word}5")

        addFeature(features, vector, "${pre2Pos}A")
        addFeature(features, vector, "${prePos}B")
        addFeature(features, vector, "${curPos}C")
        addFeature(features, vector, "${nextPos}D")
        addFeature(features, vector, "${next2Pos}E")

        addFeature(features, vector, "$pre2Pos${prePos}F")
        addFeature(features, vector, "$prePos${curPos}G")
        addFeature(features, vector, "$curPos${nextPos}H")
        addFeature(features, vector, "$nextPos${next2Pos}I")

        addFeature(features, vector, "${pre3Word}J")

        vector.add(0)
        return vector
    }

    fun extractFeature(sentence: List<PkuWord>, position: Int, callBack: Consumer<String>) {
        val size = sentence.size

        var pre2Word = B
        var pre3Word = B
        var pre2Pos = B
        if (position >= 2) {
            val x = sentence[position - 2]
            pre2Word = x.word
            pre2Pos = x.pos

            if (position > 2) {
                pre3Word = sentence[position - 3].word
            }
        }

        var preWord = B
        var prePos = B
        if (position >= 1) {
            val x = sentence[position - 1]
            preWord = x.word
            prePos = x.pos
        }

        val cur = sentence[position]
        val curWord = cur.word
        val curPos = cur.pos

        var nextWord = E
        var nextPos = E
        if (position <= size - 2) {
            val x = sentence[position + 1]
            nextWord = x.word
            nextPos = x.pos
        }

        var next2Word = E
        var next2Pos = E
        if (position <= size - 3) {
            val x = sentence[position + 2]
            next2Word = x.word
            next2Pos = x.pos
        }

        callBack.accept("${pre2Word}1")
        callBack.accept("${preWord}2")
        callBack.accept("${curWord}3")
        callBack.accept("${nextWord}4")
        callBack.accept("${next2Word}5")

        callBack.accept("${pre2Pos}A")
        callBack.accept("${prePos}B")
        callBack.accept("${curPos}C")
        callBack.accept("${nextPos}D")
        callBack.accept("${next2Pos}E")

        callBack.accept("$pre2Pos${prePos}F")
        callBack.accept("$prePos${curPos}G")
        callBack.accept("$curPos${nextPos}H")
        callBack.accept("$nextPos${next2Pos}I")
        callBack.accept("${pre3Word}J")

    }

    private fun addFeature(features: FeatureSet, vector: IntArrayList, feature: String) {
        val id = features.featureId(feature)
        if (id >= 0) {
            vector.add(id)
        }
    }

}

/**
 * 命名实体标注感知机的训练
 * @param targetPos 针对哪些词性进行训练。需要 语料里面 有个 [石景山/ns 热电厂/n]nt 这样的格式
 */
class NERPerceptronTrainer(val targetPos: Set<String>) {

    constructor() : this(setOf("ns", "nt"))

    lateinit var featureSet: FeatureSet

    /**
     * 保存 label->labelId
     */
    var labelMap: Map<String, Int>

    init {
        val set = TreeSet<String>()
        set.add("O")
        set.add("S")
        set.addAll(targetPos.flatMap { listOf("B-$it", "M-$it", "E-$it") })

        this.labelMap = set.toList().zip(0 until set.size).toMap()
    }

    /**
     * @param trainFiles 训练文件，文件或者文件夹
     * @param evaluate 评估文件
     */
    fun train(trainFiles: File,
              evaluate: File,
              maxIter: Int,
              threadNumber: Int): NERPerceptron {

        val labelList = ArrayList<String>(labelMap.keys.sorted())

        val allFiles = if (trainFiles.isFile) listOf(trainFiles) else trainFiles.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.toList()

        prepareFeatureSet(allFiles, labelList.size)

        println("Feature Set Size ${featureSet.size()}")

        val sampleList = NerSamples(targetPos, labelMap, featureSet).prepareSample(allFiles)
        val evaluateList = evaluate.allFiles().flatMap { it.readLines() }

        println("Start Train ... ")

        val trainer = PerceptronTrainer(
                featureSet,
                labelMap.size,
                sampleList,
                EvaluateRunner { model ->
                    val ner = NERPerceptron(model, labelList)
                    NEREvaluateUtils.evaluateNER(ner, evaluateList, targetPos)
                },
                maxIter, false, 14)

        val model = trainer.train(threadNumber)


        val ner = NERPerceptron(model, labelList)

        NEREvaluateUtils.evaluateNER(ner, evaluateList, targetPos)

        return ner
    }


    /**
     * 制作FeatureSet。
     * 扫描所有语料库，为每一个特征进行编码
     */
    private fun prepareFeatureSet(corposFiles: List<File>, labelCount: Int) {
        println("开始构建NER FeatureSet")
        val t1 = System.currentTimeMillis()

        val builder = DATFeatureSetBuilder(labelCount)
        val fit = Consumer<String> { f ->
            builder.put(f)
        }

        corposFiles
                .forEach { file ->
                    println(file.absolutePath)

                    file.useLines { lines ->
                        lines.forEach { line ->
                            val flatWords = line.parseToFlatWords()

                            flatWords.forEach { w ->
                                w.word = CharNormUtils.convert(w.word)
                            }
                            for (i in 0 until flatWords.size) {
                                NERPerceptronFeature.extractFeature(flatWords, i, fit)
                            }
                        }
                    }
                }

        println("Start build featureSet ...")

        featureSet = builder.build()

        println("FeatureSet 构建完成,用时${System.currentTimeMillis() - t1}ms")
    }

}

class NerSamples(val targetPos: Set<String>, val labelMap: Map<String, Int>, val featureSet: FeatureSet) {

    fun prepareSample(files: List<File>): ArrayList<TrainSample> {

        //统计有多少样本
        var sampleSize = 0

        files.forEach { file ->
            file.useLines { it.forEach { line -> if (line.isNotBlank()) sampleSize++ } }
        }

        println("Sample Size $sampleSize")

        println("Sample List Prepare ... ")
        //预先分配好空间
        val sampleList = ArrayList<TrainSample>(sampleSize + 10)

        // 解析语料库为数字化TrainSample
        files.forEach { file ->
            file.useLines { lines ->
                lines.forEach { line ->
                    val words = line.parseToWords()
                    words.forEach {
                        if (it.hasSub()) {
                            it.subWord.forEach { w -> w.word = CharNormUtils.convert(w.word) }
                        } else {
                            it.word = CharNormUtils.convert(it.word)
                        }

                    }

                    sampleList += sentenceToSample(words)
                }
            }
        }

        return sampleList
    }

    /**
     * 把一个句子，变化为TrainSample
     * 一个用空格分隔的句子.
     *
     */
    fun sentenceToSample(line: List<PkuWord>): TrainSample {

        val wordTermList = NEREvaluateUtils.convert(line, targetPos)

        val poss = wordTermList.map { labelMap[it.customFlag]!! }.toIntArray()

        val featureMatrix = ArrayList<IntArrayList>(wordTermList.size)
        for (i in 0 until wordTermList.size) {
            featureMatrix += NERPerceptronFeature.extractFeatureVector(wordTermList, i, featureSet)
        }


        return TrainSample(featureMatrix, poss)
    }


}

/**
 * NER 评估
 */
object NEREvaluateUtils {

    /**
     * 利用WordTerm的customFlag存储tag
     */
    fun convert(sentence: List<PkuWord>, targetPOS: Set<String>): List<WordTerm> {
        val list = ArrayList<WordTerm>(sentence.size)


        for (word in sentence) {
            var pos = word.pos
            if (word.hasSub()) {
                val wordList = word.subWord
                if (targetPOS.contains(word.pos)) {
                    val first = wordList.first()
                    list += word(first.word, first.pos, "B-$pos")
                    for (i in 1 until wordList.size - 1) {
                        val the = wordList[i]
                        list += word(the.word, the.pos, "M-$pos")
                    }
                    val last = wordList.last()
                    list += word(last.word, last.pos, "E-$pos")

                } else {
                    for (sub in wordList) {
                        list += word(sub.word, sub.pos, "O")
                    }
                }
            } else {
                if (targetPOS.contains(word.pos)) {
                    list += word(word.word, word.pos, "S")
                } else {
                    list += word(word.word, word.pos, "O")
                }
            }
        }
        return list
    }

    private fun word(word: String, pos: String, label: String): WordTerm {
        val term = WordTerm(word, Nature.parse(pos))
        term.customFlag = label
        return term
    }

    fun evaluateNER(recognizer: NERPerceptron,
                    evaluateData: List<String>,
                    targetPos: Set<String>): Map<String, DoubleArray> {
        val scores = TreeMap<String, DoubleArray>()
        val avg = doubleArrayOf(0.0, 0.0, 0.0)
        scores["avg."] = avg
        for (line in evaluateData) {
            val sentence = line.parseToWords()

            //CharNorm
            sentence.forEach { if (it.hasSub()) it.subWord.forEach { x -> x.word = CharNormUtils.convert(x.word) } else it.word = CharNormUtils.convert(it.word) }

            val sentenceWordTerm = convert(sentence, targetPos)

            val pred = combineNER(recognizer.decode(sentenceWordTerm))
            val gold = combineNER(sentenceWordTerm.map { it.customFlag }.toList())
            for (p in pred) {
                val type = p.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[2]
                var s: DoubleArray? = scores[type]
                if (s == null) {
                    s = doubleArrayOf(0.0, 0.0, 0.0)
                    scores[type] = s
                }
                if (gold.contains(p)) {
                    ++s[2] // 正确识别该类命名实体数
                    ++avg[2]
                }
                ++s[0] // 识别出该类命名实体总数
                ++avg[0]
            }

            for (g in gold) {
                val type = g.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[2]
                var s: DoubleArray? = scores[type]
                if (s == null) {
                    s = doubleArrayOf(0.0, 0.0, 0.0)
                    scores[type] = s
                }
                ++s[1] // 该类命名实体总数
                ++avg[1]
            }
        }
        for (s in scores.values) {
            if (s[2] == 0.0) {
                s[0] = 0.0
                s[1] = 0.0
                continue
            }
            s[1] = s[2] / s[1] * 100 // R=正确识别该类命名实体数/该类命名实体总数×100%
            s[0] = s[2] / s[0] * 100 // P=正确识别该类命名实体数/识别出该类命名实体总数×100%
            s[2] = 2.0 * s[0] * s[1] / (s[0] + s[1])
        }

        printNERScore(scores)
        return scores
    }

    fun printNERScore(scores: Map<String, DoubleArray>) {
        System.out.printf("%4s\t%6s\t%6s\t%6s\n", "NER", "P", "R", "F1")
        for ((type, s) in scores) {
            System.out.printf("%4s\t%6.2f\t%6.2f\t%6.2f\n", type, s[0], s[1], s[2])
        }
    }

    fun combineNER(nerArray: List<String>): Set<String> {
        val result = LinkedHashSet<String>()
        var begin = 0
        var prePos = posOf(nerArray[0])
        for (i in 1 until nerArray.size) {
            if (nerArray[i][0] == 'B' || nerArray[i][0] == 'S' || nerArray[i][0] == 'O') {
                if (i - begin > 1)
                    result.add(String.format("%d\t%d\t%s", begin, i, prePos))
                begin = i
            }
            prePos = posOf(nerArray[i])
        }
        if (nerArray.size - 1 - begin > 1) {
            result.add(String.format("%d\t%d\t%s", begin, nerArray.size, prePos))
        }
        return result
    }

    private fun posOf(label: String): String {
        val index = label.indexOf('-')
        return if (index == -1) {
            label
        } else label.substring(index + 1)

    }
}
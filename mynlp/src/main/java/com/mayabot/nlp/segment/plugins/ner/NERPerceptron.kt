package com.mayabot.nlp.segment.plugins.ner

import com.mayabot.nlp.common.FastStringBuilder
import com.mayabot.nlp.common.hppc.IntArrayList
import com.mayabot.nlp.common.utils.CharNormUtils
import com.mayabot.nlp.perceptron.*
import com.mayabot.nlp.segment.Nature
import com.mayabot.nlp.segment.WordAndNature
import com.mayabot.nlp.segment.WordTerm
import com.mayabot.nlp.segment.common.PkuWord
import com.mayabot.nlp.segment.common.allFiles
import com.mayabot.nlp.segment.common.parseToFlatWords
import com.mayabot.nlp.segment.common.parseToWords
import com.mayabot.nlp.segment.wordnet.Vertex
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList

class NERPerceptron(val model: PerceptronModel, private val labels: List<String>) {

    private val featureSet = model.featureSet()


    /**
     * 解码结果保存在WordTerm的customFlag字段里面
     */
    fun decode(sentence: List<WordTerm>) {
        val buffer = FastStringBuilder(100)
        val featureList = ArrayList<IntArrayList>(sentence.size)
        for (i in 0 until sentence.size) {
            featureList += NERPerceptronFeature.extractFeatureVector(sentence, i, featureSet, buffer)
        }

        val result = model.decode(featureList)

        for (i in 0 until sentence.size) {
            sentence[i].customFlag = labels[result[i]]
        }
    }

    fun decodeVertexList(sentence: List<Vertex>): List<String> {
        val buffer = FastStringBuilder(100)
        val result = ArrayList<String>(sentence.size)

        val featureList = ArrayList<IntArrayList>(sentence.size)
        for (i in 0 until sentence.size) {
            featureList += NERPerceptronFeature.extractFeatureVector(sentence, i, featureSet, buffer)
        }

        val result2 = model.decode(featureList)

        for (i in 0 until sentence.size) {
            result += labels[result2[i]]
        }
        return result
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
            val model = PerceptronModel.loadWithFeatureBin(parameterBin, featureBin)
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

    fun extractFeatureVector(sentence: List<WordAndNature>, position: Int, features: FeatureSet, buffer: FastStringBuilder): IntArrayList {

        buffer.clear()

        val size = sentence.size
        var pre3Word = B
        var pre2Word = B
        var pre2Pos = B
        if (position >= 2) {
            val x = sentence[position - 2]
            pre2Word = x.word
            pre2Pos = x.natureName

            if (position > 2) {
                pre3Word = sentence[position - 3].word
            }
        }

        var preWord = B
        var prePos = B
        if (position >= 1) {
            val x = sentence[position - 1]
            preWord = x.word
            prePos = x.natureName
        }

        val cur = sentence[position]
        val curWord = cur.word
        val curPos = cur.natureName

        var nextWord = E
        var nextPos = E
        if (position <= size - 2) {
            val x = sentence[position + 1]
            nextWord = x.word
            nextPos = x.natureName
        }

        var next2Word = E
        var next2Pos = E
        if (position <= size - 3) {
            val x = sentence[position + 2]
            next2Word = x.word
            next2Pos = x.natureName
        }

        val vector = IntArrayList(15)

        buffer.append(pre2Word)
        buffer.append('1')
        addFeature(features, vector, buffer)

        buffer.append(preWord)
        buffer.append('2')
        addFeature(features, vector, buffer)

        buffer.append(curWord)
        buffer.append('3')
        addFeature(features, vector, buffer)

        buffer.append(nextWord)
        buffer.append('4')
        addFeature(features, vector, buffer)

        buffer.append(next2Word)
        buffer.append('5')
        addFeature(features, vector, buffer)


        buffer.append(pre2Pos)
        buffer.append('A')
        addFeature(features, vector, buffer)

        buffer.append(prePos)
        buffer.append('B')
        addFeature(features, vector, buffer)

        buffer.append(curPos)
        buffer.append('C')
        addFeature(features, vector, buffer)

        buffer.append(nextPos)
        buffer.append('D')
        addFeature(features, vector, buffer)

        buffer.append(next2Pos)
        buffer.append('E')
        addFeature(features, vector, buffer)

        buffer.append(pre2Pos)
        buffer.append(prePos)
        buffer.append('F')
        addFeature(features, vector, buffer)

        buffer.append(prePos)
        buffer.append(curPos)
        buffer.append('G')
        addFeature(features, vector, buffer)

        buffer.append(curPos)
        buffer.append(nextPos)
        buffer.append('H')
        addFeature(features, vector, buffer)

        buffer.append(nextPos)
        buffer.append(next2Pos)
        buffer.append('I')
        addFeature(features, vector, buffer)

        buffer.append(pre3Word)
        buffer.append('J')
        addFeature(features, vector, buffer)

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

    private fun addFeature(features: FeatureSet, vector: IntArrayList, stringBuilder: FastStringBuilder) {

        val id = features.featureId(stringBuilder)

        stringBuilder.clear()
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
                { _, model ->
                    val ner = NERPerceptron(model, labelList)
                    NEREvaluateUtils.evaluateNER(ner, evaluateList, targetPos)
                },
                maxIter, false)

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
//                    words.forEach {
//                        if (it.hasSub()) {
//                            it.subWord.forEach { w -> w.word = CharNormUtils.convert(w.word) }
//                        } else {
//                            it.word = CharNormUtils.convert(it.word)
//                        }
//                    }
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
    private fun sentenceToSample(line: List<PkuWord>): TrainSample {
        val buffer = FastStringBuilder(100)
        val wordTermList = convert(line, targetPos)

        val poss = wordTermList.map { labelMap[it.customFlag]!! }.toIntArray()

        val featureMatrix = ArrayList<IntArrayList>(wordTermList.size)
        for (i in 0 until wordTermList.size) {
            featureMatrix += NERPerceptronFeature.extractFeatureVector(wordTermList, i, featureSet, buffer)
        }

        return TrainSample(featureMatrix, poss)
    }

    companion object {
        /**
         * 把句子序列，打上标签。
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


        fun word(word: String, pos: String, label: String): WordTerm {
            val term = WordTerm(word, Nature.parse(pos))
            term.customFlag = label
            return term
        }
    }

}

/**
 * 第一步：把语料库清理为格式化过的格式
 */
object NERCorpus {
    @JvmStatic
    fun main(args: Array<String>) {
        val files = ArrayList<File>()
        files += File("data.work/cncorpus").allFiles()
        files += File("data.work/pku").allFiles()

        val writers = (1..20).map { File("data.work/ner/ner_$it.txt").writer() }.toList()
        val random = Random()
        var count = 0
        files.forEach { file ->
            file.useLines { lines ->
                lines.forEach { line ->
                    if (line.isNotBlank()) {
                        val words = line.parseToWords()

                        var foundNER = false
                        words.forEach {
                            if (it.hasSub()) {
                                foundNER = true
                                it.subWord.forEach { w -> w.word = CharNormUtils.convert(w.word) }
                            } else {
                                it.word = CharNormUtils.convert(it.word)
                            }
                        }

                        if (foundNER) {
                            count++
                            writers[random.nextInt(writers.size)].appendln(line)
                        }

                    }
                }
            }
        }

        println(count)


        writers.forEach { it.flush();it.close() }
    }
}

/**
 * NER 评估
 */
object NEREvaluateUtils {


    fun evaluateNER(recognizer: NERPerceptron,
                    evaluateData: List<String>,
                    targetPos: Set<String>): Map<String, DoubleArray> {
        val scores = TreeMap<String, DoubleArray>()
        val avg = doubleArrayOf(0.0, 0.0, 0.0)
        scores["avg."] = avg
        for (line in evaluateData) {
            val sentence = line.parseToWords()

            //CharNorm 语料库预先处理过
            //         sentence.forEach { if (it.hasSub()) it.subWord.forEach { x -> x.word = CharNormUtils.convert(x.word) } else it.word = CharNormUtils.convert(it.word) }

            val sentenceWordTerm = NerSamples.convert(sentence, targetPos)

            val gold = combineNER(sentenceWordTerm.map { it.customFlag }.toList())

            recognizer.decode(sentenceWordTerm)
            val pred = combineNER(sentenceWordTerm.map { it.customFlag })


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
package com.mayabot.nlp.segment.perceptron

import com.carrotsearch.hppc.IntArrayList
import com.mayabot.nlp.perceptron.*
import com.mayabot.nlp.segment.dictionary.Nature
import com.mayabot.nlp.segment.perceptron.POSPerceptronFeature.extractFeature
import com.mayabot.nlp.segment.perceptron.POSPerceptronFeature.extractFeatureVector
import com.mayabot.nlp.segment.perceptron.POSPerceptronFeature.extractFeatureVector2
import com.mayabot.nlp.utils.CharNormUtils
import com.mayabot.nlp.utils.Characters
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import kotlin.collections.ArrayList

/**
 * 词性分析感知机
 */
class POSPerceptron(val model: Perceptron, val labelList: Array<String>) {

    private val featureSet = model.featureSet()

    val parameter = (model as PerceptronModel).parameter

    val natureList = labelList.map { Nature.parse(it) }.toTypedArray()

    fun decodeToPos(sentence: List<String>): List<String> {

        val featureList = ArrayList<IntArrayList>(sentence.size)

        for (i in 0 until sentence.size) {
            featureList += extractFeatureVector(sentence, sentence.size, i, featureSet)
        }

        val result = model.decode(featureList)

        return result.map { labelList[it] }
    }

    fun decode(sentence: List<String>): List<Nature> {

        val size = sentence.size
        val featureList = ArrayList<IntArrayList>(size)

        for (i in 0 until size) {
            featureList += extractFeatureVector(sentence, size, i, featureSet)
        }

        val result = model.decode(featureList)

        return result.map { natureList[it] }
    }

    fun <T> decode(sentence: List<T>, sink: Function<T, String>): List<Nature> {

        val size = sentence.size
        val featureList = ArrayList<IntArrayList>(size)

        for (i in 0 until size) {
            featureList += extractFeatureVector2(sentence, size, i, featureSet, sink)
        }

        val result = model.decode(featureList)

        return result.map { natureList[it] }
    }

    /**
     * 特殊情况，只是查询一个词的词性
     */
    fun decode(word: String): Nature {
        val labelSize = labelList.size
        val vector = POSPerceptronFeature.extractFeatureVector(listOf(word), 1, 0, featureSet)
        val vectorBuffer = vector.buffer
        var maxIndex = 0
        var maxScore = Double.MIN_VALUE

        val offset = IntArray(vector.size() - 1)
        for (j in 0 until vector.size() - 1) {
            val index = vectorBuffer[j]
            offset[j] = index * labelSize
        }

        for (label in 0 until labelSize) {
            var score = 0.0

            for (j in 0 until vector.size() - 1) {
                score += parameter[offset[j] + label]
            }

            if (score > maxScore) {
                maxIndex = label
                maxScore = score
            }
        }

        return natureList[maxIndex]
    }


    /**
     * 保存词性分析感知机模型到文件
     */
    fun save(dir: File) {
        dir.mkdirs()

        model.save(dir)

        val out = File(dir, "label.txt").bufferedWriter()
        out.use {
            it.write(labelList.joinToString(separator = "\n"))
        }
    }

    companion object {

        /**
         * 加载NER模型
         */
        @JvmStatic
        fun load(dir: File): POSPerceptron {
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
        fun load(parameterBin: InputStream, featureBin: InputStream, labelText: InputStream): POSPerceptron {
            val model = PerceptronModel.load(parameterBin, featureBin, true)
            val labelList = labelText.use { it.bufferedReader().readLines() }
            return POSPerceptron(model, labelList.toTypedArray())
        }

    }
}

/**
 * POS特征工程
 */
object POSPerceptronFeature {

    private const val CHAR_BEGIN = "_B_"

    private const val CHAR_END = "_E_"

    @JvmStatic
    fun extractFeature(sentence: List<String>, size: Int, position: Int, callBack: Consumer<String>) {

        var preWord = if (position > 0) sentence[position - 1] else CHAR_BEGIN
        val curWord = sentence[position]
        var nextWord = if (position < size - 1) sentence[position + 1] else CHAR_END

        if (nextWord.length == 1) {
            val c = nextWord[0]
            val isP = Characters.isPunctuation(c)
            if (isP || c == ' ') {
                // 我认为标点符号和词性无关
                nextWord = "XPU"
            }
        }

        if (preWord.length == 1) {
            val c = preWord[0]
            val isP = Characters.isPunctuation(c)
            if (isP || c == ' ') {
                // 我认为标点符号和词性无关
                preWord = "XPU"
            }
        }

        callBack.accept("${preWord}☺")
        //让同一个特征出现两次。我认为这个特征比较重要
        callBack.accept(curWord)
        callBack.accept("${nextWord}♂")

        val length = curWord.length

        // prefix
        if (length >= 2) {
            val last = length - 1

            val c1 = curWord[0]
            val l1 = curWord[last]

            callBack.accept("$c1★")
            callBack.accept("$l1✆")

            if (length >= 3) {
                val c2 = curWord[1]
                val l2 = curWord[last - 1]

                callBack.accept("$c1$c2★")
                callBack.accept("$l1$l2✆")

                if (length >= 4) {
                    val c3 = curWord[2]
                    val l3 = curWord[last - 2]
                    callBack.accept("$c1$c2$c3★")
                    callBack.accept("$l1$l2$l3✆")
                }
            }
        }
    }


    val s2s = Function<String, String> { it }

    @JvmStatic
    fun extractFeatureVector(sentence: List<String>, size: Int, position: Int, features: FeatureSet): IntArrayList {
        return extractFeatureVector2(sentence, size, position, features, s2s)
    }


    //TODO 这里可以把String变成CharSequence. 这样vertex就可以传入char[]进行优化
    @JvmStatic
    fun <T> extractFeatureVector2(sentence: List<T>, size: Int, position: Int, features: FeatureSet, sink: Function<T, String>): IntArrayList {

        val vector = IntArrayList(11)

        var preWord = if (position > 0) sink.apply(sentence[position - 1]) else CHAR_BEGIN
        val curWord = sink.apply(sentence[position])
        var nextWord = if (position < size - 1) sink.apply(sentence[position + 1]) else CHAR_END

        if (nextWord.length == 1) {
            val c = nextWord[0]
            val isP = Characters.isPunctuation(c)
            if (isP || c == ' ') {
                // 我认为标点符号和词性无关
                nextWord = "XPU"
            }
        }

        if (preWord.length == 1) {
            val c = preWord[0]
            val isP = Characters.isPunctuation(c)
            if (isP || c == ' ') {
                // 我认为标点符号和词性无关
                preWord = "XPU"
            }
        }

        addFeature(features, vector, "${preWord}☺")

        //让同一个特征出现两次。我认为这个特征比较重要
        val id = features.featureId(curWord)
        if (id >= 0) {
            vector.add(id)
            vector.add(id)
        }

        addFeature(features, vector, "${nextWord}♂")

        val length = curWord.length

        // prefix
        if (length >= 2) {
            val last = length - 1

            val c1 = curWord[0]
            val l1 = curWord[last]

            addFeature(features, vector, "$c1★")
            addFeature(features, vector, "$l1✆")

            if (length >= 3) {
                val c2 = curWord[1]
                val l2 = curWord[last - 1]

                addFeature(features, vector, "$c1$c2★")
                addFeature(features, vector, "$l1$l2✆")

                if (length >= 4) {
                    val c3 = curWord[2]
                    val l3 = curWord[last - 2]
                    addFeature(features, vector, "$c1$c2$c3★")
                    addFeature(features, vector, "$l1$l2$l3✆")
                }
            }
        }
//
//        //最后一列保留给特征向量使用
        vector.add(0)

        return vector
    }

    private fun addFeature(features: FeatureSet, vector: IntArrayList, feature: String) {
        val id = features.featureId(feature)
        if (id >= 0) {
            vector.add(id)
        }
    }

}

/**
 * 词性标注感知机的训练
 */
class POSPerceptronTrainer {

    lateinit var featureSet: FeatureSet

    /**
     * 保存 词性->词性ID
     */
    lateinit var labelMap: Map<String, Int>

    fun train(dir: File, maxIter: Int, threadNumber: Int): POSPerceptron {

        val allFiles = if (dir.isFile) listOf(dir) else dir.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.toList()

        prepareFeatureSet(allFiles)

        println("Feature Set Size ${featureSet.size()}")

        val sampleList = loadSamples(allFiles)

        println("Start Train ... ")

        val trainer = PerceptronTrainer(
                featureSet,
                labelMap.size,
                sampleList,
                POSEvaluateRunner(sampleList),
                maxIter, true, 10)

        trainer

        val model = trainer.train(threadNumber)

        println("--------------------")

        POSEvaluateRunner(sampleList).run(model)

        return POSPerceptron(model, labelMap.keys.sorted().toTypedArray())
    }


    /**
     * 从文件中加载TrainSample
     */
    fun loadSamples(files: List<File>): List<TrainSample> {

        /**
         * 把一个句子，变化为TrainSample
         * 一个用空格分隔的句子.
         *
         */
        fun sentenceToSample(line: List<PkuWord>): TrainSample {
            val words = line.map { it.word }
            val poss = line.map { labelMap[it.pos]!! }.toIntArray()

            val featureMatrix = ArrayList<IntArrayList>(words.size)
            for (i in 0 until words.size) {
                featureMatrix += POSPerceptronFeature.extractFeatureVector(words, words.size, i, featureSet)
            }

            return TrainSample(featureMatrix, poss)
        }

        //统计有多少样本
        var sampleSize = 0

        files.forEach { file ->
            file.useLines { it.forEach { line -> if (line.isNotBlank()) sampleSize++ } }
        }

        println("Will load $sampleSize Sample")

        //预先分配好空间
        val sampleList = ArrayList<TrainSample>(sampleSize + 10)

        var count = 0

        System.out.print("Load 0%")
        // 解析语料库为数字化TrainSample
        files.forEach { file ->
            file.useLines { lines ->
                lines.forEach { line ->
                    val words = line.parseToFlatWords().filter { it.word.isNotEmpty() && it.pos != "" }
                    words.forEach {
                        it.word = CharNormUtils.convert(it.word)
                    }
                    sampleList += sentenceToSample(words)

                    count++

                    if (count % 100 == 0) {
                        System.out.print("\rLoad ${"%.2f".format(count * 100.0 / sampleList.size)}%")
                    }
                }
            }
        }
        System.out.print("\r")

        return sampleList
    }

    /**
     * 制作FeatureSet。
     * 扫描所有语料库，为每一个特征进行编码。
     * 把feature进行字典排序，feature的位置就是ID。
     *
     */
    private fun prepareFeatureSet(corposFiles: List<File>) {
        println("开始构建POS FeatureSet")
        val t1 = System.currentTimeMillis()


        val posSet = HashSet<String>(500)
        corposFiles.forEach { file ->
            println(file.absolutePath)
            file.useLines { lines ->
                lines.forEach { line ->
                    val flatWords = line.parseToFlatWords()
                    flatWords.forEach {
                        if (it.pos != "" && !posSet.contains(it.pos)) {
                            posSet.add(it.pos)
                        }
                    }
                }
            }
        }

        val builder = DATFeatureSetBuilder(posSet.size)
        val fit = Consumer<String> { f ->
            builder.put(f)
        }

        corposFiles.forEach { file ->
            println(file.absolutePath)
            file.useLines { lines ->
                lines.forEach { line ->
                    val flatWords = line.parseToFlatWords()
                    val words = flatWords.map { CharNormUtils.convert(it.word) }.filter { it.isNotEmpty() }
                    for (i in 0 until words.size) {
                        extractFeature(words, words.size, i, fit)
                    }
                }
            }
        }

        println("Start build featureSet ...")

        this.featureSet = builder.build()

        this.labelMap = posSet.sorted().zip(0 until posSet.size).toMap()

        println("FeatureSet构建完成,用时${System.currentTimeMillis() - t1}ms")
    }

}


class POSEvaluateRunner(val sampleList: List<TrainSample>) : EvaluateRunner {

    override fun run(model: Perceptron) {

        val random = Random(0)
        val bili = 0.8f
        var total = 0.0
        var right = 0.0
        var targetSampleSize = (sampleList.size * bili).toInt()
        var count = 0
        System.out.print("Evaluating 0%")
        sampleList.forEach { sample ->
            // 抽样10%进行验证
            if (random.nextDouble() < bili) {
                count++
                total += sample.label.size
                val result = model.decode(sample.featureMatrix)
                for (x in 0 until result.size) {
                    if (sample.label[x] == result[x]) {
                        right++
                    }
                }
                if (count % 800 == 0) {
                    System.out.print("\rEvaluating ${"%.2f".format(count * 100.0 / targetSampleSize)}%")

                }
            }
        }

        System.out.println("\nP = ${"%.3f".format((right / total))}")
    }

}
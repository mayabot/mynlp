package com.mayabot.nlp.perceptron.solution.pos

import com.carrotsearch.hppc.IntArrayList
import com.mayabot.nlp.perceptron.*
import com.mayabot.nlp.perceptron.solution.Word
import com.mayabot.nlp.perceptron.solution.parseToFlatWords
import com.mayabot.nlp.utils.CharNormUtils
import com.mayabot.nlp.utils.Characters
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


fun main(args: Array<String>) {
//    val model = POSPerceptronTrainer().train(File("data/pku01.txt"),2)
//
//    model.save(File("data/pos.bin"))
    val model = POSPerceptron.loadModel(File("data/pos.bin"))
    val words = "余额宝 的 规模 增长 一直 呈现 不断 加速 , 的 状态".split(" ")
    val result = model.decode(words)
    println(words.zip(result))
}


/**
 * 词性分析感知机
 */
class POSPerceptron(val model: PerceptronModel, val posLabMap: Map<String, Int>) {


    val featureSet = model.featureSet()!!

    private val posTagList = Array(posLabMap.size) { "" }.apply {
        posLabMap.forEach { t, u ->
            this[u] = t
        }
    }

    /**
     * 保存词性分析感知机模型到文件
     */
    fun save(file: File) {
        val out = file.outputStream().buffered()
        out.use {
            model.save(it)
            val outBuffer = DataOutputStream(out)
            outBuffer.writeUTF(posTagList.joinToString(separator = ","))
            outBuffer.flush()
            it.flush()
        }
    }


    fun decode(sentence: List<String>): List<String> {
        val featureList = ArrayList<IntArrayList>(sentence.size)
        for (i in 0 until sentence.size) {
            featureList += extractFeatureVector(sentence, sentence.size, i, featureSet)
        }

        val result = model.decode(featureList)

        return result.map { posTagList[it] }
    }


    companion object {

        private const val CHAR_BEGIN = "_B_"

        private const val CHAR_END = "_E_"

        fun loadModel(file: File): POSPerceptron {
            return loadModel(file.inputStream().buffered())
        }

        fun loadModel(input: InputStream): POSPerceptron {
            val model = CostumisedPerceptron.load(input)
            val din = DataInputStream(input)
            val lab = din.readUTF().split(",").toTypedArray()
            val labMap = HashMap<String, Int>()
            lab.forEachIndexed { index, s ->
                labMap[s] = index
            }

            return POSPerceptron(model, labMap)
        }

        private inline fun addFeature(features: FeatureSet, vector: IntArrayList, feature: String) {
            val id = features.featureId(feature)
            if (id >= 0) {
                vector.add(id)
            }
        }

        fun extractFeatureVector(sentence: List<String>, size: Int, position: Int, features: FeatureSet): IntArrayList {

            val vector = IntArrayList(11)

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
            return vector
        }

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
            val x = curWord
            callBack.accept(x)
            callBack.accept(x)
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
    }

}


/**
 * 词性标注感知机的训练
 */
class POSPerceptronTrainer(
) {
//
//    init {
//        workDir.mkdirs()
//    }

    lateinit var featureSet: FeatureSet

    /**
     * 保存 词性->词性ID
     */
    lateinit var labelMap: Map<String, Int>

    fun train(dir: File, maxIter: Int): POSPerceptron {

        val allFiles = if (dir.isFile) listOf(dir) else dir.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.toList()

        prepareFeatureSet(allFiles)

        println("Feature Set Size ${featureSet.size()}")

        //统计有多少样本
        var sampleSize = 0

        allFiles.forEach { file ->
            file.useLines { it.forEach { line -> if (line.isNotBlank()) sampleSize++ } }
        }

        println("Sample Size $sampleSize")

        println("Sample List Prepare ... ")
        //预先分配好空间
        val sampleList = ArrayList<TrainSample>(sampleSize + 10)

        // 解析语料库为数字化TrainSample
        allFiles.forEach { file ->
            file.useLines { lines ->
                lines.forEach { line ->
                    val words = line.parseToFlatWords().filter { it.word.isNotEmpty() && it.pos != "" }
                    sampleList += sentenceToSample(words)
                }
            }
        }

        println("Start Train ... ")

        val evaluateRunner = EvaluateRunner { model ->
            val random = Random(0)
            val bili = 0.1f
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
                    if (count % 200 == 0) {
                        System.out.print("\rEvaluating ${"%.2f".format(count * 100.0 / targetSampleSize)}%")

                    }
                }
            }
            System.out.print("\r")

            System.out.println("P = ${"%.3f".format((right / total))}")
        }

        val trainer = PerceptronTrainer(
                featureSet,
                labelMap.size,
                sampleList,
                evaluateRunner,
                maxIter)

        val model = trainer.train()

        return POSPerceptron(model, labelMap)
    }

    /**
     * 把一个句子，变化为TrainSample
     * 一个用空格分隔的句子.
     *
     */
    fun sentenceToSample(line: List<Word>): TrainSample {
        val words = line.map { it.word }
        val poss = line.map { labelMap[it.pos]!! }.toIntArray()

        val featureMatrix = ArrayList<IntArrayList>(words.size)
        for (i in 0 until words.size) {
            featureMatrix += POSPerceptron.extractFeatureVector(words, words.size, i, featureSet)
        }

        return TrainSample(featureMatrix, poss)
    }

    /**
     * 制作FeatureSet。
     * 扫描所有语料库，为每一个特征进行编码
     */
    private fun prepareFeatureSet(corposFiles: List<File>) {
        println("开始构建POS FeatureSet")
        val t1 = System.currentTimeMillis()

        val builder = FeatureSetBuilder()
        val fit = Consumer<String> { f ->
            builder.put(f)
        }

        val posSet = HashSet<String>(500)

        corposFiles
                .forEach { file ->
                    println(file.absolutePath)

                    file.useLines { lines ->
                        lines.forEach { line ->
                            val flatWords = line.parseToFlatWords()
                            flatWords.forEach {
                                if (it.pos != "" && !posSet.contains(it.pos)) {
                                    posSet.add(it.pos)
                                }
                            }
                            val words = flatWords.map { CharNormUtils.convert(it.word) }.filter { it.isNotEmpty() }
                            for (i in 0 until words.size) {
                                POSPerceptron.extractFeature(words, words.size, i, fit)
                            }
                        }
                    }
                }

        println("Start build featureSet ...")

        featureSet = builder.build()

        val labelMap = HashMap<String, Int>(posSet.size * 3)
        var id = 0
        TreeSet(posSet).forEach { word ->
            labelMap[word] = id
            id++
        }
        this.labelMap = labelMap

        println("FeatureSet 构建完成,用时${System.currentTimeMillis() - t1}ms")
    }

}
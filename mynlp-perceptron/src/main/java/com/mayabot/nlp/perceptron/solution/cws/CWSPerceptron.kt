package com.mayabot.nlp.perceptron.solution.cws

import com.carrotsearch.hppc.IntArrayList
import com.google.common.base.Splitter
import com.mayabot.nlp.perceptron.*
import com.mayabot.nlp.perceptron.solution.cws.CWSPerceptron.Companion.B
import com.mayabot.nlp.perceptron.solution.cws.CWSPerceptron.Companion.E
import com.mayabot.nlp.perceptron.solution.cws.CWSPerceptron.Companion.M
import com.mayabot.nlp.perceptron.solution.cws.CWSPerceptron.Companion.S
import com.mayabot.nlp.perceptron.solution.cws.CWSPerceptron.Companion.extractFeature
import com.mayabot.nlp.utils.CharNormUtils
import java.io.File
import java.io.InputStream
import java.util.function.Consumer

/**
 * 用B M E S进行分词的感知机模型
 */
class CWSPerceptron(val model: PerceptronModel) {

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

        val featureList = ArrayList<IntArrayList>(sentence.size)
        for (i in 0 until sentence.size) {
            featureList += extractFeatureVector(sentence, sentence.size, i, model.featureSet())
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

        private const val CHAR_BEGIN = '\u0001'

        private const val CHAR_END = '\u0002'

        fun load(parameterBin: InputStream, featureBin: InputStream): CWSPerceptron {
            val model = Perceptron.load(parameterBin, featureBin, true)
            return CWSPerceptron(model)
        }

        fun load(dir: File): CWSPerceptron {
            return load(File(dir, "parameter.bin").inputStream().buffered(),
                    File(dir, "feature.dat").inputStream().buffered())
        }

        fun extractFeatureVector(sentence: CharArray, size: Int, position: Int, features: FeatureSet): IntArrayList {
            val vector = IntArrayList(8)

            val pre2Char = if (position >= 2) sentence[position - 2] else CHAR_BEGIN
            val preChar = if (position >= 1) sentence[position - 1] else CHAR_BEGIN
            val curChar = sentence[position]
            val nextChar = if (position < size - 1) sentence[position + 1] else CHAR_END
            val next2Char = if (position < size - 2) sentence[position + 2] else CHAR_END

            addFeature(features, vector, "${pre2Char}1")
            addFeature(features, vector, "$curChar")
            addFeature(features, vector, "${nextChar}3")

            addFeature(features, vector, pre2Char + "/" + preChar + "4")
            addFeature(features, vector, preChar + "/" + curChar + "5")
            addFeature(features, vector, curChar + "/" + nextChar + "6")
            addFeature(features, vector, nextChar + "/" + next2Char + "7")

            //最后一列保留给特征向量使用
            vector.add(0)

            return vector
        }


        private inline fun addFeature(features: FeatureSet, vector: IntArrayList, feature: String) {
            val id = features.featureId(feature)
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

            callBack.accept(pre2Char + "1")
            callBack.accept(curChar + "")
            callBack.accept(nextChar + "3")

            callBack.accept(pre2Char + "/" + preChar + "4")
            callBack.accept(preChar + "/" + curChar + "5")
            callBack.accept(curChar + "/" + nextChar + "6")
            callBack.accept(nextChar + "/" + next2Char + "7")
        }
    }
}


/**
 * 分词感知机的训练
 */
class CWSPerceptronTrainer(val workDir: File = File("data/pcws")) {

    lateinit var featureSet: FeatureSet

    init {
        workDir.mkdirs()
    }

    fun train(trainFileDir: File, evaluateFile: File, maxIter: Int, threadNumber: Int): CWSPerceptron {

        val allFiles = if (trainFileDir.isFile) listOf(trainFileDir) else trainFileDir.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.toList()


        val featureSetFile = File(workDir, "feature.dat")
        if (featureSetFile.exists()) {
            featureSet = FeatureSet.read(featureSetFile.inputStream().buffered(),
                    File(workDir, "feature.txt").inputStream().buffered()
            )
        } else {
            prepareFeatureSet(allFiles)
            featureSet.save(File(workDir, "feature.dat"), File(workDir, "feature.txt"))
        }

        println("FeatureSet Size ${featureSet.size()}")

        //统计有多少样本
        var sampleSize = 0

        allFiles.forEach { file ->
            file.useLines { it.forEach { if (it.isNotBlank()) sampleSize++ } }
        }

        println("Sample Size $sampleSize")

        //预先分配好空间
        val sampleList = ArrayList<TrainSample>(sampleSize + 10)

        // 解析语料库为数字化TrainSample
        allFiles.forEach { file ->
            file.useLines { lines ->
                lines.forEach { line ->
                    if (line.isNotBlank()) {
                        sampleList += sentenceToSample(line.trim())
                    }
                }
            }
        }

        //验证集合
        val evaluateSample = (if (evaluateFile.isFile) listOf(evaluateFile) else evaluateFile.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.toList())
                .flatMap { it.readLines() }.map { CharNormUtils.convert(it) }

        println("Start train ...")

        val trainer = PerceptronTrainer(featureSet, CWSPerceptron.tagList.size, sampleList, EvaluateRunner { it ->
            CWSEvaluate.evaluate(evaluateSample, CWSPerceptron(it))
        }, maxIter)

        val model = CWSPerceptron(trainer.train(threadNumber))

        return model

    }


    /**
     * 把一个句子，变化为TrainSample
     * 一个用空格分隔的句子.
     *
     */
    private fun sentenceToSample(lineInput: String): TrainSample {
        val line = CharNormUtils.convert(lineInput)
        val juzi = CharArray(line.length)
        val split = BooleanArray(line.length)
        var len = 0
        line.forEach { c ->
            if (c != ' ') {
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
            val vec = CWSPerceptron.extractFeatureVector(juzi, len, i, featureSet)
            list.add(vec)

            if (split[i]) {
                val wordLen = i - from + 1
                if (wordLen == M) {
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

    /**
     * 制作FeatureSet。
     * 扫描所有语料库，为每一个特征进行编码
     */
    fun prepareFeatureSet(files: List<File>) {
        println("开始构建FeatureSet")
        val t1 = System.currentTimeMillis()
        val builder = DATFeatureSetBuilder()
        val fit = Consumer<String> { f ->
            builder.put(f)
        }
        files.forEach { dictFile ->
            println(dictFile.absolutePath)
            val lines = dictFile.readLines()

            lines.forEach { line ->
                val out = CharArray(line.length)
                var p = 0
                CharNormUtils.convert(line).forEach { c ->
                    if (c != ' ' && !c.isWhitespace()) {
                        out[p++] = c
                    }
                }
                val len = p


                for (i in 0 until len) {
                    extractFeature(out, len, i, fit)
                }
            }
        }

        val f = File("data/pcws/features.txt").bufferedWriter()

        builder.keys.forEach { key ->
            f.write("$key\n")
        }

        f.close()

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

        val splitter = Splitter.on(" ").omitEmptyStrings().trimResults()

        System.out.print("Evaluating 0%")

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

            if (count % 200 == 0) {
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

        System.out.println("正确率(P) %.2f , 召回率(R) %.2f , F1 %.2f".format(result[0], result[1], result[2]))

        return result
    }
}

package com.mayabot.nlp.perceptron

import com.carrotsearch.hppc.IntArrayList
import com.mayabot.nlp.collection.dat.DATArrayIndex
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.InputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors


/**
 * 感知机训练的样本
 * featureMatrix中的IntArray最后多一位是留个转移特征使用
 */
class TrainSample(
        val featureMatrix: List<IntArrayList>,
        val label: IntArray) {
    val size = featureMatrix.size
}

class Labels(val goldFeature: IntArray, val predFeature: IntArray)


/**
 * 感知机模型
 */
open class PerceptronModel(
        private var featureSet: FeatureSet,
        protected val labelCount: Int,
        var parameter: FloatArray
) : Perceptron {


    private val MaxScore = Integer.MIN_VALUE.toDouble()
    var decodeQuickModel = false

    var featureSetSize = featureSet.size()
    var parameterSize = parameter.size


    constructor(featureSet: FeatureSet, labelCount: Int) :
            this(featureSet, labelCount, FloatArray(featureSet.size() * labelCount))

    override fun featureSet() = featureSet

    /**
     * 单线程训练时调用.
     * 平均感知机
     */
    fun update(data: TrainSample, total: DoubleArray, timestamp: IntArray, current: Int) {
        val length = data.size
        val guessLabel = IntArray(length)
        decode(data.featureMatrix, guessLabel)
        for (i in 0 until length) {
            val labels = featureToLabel(data, guessLabel, i)
            updateParameter(labels.goldFeature, labels.predFeature, total, timestamp, current)
        }
    }

    /**
     * 多线程训练时调用.
     * 结构化感知机
     */
    fun update(data: TrainSample) {
        val length = data.size
        val guessLabel = IntArray(length)
        decode(data.featureMatrix, guessLabel)
        for (i in 0 until length) {
            val labels = featureToLabel(data, guessLabel, i)
            updateOnline(labels.goldFeature, labels.predFeature)
        }
    }

    private fun featureToLabel(data: TrainSample, guessLabel: IntArray, i: Int): Labels {
        //序号代替向量 替代数组
        val featureVector = data.featureMatrix[i]
        //正确标签
        val goldFeature = IntArray(featureVector.size())
        //预测标签
        val predFeature = IntArray(featureVector.size())
        val last = featureVector.size() - 1
        for (j in 0 until last) {
            //特征 -> 对应标签
            goldFeature[j] = featureVector[j] * labelCount + data.label[i]
            predFeature[j] = featureVector[j] * labelCount + guessLabel[i]
        }
        goldFeature[last] = (if (i == 0) labelCount else data.label[i - 1]) * labelCount + data.label[i]
        predFeature[last] = (if (i == 0) labelCount else guessLabel[i - 1]) * labelCount + guessLabel[i]
        return Labels(goldFeature, predFeature)
    }

    private fun updateParameter(goldIndex: IntArray, predictIndex: IntArray, total: DoubleArray, timestamp: IntArray, current: Int): Boolean {
        if (goldIndex.contentEquals(predictIndex)) return false
        for (i in goldIndex.indices) {
            if (goldIndex[i] == predictIndex[i])
                continue
            else {
                record(goldIndex[i], 1f, total, timestamp, current)//当预测的标注和实际标注不一致时 先更新权重
                if (predictIndex[i] >= 0 && predictIndex[i] < parameter.size)
                    record(predictIndex[i], -1f, total, timestamp, current)
                else {
                    throw IllegalArgumentException("更新参数时传入了非法的下标")
                }
            }
        }
        return true
    }

    private fun updateOnline(goldIndex: IntArray, predictIndex: IntArray) {
        for (i in goldIndex.indices) {

            val xii = predictIndex[i]
            if (goldIndex[i] == xii)
                continue
            else {
                parameter[goldIndex[i]]++
                if (xii >= 0 && xii < parameter.size)
                    parameter[xii]--
                else {
                    throw IllegalArgumentException("更新参数时传入了非法的下标")
                }
            }
        }
    }

    private fun record(index: Int, value: Float, total: DoubleArray, timestamp: IntArray, current: Int) {
        val passed = current - timestamp[index]
        total[index] += passed * parameter[index].toDouble() //权重乘以该纬度经历时间
        parameter[index] += value
        timestamp[index] = current
    }


    fun average(total: DoubleArray, timestamp: IntArray, current: Int) {
        for (i in 0 until parameter.size) {
            val pass = current.toFloat() - timestamp[i].toFloat()
            val totali = total[i].toFloat()
            parameter[i] = (totali + pass * parameter[i]) / current
        }
    }

    /**
     * 模型压缩
     * @param ratio 压缩比，如0.2，那么就是去掉0.2的特征
     */
    override fun compress(ratio: Double, threshold: Double) {
        if (ratio < 0 || ratio >= 1) {
            throw IllegalArgumentException("压缩比必须介于 0 和 1 之间")
        }

        assert(featureSet.keys != null)

        val k = if (ratio == 0.0) 0 else (ratio * featureSet.size()).toInt()

        val featureList = featureSet.keys!!

        fun score(id: Int): Float {
            var s = 0f
            for (i in id * labelCount until id * labelCount + labelCount) {
                s += Math.abs(parameter[i])
            }
            return s
        }

        val filterSet = HashSet<Int>()
        for (id in 0 until featureList.size) {
            val s = score(id)
            if (s < threshold) {
                filterSet.add(id)
            }
        }

        println("threshold filterd ${filterSet.size}")

        //移除了很小的之后，还没有达到缩减的值
        if (k > 0 && filterSet.size < k) {
            val heap = TopIntMinK(k - filterSet.size)

            println("let's filter top min ${k - filterSet.size}")

            for (id in 0 until featureList.size) {
                val s = score(id)
                if (s >= threshold) {
                    heap.push(id, s)
                }
            }

            val topMinResultIdSet = heap.result().map { it.first }.toSet()

            filterSet.addAll(topMinResultIdSet)
        }

        println("remove ${filterSet.size} feature,real compress ${String.format("%.3f", filterSet.size * 1.0f / featureList.size)}")


        val newSize = featureList.size - filterSet.size
        val newFeatureList = ArrayList<String>(newSize)
        val newParameter = FloatArray(labelCount * newSize)

        var cc = 0

        featureList.forEachIndexed { index, s ->
            if (index <= labelCount || !filterSet.contains(index)) {
                // index<=labelCount 是特征构件是保留的和labelCount+1数量相等的特征，需要保留
                newFeatureList += s
                System.arraycopy(parameter, index * labelCount, newParameter, cc * labelCount, labelCount)
                cc++
            }
        }

        parameter = newParameter
        this.featureSet = FeatureSet(DATArrayIndex(newFeatureList), newFeatureList)

        featureSetSize = this.featureSet.size()
        parameterSize = parameter.size

    }

    override fun save(dir: File) {
        dir.mkdirs()
        File(dir, "parameter.bin").outputStream().buffered().use {
            val dout = DataOutputStream(it)
            dout.writeInt(labelCount)
            dout.writeInt(parameter.size)
            parameter.forEach { w -> dout.writeFloat(w) }

            dout.flush()
        }

        featureSet.save(File(dir, "feature.dat"), File(dir, "feature.txt"))
    }

    companion object {

        fun load(parameterBin: InputStream, featureBin: InputStream, featureDat: Boolean): PerceptronModel {

            return if (featureDat) {
                load(parameterBin, featureBin, null)
            } else {
                load(parameterBin, null, featureBin)
            }
        }

        fun load(parameterFile: File, featureBin: File?, featureText: File?): PerceptronModel {
            return load(parameterFile.inputStream().buffered(),
                    featureBin?.inputStream()?.buffered(),
                    featureText?.inputStream()?.buffered()
            )
        }

        fun load(dir: File): PerceptronModel {
            fun loadIfExit(fname: String): File? {
                val f = File(dir, fname)
                return if (f.exists()) {
                    f
                } else {
                    null
                }
            }

            return load(
                    File(dir, "parameter.bin"),
                    loadIfExit("feature.dat"),
                    loadIfExit("feature.txt")
            )
        }

        fun load(parameterBin: InputStream, featureBin: InputStream?, featureText: InputStream?): PerceptronModel {

            var labelCount = 0
            var parameter = FloatArray(0)
            parameterBin.use { x ->
                val input = DataInputStream(x)
                labelCount = input.readInt()

                val pSize = input.readInt()
                parameter = FloatArray(pSize)
                for (i in 0 until pSize) {
                    parameter[i] = input.readFloat()
                }
            }

            val fs = if (featureBin != null) {
                if (featureText != null) {
                    FeatureSet.read(featureBin, featureText)
                } else {
                    FeatureSet.read(featureBin)
                }
            } else {
                if (featureText != null) {
                    FeatureSet.read(featureText)
                } else {
                    throw RuntimeException()
                }
            }

            return PerceptronModel(fs, labelCount, parameter)
        }
    }


    /**
     * viterbi
     */
    override fun decode(featureSequence: List<IntArrayList>, guessLabel: IntArray): Double {
        val bos = labelCount
        val sentenceLength = featureSequence.size
        val labelSize = labelCount

        val preMatrix = IntArray(sentenceLength * labelSize)

//        val scoreMatrix = Array(2) { DoubleArray(labelSize) }
        //上一回的状态
        var scoreMLast = DoubleArray(labelSize)
        var scoreMNow = DoubleArray(labelSize)

        //first
        val firstFeature = featureSequence[0]

        for (j in 0 until labelCount) {
            preMatrix[j] = j
            val score = scoreBase(firstFeature, j) + parameter[bos * labelSize + j]
            scoreMLast[j] = score
        }

        if (decodeQuickModel) {
            val top = TopIntMaxK(4)
            for (i in 1 until sentenceLength) {

                val allFeature = featureSequence[i]
                val base = i * labelSize


                for (curLabel in 0 until labelCount) {
                    val baseScore = scoreBase(allFeature, curLabel)
                    top.push(curLabel, baseScore)
                }

                //找出最大的TOP4
                val topThree = top.resort()
                top.clear()

                for (e in topThree) {
                    var maxScore = MaxScore

                    val curLabel = e.first
                    val baseScore = e.second

                    for (preLabel in 0 until labelCount) {

                        val score = baseScore + parameter[preLabel * labelSize + curLabel]

                        val curScore = scoreMLast[preLabel] + score

                        if (curScore > maxScore) {
                            maxScore = curScore
                            preMatrix[base + curLabel] = preLabel
                            scoreMNow[curLabel] = maxScore
                        }
                    }
                }

                //switch
                val temp = scoreMLast
                scoreMLast = scoreMNow
                scoreMNow = temp
            }

        } else {
            for (i in 1 until sentenceLength) {

                val allFeature = featureSequence[i]
                val base = i * labelSize

                for (curLabel in 0 until labelCount) {

                    var maxScore = MaxScore

                    //因为score(allFeature, curLabel) 中除了最后特征位置不一样，其他位置的得分都是重复的
                    val baseScore = scoreBase(allFeature, curLabel)

                    for (preLabel in 0 until labelCount) {

//                    allFeature[transitionFeatureIndex] = preLabel
//                    val score = score(allFeature, curLabel)
                        val score = baseScore + parameter[preLabel * labelSize + curLabel]

                        val curScore = scoreMLast[preLabel] + score

                        if (curScore > maxScore) {
                            maxScore = curScore
                            preMatrix[base + curLabel] = preLabel
                            scoreMNow[curLabel] = maxScore
                        }
                    }
                }

                //switch
                val temp = scoreMLast
                scoreMLast = scoreMNow
                scoreMNow = temp
            }

        }


        //此时scoreM0 肯定是最后一个
        var maxIndex = 0
        var maxScore = scoreMLast[0]

        for (index in 1 until labelCount) {
            val x = scoreMLast[index]
            if (maxScore < x) {
                maxIndex = index
                maxScore = x
            }
        }

        var k = (sentenceLength - 1) * labelCount
        for (i in sentenceLength - 1 downTo 0) {
            guessLabel[i] = maxIndex
            maxIndex = preMatrix[k + maxIndex]
            k -= labelCount
        }

        return maxScore
    }

    private fun scoreBase(featureVector: IntArrayList, currentTag: Int): Double {

        var score = 0.0

        val buffer = featureVector.buffer
        for (i in 0 until featureVector.size() - 1) {
            val index = buffer[i]
            if (index < 0 || index >= featureSetSize) {
                // do nothing
            } else {
                score += parameter[index * labelCount + currentTag]
            }
        }

        return score
    }

}


/**
 * 通用感知机训练器
 * @param featureSet 特征集合
 * @param labelCount 标签的数量
 * @param trainSource 训练样例列表
 * @param evaluateScript 评估运行器
 * @param maxIter 迭代轮数
 * @param decodeQuickModel_ 是否启用快速解码(词性标注时启用)
 */
class PerceptronTrainer(
        private val featureSet: FeatureSet,
        private val labelCount: Int,
        private val trainSource: List<TrainSample>,
        private val evaluateScript: EvaluateRunner,
        private val maxIter: Int,
        private val decodeQuickModel_: Boolean) {

    private fun buildPerceptronModel(featureSet: FeatureSet, labelCount: Int): PerceptronModel {
        return PerceptronModel(
                featureSet, labelCount
        ).apply { this.decodeQuickModel = decodeQuickModel_ }
    }

    private fun buildPerceptronModel(featureSet: FeatureSet, labelCount: Int, parameter: FloatArray): PerceptronModel {
        return PerceptronModel(
                featureSet, labelCount, parameter
        ).apply { this.decodeQuickModel = decodeQuickModel_ }
    }

    /**
     * 默认多线程训练。
     * @param threadNumber 线程数。threadNumber=1 时平均感知机
     */
    @JvmOverloads
    fun train(threadNumber: Int = Runtime.getRuntime().availableProcessors() - 1): Perceptron {
        return if (threadNumber == 1) {
            trainOneThread()
        } else {
            trainParallel(threadNumber)
        }
    }

    /**
     * 单线程训练
     */
    private fun trainOneThread(): Perceptron {
        val model = buildPerceptronModel(
                featureSet, labelCount
        )
        //应该是权重的总和 最后要平均？
        val total = DoubleArray(model.parameter.size)
        //时间戳 每个正确预测的存活时间
        val timestamp = IntArray(model.parameter.size)
        var current = 0//第N次更新

        for (k in 1..maxIter) {
            val t1 = System.currentTimeMillis()
            println("\n#ITER $k/$maxIter")

            System.out.print("Process 0%")
            var per = 0
            trainSource.forEach {
                current++

                model.update(it, total, timestamp, current)

                per++

                if (per % 100 == 0) {
                    System.out.print("\rProcess ${"%.2f".format((per * 100.0 / trainSource.size))}%")
                }
            }
            System.out.print("\r")
            val t2 = System.currentTimeMillis()

            // 运行评估
            evaluateScript.run(model)

            println("use ${t2 - t1} ms\n")
        }
        //
        model.average(total, timestamp, current)
        return model
    }

    private fun trainParallel(threadNumber: Int): Perceptron {
        // val size = featureSet.size() * labelCount
        val modelArray = Array(threadNumber) {
            buildPerceptronModel(featureSet, labelCount)
        }

        val executor = Executors.newFixedThreadPool(threadNumber)

        val parts = trainSource.chunked((trainSource.size * 1.0 / threadNumber).toInt() + 1)

        for (k in 1..maxIter) {
            println("#ITER $k/$maxIter")
            val t1 = System.currentTimeMillis()

            var countDownLatch = CountDownLatch(threadNumber)
            for (s in 0 until threadNumber) {
                executor.submit {
                    try {
                        val list = parts[s]
                        var count = 0

                        if (s == 0) {
                            System.out.print("Process 0%")
                        }

                        list.forEach { d ->
                            modelArray[s].update(d)
                            count++

                            if (s == 0 && count % 2000 == 0) {
                                System.out.print("\rProcess ${"%.2f".format((count * 100.0 / list.size))}%")
                            }
                        }

                        if (s == 0) {
                            System.out.print("\r")
                        }

                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }
            countDownLatch.await()

            //把第二个开始的模型的参数全部和第一个平均
            val first = modelArray.first().parameter
            for (i in 1 until modelArray.size) {
                val the = modelArray[i].parameter
                for (j in 0 until first.size) {
                    first[j] += the[j]
                }
            }

            for (j in 0 until first.size) {
                first[j] = first[j] / modelArray.size
            }

            evaluateScript.run(modelArray.first())

            val t2 = System.currentTimeMillis()

            println("use ${t2 - t1} ms\n")

        }

        executor.shutdownNow()


        return buildPerceptronModel(
                featureSet,
                labelCount,
                modelArray.first().parameter
        )

    }
}

//
//class PerceptronModelForPos(featureSet: FeatureSet, labelCount: Int, parameter: FloatArray)
//    : PerceptronModel(featureSet, labelCount, parameter) {
//
//    companion object {
//
//        fun load(parameterBin: InputStream, featureBin: InputStream, featureDat: Boolean): PerceptronModelForPos {
//
//            return if (featureDat) {
//                load(parameterBin, featureBin, null)
//            } else {
//                load(parameterBin, null, featureBin)
//            }
//        }
//
//        fun load(parameterFile: File, featureBin: File?, featureText: File?): PerceptronModelForPos {
//            return load(parameterFile.inputStream().buffered(),
//                    featureBin?.inputStream()?.buffered(),
//                    featureText?.inputStream()?.buffered()
//            )
//        }
//
//        fun load(dir: File): PerceptronModel {
//            fun loadIfExit(fname: String): File? {
//                val f = File(dir, fname)
//                return if (f.exists()) {
//                    f
//                } else {
//                    null
//                }
//            }
//
//            return load(
//                    File(dir, "parameter.bin"),
//                    loadIfExit("feature.dat"),
//                    loadIfExit("feature.txt")
//            )
//        }
//
//        fun load(parameterBin: InputStream, featureBin: InputStream?, featureText: InputStream?): PerceptronModelForPos {
//
//            var labelCount = 0
//            var parameter = FloatArray(0)
//            parameterBin.use { x ->
//                val input = DataInputStream(x)
//                labelCount = input.readInt()
//
//                val pSize = input.readInt()
//                parameter = FloatArray(pSize)
//                for (i in 0 until pSize) {
//                    parameter[i] = input.readFloat()
//                }
//            }
//
//            val fs = if (featureBin != null) {
//                if (featureText != null) {
//                    FeatureSet.read(featureBin, featureText)
//                } else {
//                    FeatureSet.read(featureBin)
//                }
//            } else {
//                if (featureText != null) {
//                    FeatureSet.read(featureText)
//                } else {
//                    throw RuntimeException()
//                }
//            }
//
//            return PerceptronModelForPos(fs, labelCount, parameter)
//        }
//    }
//
//    constructor(featureSet: FeatureSet, labelCount: Int) :
//            this(featureSet, labelCount, FloatArray(featureSet.size() * labelCount))
//
//    override fun decode(featureSequence: List<IntArrayList>, guessLabel: IntArray): Double {
//        val bos = labelCount
//        val sentenceLength = featureSequence.size
//        val labelSize = labelCount
//
//        val preMatrix = IntArray(sentenceLength * labelSize)
//
////        val scoreMatrix = Array(2) { DoubleArray(labelSize) }
//        //上一回的状态
//        var scoreMLast = DoubleArray(labelSize)
//        var scoreMNow = DoubleArray(labelSize)
//
//        //first
//        val firstFeature = featureSequence[0]
//        firstFeature[firstFeature.size() - 1] = bos
//
//        for (j in 0 until labelCount) {
//            preMatrix[j] = j
//            val score = score(firstFeature, j)
//            scoreMLast[j] = score
//        }
//
//
//        for (i in 1 until sentenceLength) {
//            val allFeature = featureSequence[i]
////            val transitionFeatureIndex = allFeature.size() - 1
//            val base = i * labelSize
//
//
//            // i 对应的这个特征，我想选取最有可能的top4，参与计算
//
//            val baseScoreArray = DoubleArray(labelCount)
//            for (curLabel in 0 until labelCount) {
//                val baseScore = scoreBase(allFeature,curLabel)
//                baseScoreArray[curLabel] = baseScore
//            }
//
//            val sorted = baseScoreArray.zip(0 until labelCount ).sortedByDescending { it.first }
//            //找出最大的TOP3
//            val topThree = intArrayOf(sorted[0].second,sorted[1].second,sorted[2].second)
//
//            for (curLabel in topThree) {
//
//                var maxScore = MaxScore
//
//                //因为score(allFeature, curLabel) 中除了最后特征位置不一样，其他位置的得分都是重复的
//                val baseScore = baseScoreArray[curLabel]
//
//                for (preLabel in 0 until labelCount) {
//
//                    // allFeature[transitionFeatureIndex] = preLabel
//
////                    val score = score(allFeature, curLabel)
//                    val score = baseScore + parameter[preLabel*labelSize+curLabel]
//
//                    val curScore = scoreMLast[preLabel] + score
//
//                    if (maxScore < curScore) {
//                        maxScore = curScore
//                        preMatrix[base + curLabel] = preLabel
//                        scoreMNow[curLabel] = maxScore
//                    }
//                }
//            }
//
//            val temp = scoreMLast
//            scoreMLast = scoreMNow
//            scoreMNow = temp
//        }
//
//        //此时scoreM0 肯定是最后一个
//        var maxIndex = 0
//        var maxScore = scoreMLast[0]
//
//        for (index in 1 until labelCount) {
//            val x = scoreMLast[index]
//            if (maxScore < x) {
//                maxIndex = index
//                maxScore = x
//            }
//        }
//
//        var k = (sentenceLength - 1) * labelCount
//        for (i in sentenceLength - 1 downTo 0) {
//            guessLabel[i] = maxIndex
//            maxIndex = preMatrix[k + maxIndex]
//            k -= labelCount
//        }
//
//        return maxScore
//    }
//
//
//    private fun score(featureVector: IntArrayList, currentTag: Int): Double {
//        var score = 0.0
//
//        val buffer = featureVector.buffer
//        for (i in 0 until featureVector.size()) {
//            val index = buffer[i]
//            if (index < 0 || index >= featureSetSize) {
//                // do nothing
//            } else {
//                score += parameter[index * labelCount + currentTag]
//            }
//        }
//        return score
//    }
//
//    private fun findTop(list:DoubleArray) : IntArray{
//        val top = IntArray(3)
//
//
//
//        return top
//    }
//
//    private fun scoreBase(featureVector: IntArrayList, currentTag: Int): Double {
//        var score = 0.0
//
//        val buffer = featureVector.buffer
//        for (i in 0 until featureVector.size()-1) {
//            val index = buffer[i]
//            if (index < 0 || index >= featureSetSize) {
//                // do nothing
//            } else {
//                score += parameter[index * labelCount + currentTag]
//            }
//        }
//        return score
//    }
//}
//
//
//class PerceptronTrainerForPos(featureSet: FeatureSet, labelCount: Int, trainSource: List<TrainSample>, evaluateScript: EvaluateRunner, maxIter: Int)
//    : PerceptronTrainer(featureSet, labelCount, trainSource, evaluateScript, maxIter) {
//    override fun buildPerceptronModel(featureSet: FeatureSet, labelCount: Int): PerceptronModel {
//        return PerceptronModelForPos(
//                featureSet, labelCount
//        )
//    }
//
//    override fun buildPerceptronModel(featureSet: FeatureSet, labelCount: Int, parameter: FloatArray): PerceptronModel {
//        return PerceptronModelForPos(
//                featureSet, labelCount, parameter
//        )
//    }
//
//}


/**
 * 求最大Top K
 * 内部是小顶堆
 */
class TopIntMaxK(val k: Int) {

    val heap = DoubleArray(k)
    val idIndex = IntArray(k) { -1 }

    var size = 0

    fun clear() {
        size = 0
        heap.fill(0.0)
        idIndex.fill(-1)
    }

    fun push(id: Int, score: Double) {
        if (size < k) {
            heap[size] = score
            idIndex[size] = id
            size++

            if (size == k) {
                buildMinHeap()
            }
        } else {
            // 如果这个数据大于最下值，那么有资格进入
            if (score > heap[0]) {
                heap[0] = score
                idIndex[0] = id

                mintopify(0)
            }
        }
    }

    fun resort(): java.util.ArrayList<Pair<Int, Double>> {
        val top = Math.min(k, size)
        val list = java.util.ArrayList<Pair<Int, Double>>(top)

        heap.forEachIndexed { index, fl ->
            if (size < k && index < size) {
                list += idIndex[index] to fl
            } else {
                list += idIndex[index] to fl
            }

        }
        list.sortByDescending { it.second }
        return list
    }

    private fun buildMinHeap() {
        for (i in k / 2 - 1 downTo 0) {
            mintopify(i)// 依次向上将当前子树最大堆化
        }
    }

    /**
     * 让heap数组符合堆特性
     * @param i
     */
    private fun mintopify(i: Int) {
        val l = 2 * i + 1
        val r = 2 * i + 2
        var min: Int

        if (l < k && heap[l] < heap[i])
            min = l
        else
            min = i

        if (r < k && heap[r] < heap[min])
            min = r

        if (min == i || min >= k) {
            // 如果largest等于i说明i是最大元素
            // largest超出heap范围说明不存在比i节点大的子女
            return
        }

        swap(i, min)
        mintopify(min)
    }

    private fun swap(i: Int, j: Int) {
        val tmp = heap[i]
        heap[i] = heap[j]
        heap[j] = tmp

        val tmp2 = idIndex[i]
        idIndex[i] = idIndex[j]
        idIndex[j] = tmp2
    }
}
/**
 * Top K 最小值。
 */
class TopIntMinK(val k: Int) {

    val heap = FloatArray(k)
    val idIndex = IntArray(k) { -1 }

    var size = 0

    fun push(id: Int, score: Float) {
        if (size < k) {
            heap[size] = score
            idIndex[size] = id
            size++

            if (size == k) {
                buildMinHeap()
            }
        } else {
            // 如果这个数据小于最大值，那么有资格进入
            if (score < heap[0]) {
                heap[0] = score
                idIndex[0] = id

                topify(0)
            }
        }
    }

    fun result(): java.util.ArrayList<Pair<Int, Float>> {
        val top = Math.min(k, size)
        val list = java.util.ArrayList<Pair<Int, Float>>(top)

        for (i in 0 until top) {
            list += idIndex[i] to heap[i]
        }

        list.sortBy { it.second }
        return list
    }

    private fun buildMinHeap() {
        for (i in k / 2 - 1 downTo 0) {
            topify(i)// 依次向上将当前子树最大堆化
        }
    }

    fun topify(i: Int) {
        val l = 2 * i + 1
        val r = 2 * i + 2
        var max: Int

        if (l < k && heap[l] > heap[i])
            max = l
        else
            max = i

        if (r < k && heap[r] > heap[max]) {
            max = r
        }

        if (max == i || max >= k)
        // 如果largest等于i说明i是最大元素
        // largest超出heap范围说明不存在比i节点大的子女
            return

        swap(i, max)
        topify(max)
    }

    private fun swap(i: Int, j: Int) {
        val tmp = heap[i]
        heap[i] = heap[j]
        heap[j] = tmp

        val tmp2 = idIndex[i]
        idIndex[i] = idIndex[j]
        idIndex[j] = tmp2
    }
}

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
class Perceptron(
        private var featureSet: FeatureSet,
        private val labelCount: Int,
        var parameter: FloatArray
) : PerceptronModel {

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
            if (goldIndex[i] == predictIndex[i])
                continue
            else {
                parameter[goldIndex[i]]++
                if (predictIndex[i] >= 0 && predictIndex[i] < parameter.size)
                    parameter[predictIndex[i]]--
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
     * 不裁剪特征.修改flaot的精度
     *
     */
    override fun optimizeForZip() {
        for (i in 0 until parameter.size) {
            parameter[i] = (parameter[i] * 100000).toInt() / 100000.0f
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
            val heap = TopMinK(k - filterSet.size)

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
            if (!filterSet.contains(index)) {
                newFeatureList += s
                System.arraycopy(parameter, index * labelCount, newParameter, cc * labelCount, labelCount)
                cc++
            }
        }

        parameter = newParameter
        this.featureSet = FeatureSet(DATArrayIndex(newFeatureList), newFeatureList)

        featureSetSize = this.featureSet.size()
        parameterSize = parameter.size

        optimizeForZip()
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

        fun load(parameterBin: InputStream, featureBin: InputStream, featureDat: Boolean): Perceptron {

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

            val fs = if (featureDat) {
                FeatureSet.read(featureBin)
            } else {
                FeatureSet.readFromText(featureBin)
            }

            return Perceptron(fs, labelCount, parameter)
        }
    }

    /**
     * viterbi
     */
    override fun decode(featureSequence: List<IntArrayList>, guessLabel: IntArray): Double {
        val bos = labelCount
        val sentenceLength = featureSequence.size
        val labelSize = labelCount

        val preMatrix = Array(sentenceLength) { IntArray(labelSize) }
        val scoreMatrix = Array(2) { DoubleArray(labelSize) }

        for (i in 0 until sentenceLength) {

            val _i = i and 1//偶数得0 奇数得1
            val _i_1 = 1 - _i//偶数得1 奇数得0
            val allFeature = featureSequence[i]
            val transitionFeatureIndex = allFeature.size() - 1
            if (0 == i) {
                allFeature[transitionFeatureIndex] = bos
                for (j in 0 until labelCount) {
                    preMatrix[0][j] = j

                    val score = score(allFeature, j)

                    scoreMatrix[0][j] = score
                }
            } else {
                for (curLabel in 0 until labelCount) {

                    var maxScore = Integer.MIN_VALUE.toDouble()

                    for (preLabel in 0 until labelCount) {

                        allFeature[transitionFeatureIndex] = preLabel
                        val score = score(allFeature, curLabel)

                        val curScore = scoreMatrix[_i_1][preLabel] + score

                        if (maxScore < curScore) {
                            maxScore = curScore
                            preMatrix[i][curLabel] = preLabel
                            scoreMatrix[_i][curLabel] = maxScore
                        }
                    }
                }

            }
        }

        var maxIndex = 0
        var maxScore = scoreMatrix[sentenceLength - 1 and 1][0]

        for (index in 1 until labelCount) {
            if (maxScore < scoreMatrix[sentenceLength - 1 and 1][index]) {
                maxIndex = index
                maxScore = scoreMatrix[sentenceLength - 1 and 1][index]
            }
        }

        for (i in sentenceLength - 1 downTo 0) {
            //guessLabel[i] = labelSet[maxIndex]
            guessLabel[i] = maxIndex
            maxIndex = preMatrix[i][maxIndex]
        }

        return maxScore
    }

    private fun score(featureVector: IntArrayList, currentTag: Int): Double {
        var score = 0.0

        val buffer = featureVector.buffer
        for (i in 0 until featureVector.size()) {
            val index = buffer[i]
            if (index < 0 || index >= featureSetSize) {
                // do nothing
            } else {
                val temp = index * labelCount + currentTag
                if (temp >= 0 && temp < parameterSize) {
                    score += parameter[temp]
                }
            }
        }
        return score
    }
}


/**
 * 训练器
 */
class PerceptronTrainer(
        private val featureSet: FeatureSet,
        private val labelCount: Int,
        private val trainSource: List<TrainSample>,
        private val evaluateScript: EvaluateRunner,
        private val maxIter: Int) {

    /**
     * 默认多线程训练。
     * @param threadNumber 线程数。threadNumber=1 时平均感知机
     */
    @JvmOverloads
    fun train(threadNumber: Int = Runtime.getRuntime().availableProcessors() - 1): PerceptronModel {
        return if (threadNumber == 1) {
            trainOneThread()
        } else {
            trainParallel(threadNumber)
        }
    }

    /**
     * 单线程训练
     */
    private fun trainOneThread(): PerceptronModel {
        val model = Perceptron(
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
        model.average(total, timestamp, current)
        return model
    }

    private fun trainParallel(threadNumber: Int): PerceptronModel {
        val size = featureSet.size() * labelCount
        val modelArray = Array(threadNumber) {
            Perceptron(featureSet, labelCount, FloatArray(size))
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

                            if (s == 0 && count % 100 == 0) {
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


        return Perceptron(
                featureSet,
                labelCount,
                modelArray.first().parameter
        )

    }
}
package com.mayabot.nlp.perceptron

import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors


/**
 * 感知机训练的样本
 * featureMatrix中的IntArray最后多一位是留个转移特征使用
 */
class TrainSample(
        val featureMatrix: List<IntArray>,
        val label: IntArray) {
    val size = featureMatrix.size
}


class Labels(val goldFeature: IntArray, val predFeature: IntArray)


/**
 * 感知机模型
 */
class CostumisedPerceptron(
        private val featureSet: FeatureSet,
        private val labelCount: Int,
        var parameter: FloatArray
) : PerceptronModel {

    constructor(featureSet: FeatureSet, labelCount: Int) :
            this(featureSet, labelCount, FloatArray(featureSet.size() * labelCount))

    override fun featureSet() = featureSet

    fun update(data: TrainSample, total: DoubleArray, timestamp: IntArray, current: Int) {
        val length = data.size
        val guessLabel = IntArray(length)
        decode(data.featureMatrix, guessLabel)
        for (i in 0 until length) {
            val labels = featureToLabel(data,guessLabel,i)
            updateParameter(labels.goldFeature, labels.predFeature, total, timestamp, current)
        }
    }

    fun update(data: TrainSample) {
        val length = data.size
        val guessLabel = IntArray(length)
        decode(data.featureMatrix, guessLabel)
        for (i in 0 until length) {
            val labels = featureToLabel(data,guessLabel,i)
            updateOnline(labels.goldFeature, labels.predFeature)
        }
    }

    private fun featureToLabel(data: TrainSample, guessLabel: IntArray, i: Int): Labels {
        //序号代替向量 替代数组
        val featureVector = data.featureMatrix[i]
        //正确标签
        val goldFeature = IntArray(featureVector.size)
        //预测标签
        val predFeature = IntArray(featureVector.size)
        for (j in 0 until featureVector.size - 1) {
            //特征 -> 对应标签
            goldFeature[j] = featureVector[j] * labelCount + data.label[i]
            predFeature[j] = featureVector[j] * labelCount + guessLabel[i]
        }
        goldFeature[featureVector.size - 1] = (if (i == 0) labelCount else data.label[i - 1]) * labelCount + data.label[i]
        predFeature[featureVector.size - 1] = (if (i == 0) labelCount else guessLabel[i - 1]) * labelCount + guessLabel[i]
        return Labels(goldFeature, predFeature)
    }

    private fun updateParameter(goldIndex: IntArray, predictIndex: IntArray, total: DoubleArray, timestamp: IntArray, current: Int) : Boolean{
        if (goldIndex.contentEquals( predictIndex)) return false
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

    private fun record(index: Int, value: Float, total: DoubleArray, timestamp: IntArray, current: Int) {
        val passed = current - timestamp[index]
        total[index] += passed * parameter[index].toDouble() //权重乘以该纬度经历时间
        parameter[index] += value
        timestamp[index] = current
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

    fun average(total: DoubleArray, timestamp: IntArray, current: Int) {
        for (i in 0 until parameter.size) {
            val pass = current.toFloat() - timestamp[i].toFloat()
            val totali = total[i].toFloat()
            parameter[i] = (totali + pass * parameter[i]) / current
        }
    }

//    fun compress(ratio: Double, vararg threshold: Double) {
//        if (ratio <= 0 || ratio >= 1) {
//            throw IllegalArgumentException("压缩比必须介于 0 和 1 之间")
//        }
//        val k = (ratio * featureSet.size()).toInt()
//        val heap = TopMaxK<String>(k, String::class.java)
//        val temp = if (threshold.isEmpty()) 1e-3 else threshold[0]
//
//        featureMap.featureMap.forEach {
//            var score = 0f
//
//            for (i in 0 until labelCount) {
//                //
//                try {
//                    score += Math.abs(parameter[it.value * labelCount + i])
//                } catch (e: IndexOutOfBoundsException) {
//                }
//            }
//            if (score > temp) {
//                if (it.value >= labelCount)
//                    heap.push(it.key, score)
//            }
//        }
//
//        var para = ArrayList<Float>()
//        val mapCompressed = ObjectIntHashMap<String>()
//        //先写死看看
//        // TODO
//        mapCompressed.put("BL=B", 0)
//        mapCompressed.put("BL=M", 1)
//        mapCompressed.put("BL=E", 2)
//        mapCompressed.put("BL=S", 3)
//        mapCompressed.put("BL=_BL_", 4)
//
//        for (i in 0 until 20) {
//            para.add(parameter[i])
//        }
//
//        heap.result().forEach {
//            mapCompressed.put(it.first, mapCompressed.size() + 1)
//            for (i in 0 until labelCount) {
//                para.add(parameter[featureMap.featureMap[it.first] * labelCount + i])
//            }
//        }
//        parameter = para.toFloatArray()
//        featureMap.featureMap = mapCompressed
//    }

    override fun save(file: File) {
        val outBuffer = DataOutputStream(file.outputStream().buffered())
        outBuffer.use {
            val dout = it
            dout.writeInt(labelCount)

            featureSet.save(dout)

            dout.writeInt(parameter.size)
            parameter.forEach { w -> dout.writeFloat(w) }

            dout.flush()
        }
    }

    companion object {
        fun load(file: File): CostumisedPerceptron {
            val input = DataInputStream(file.inputStream().buffered())
            val ltc = input.readInt()

            val fs = FeatureSet.read(input)

            val pSize = input.readInt()

            val parameter = FloatArray(pSize)

            for (i in 0 until pSize) {
                parameter[i] = input.readFloat()
            }

            return CostumisedPerceptron(fs, ltc, parameter)
        }
    }

    /**
     * viterbi
     */
    override fun decode(featureSequence: List<IntArray>, guessLabel: IntArray): Double {
        val bos = labelCount
        val sentenceLength = featureSequence.size
        val labelSize = labelCount

        val preMatrix = Array(sentenceLength) { IntArray(labelSize) }
        val scoreMatrix = Array(2) { DoubleArray(labelSize) }

        for (i in 0 until sentenceLength) {

            val _i = i and 1//偶数得0 奇数得1
            val _i_1 = 1 - _i//偶数得1 奇数得0
            val allFeature = featureSequence[i]
            val transitionFeatureIndex = allFeature.size - 1
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

    private fun score(featureVector: IntArray, currentTag: Int): Double {
        var score = 0.0
        for (index in featureVector) {
            if (index == -1) {
                continue
            } else if (index < -1 || index >= featureSet.size()) {
                throw IllegalArgumentException("在打分时传入了非法的下标")
            } else {
                val temp = index * labelCount + currentTag
                try {
                    score += parameter[temp]   // 其实就是特征权重的累加
                } catch (e: ArrayIndexOutOfBoundsException) {

                }
            }
        }
        return score
    }
}


/**
 * 训练器
 */
class SPTrainer(
        private val featureSet: FeatureSet,
        private val labelCount: Int) {

    /**
     * 训练一轮
     */
    fun train(source: Iterable<TrainSample>, maxIter: Int): PerceptronModel {
        val model = CostumisedPerceptron(
                featureSet, labelCount
        )
        //应该是权重的总和 最后要平均？
        val total = DoubleArray(model.parameter.size)
        //时间戳 每个正确预测的存活时间
        val timestamp = IntArray(model.parameter.size)
        var current = 0//第N次更新

        for (iterate in 0 until maxIter) {
            val t1 = System.currentTimeMillis()
            println("Start train ${iterate + 1}/${maxIter} .")
            source.forEach {
                current++
                model.update(it, total, timestamp, current)
            }
            val t2 = System.currentTimeMillis()
            println("End train ${iterate + 1}/${maxIter} . use ${t2 - t1} ms")
        }
        model.average(total, timestamp, current)
        return model
    }

    fun trainParallel(source: Iterable<TrainSample>, threadNumber: Int): PerceptronModel {
        val size = featureSet.size() * labelCount
        val modelArray = Array(threadNumber) {
            CostumisedPerceptron(featureSet, labelCount, FloatArray(size))
        }

        val executor = Executors.newFixedThreadPool(threadNumber)

        val parts = Lists.newArrayList(Iterables.partition(source, threadNumber))

        var countDownLatch = CountDownLatch(threadNumber)
        for (s in 0 until threadNumber) {
            executor.submit {
                try {
                    val list = parts[s]
                    list.forEach { d ->
                        modelArray[s].update(d)
                    }
                } finally {
                    countDownLatch.countDown()
                }
            }
        }
        countDownLatch.await()
        executor.shutdownNow()

        val result = FloatArray(size) { 0f }
        for (i in 0 until size) {
            for (j in 0 until threadNumber) {
                result[i] = modelArray[j].parameter[i]
            }
            result[i] = result[i] / threadNumber
        }

        return CostumisedPerceptron(
                featureSet,
                labelCount,
                result
        )

    }
}
package com.mayabot.nlp.perceptron

import com.mayabot.nlp.algorithm.TopIntMinK
import com.mayabot.nlp.algorithm.collection.dat.DoubleArrayTrie
import java.io.DataOutputStream
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * 感知机模型实现。
 *
 * parameter是一个很长的float数组。
 *
 * [ [F0],[F1],[label_0,label_1,label_x]..[FN]] ]
 * Fn 表示一个特征在每个label的权重。
 * 假设Label是4个，特征总数是10000.
 * 那么逻辑上存在4个长度为1万的float向量，每一个向量代表对于label的感知机参数权重。
 * 也是是存在四个感知机。
 *
 * 一个Input，包含了多个特征，这些特征在每个每个感知机里面对应的位置之和，就是每个label的score了。
 * 在不考虑转移概率和全局最后的情况下，只要选择一个当前最大的得分即可。
 * 如果要考虑全局最优那么需要viterbi解码。
 *
 * FeatureSet中Feature的key是按照顺序排序的，假设实际数据的特征数量是1000，label是4，那么在前面插入4+1个特殊特征。
 *
 *
 * @author jimichan
 */
class PerceptronModelImpl(
        private val featureSet: FeatureSet,
        val labelCount: Int,
        var parameter: FloatArray
) : PerceptronModel {

    override fun parameterAt(index: Int) = parameter[index]

    override fun decodeQuickMode(quick: Boolean) {
        decodeQuickModel = quick
    }

    private val MaxScore = Integer.MIN_VALUE.toDouble()
    private var decodeQuickModel = false

    private val labelLimitInParameter = (labelCount + 1) * labelCount

    constructor(featureSet: FeatureSet, labelCount: Int) :
            this(featureSet, labelCount, FloatArray(featureSet.size() * labelCount))

    fun parameterSize() = parameter.size

    override fun featureSet() = featureSet

    override fun makeSureParameter(featureId: Int) {

        var newSize = (featureId + 1) * labelCount

        if (newSize > parameter.size) {
            val pad = 1000
            newSize = (featureId + pad) * labelCount
            parameter = Arrays.copyOf(parameter, newSize)
        }
    }

    /**
     * 单线程训练时调用.
     * 平均感知机
     */
    fun update(data: TrainSample, total: DoubleArray, timestamp: IntArray, current: Int) {
        val length = data.size
        val guessLabel = IntArray(length)
        decode(data.featureSequence, guessLabel)
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
        decode(data.featureSequence, guessLabel)
        for (i in 0 until length) {
            val labels = featureToLabel(data, guessLabel, i)
            updateOnline(labels.goldFeature, labels.predFeature)
        }
    }


    override fun onlineLearn(data: TrainSample) {
        val guessLabel = IntArray(data.size)
        decode(data.featureSequence, guessLabel)
        if (Arrays.equals(guessLabel, data.label)) {
            return
        }

        var over = 0
        for (i in 0 until 10) {
            val eq = updateForOnlineLearnInner(data, 1f)
            if (eq) {
                over++
                if (over > 1) {
                    return
                }
            }
        }

    }

    private fun updateForOnlineLearnInner(data: TrainSample, step: Float): Boolean {
        val length = data.size
        val guessLabel = IntArray(length)
        decode(data.featureSequence, guessLabel)

        for (i in 0 until length) {
            val labels = featureToLabel(data, guessLabel, i)
            updateOnline2(labels.goldFeature, labels.predFeature, step)
        }


        decode(data.featureSequence, guessLabel)
        return Arrays.equals(guessLabel, data.label)
    }

    private fun updateOnline2(goldIndex: IntArray, predictIndex: IntArray, step: Float) {
        for (i in goldIndex.indices) {

            val xii = predictIndex[i]
            if (goldIndex[i] == xii)
                continue
            else {
                val toAdd = goldIndex[i]
                if (toAdd > labelLimitInParameter) {
                    parameter[toAdd] += step
                }

                if (xii >= 0 && xii < parameter.size && xii > labelLimitInParameter) {
                    parameter[xii] -= step
                } else {
                    //throw IllegalArgumentException("更新参数时传入了非法的下标")
                }
            }
        }
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


    private fun featureToLabel(data: TrainSample, guessLabel: IntArray, i: Int): Labels {
        //序号代替向量 替代数组
        val featureVector = data.featureSequence[i]
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

    private fun record(index: Int, value: Float, total: DoubleArray, timestamp: IntArray, current: Int) {
        val passed = current - timestamp[index]
        total[index] += passed * parameter[index].toDouble() //权重乘以该纬度经历时间
        parameter[index] += value
        timestamp[index] = current
    }


    fun average(total: DoubleArray, timestamp: IntArray, current: Int) {
        val cf = current.toFloat()
        for (i in 0 until parameter.size) {
            val pass = cf - timestamp[i].toFloat()
            val totali = total[i].toFloat()
            parameter[i] = (totali + pass * parameter[i]) / cf
        }
    }

    /**
     * 模型压缩
     * @param ratio 压缩比，如0.2，那么就是去掉0.2的特征
     */
    override fun compress(ratio: Double, threshold: Double): PerceptronModel {
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

        return PerceptronModelImpl(FeatureSet(DoubleArrayTrie(newFeatureList), newFeatureList),
                labelCount,
                newParameter
        )
    }

    /**
     * 二进制格式：
     * parameter.bin => [labelCount][parameter.size][parameter array]
     * feature.dat => 特征集合
     */
    override fun save(dir: File) {
        dir.mkdirs()
        File(dir, "parameter.bin").outputStream().buffered().use {
            val out = DataOutputStream(it)
            out.writeInt(labelCount)
            out.writeInt(parameter.size)
            parameter.forEach { w -> out.writeFloat(w) }

            out.flush()
        }

        featureSet.save(
                File(dir, "feature.dat"),
                File(dir, "feature.txt"))
    }

    /**
     * 快速解码。不考虑转移概率，比较适合词性模型解码。
     */
    private fun decodeQuick(featureSequence: FeatureVectorSequence, guessLabel: IntArray) {

        var index = 0

        val parameterArray = parameter

        for (feature in featureSequence) {

            val buffer = feature.buffer
            val sizeM1 = feature.size() - 1

            var maxScore = Float.MIN_VALUE

            //这里修改为-1，一定会有什么特征都不存在的。
            var maxIndex = -1
            if(sizeM1 != 0) {
                for (label in 0 until labelCount) {

                    var score = 0.0f

                    for (i in 0 until sizeM1) {
                        score += parameterArray[buffer[i] * labelCount + label]
                    }

                    if (score > maxScore) {
                        maxIndex = label
                        maxScore = score
                    }
                }
            }

            guessLabel[index++] = maxIndex
        }

    }

    /**
     * viterbi
     */
    override fun decode(featureSequence: FeatureVectorSequence, guessLabel: IntArray) {
        val parameter = parameter

        //快速模式，不考虑转移，只适用于词性标注类型的任务
        if (decodeQuickModel) {
            decodeQuick(featureSequence, guessLabel)
            return
        }

        val sentenceLength = featureSequence.size

        val preMatrix = IntArray(sentenceLength * labelCount)

        // 二阶维特比解码算法，目的是找出每个选择带来权重之后全局最大化。

        //上一回的状态
        var scoreMLast = DoubleArray(labelCount)
        var scoreMNow = DoubleArray(labelCount)


        //first
        val firstFeature = featureSequence[0]

        val bos = labelCount
        val bosBase = bos * labelCount
        for (j in 0 until labelCount) {
            preMatrix[j] = j
            val score = scoreBase(firstFeature, j) + parameter[bosBase + j]
            scoreMLast[j] = score
        }


        for (i in 1 until sentenceLength) {

            val allFeature = featureSequence[i]
            val base = i * labelCount

            for (curLabel in 0 until labelCount) {

                var maxScore = MaxScore

                // baseScore的计算提取到下面for循环之外来，避免重复计算，提高性能
                val baseScore = scoreBase(allFeature, curLabel)

                for (preLabel in 0 until labelCount) {
                    val curScore = scoreMLast[preLabel] + baseScore + parameter[preLabel * labelCount + curLabel]

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


        //此时scoreM0 肯定是最后一个
        // 找出scoreMLast中最大的score和index
        var maxIndex = 0
        var maxScore = scoreMLast[0]

        for (index in 1 until labelCount) {
            val x = scoreMLast[index]
            if (maxScore < x) {
                maxIndex = index
                maxScore = x
            }
        }

        //回填每个位置的猜测的结构。
        var k = (sentenceLength - 1) * labelCount
        for (i in sentenceLength - 1 downTo 0) {
            guessLabel[i] = maxIndex
            maxIndex = preMatrix[k + maxIndex]
            k -= labelCount
        }

    }

    private fun scoreBase(featureVector: FeatureVector, currentTag: Int): Double {
        val parameter = parameter
        var score = 0.0

        val buffer = featureVector.buffer
        for (i in 0 until featureVector.size() - 1) {
            val index = buffer[i]
            score += parameter[index * labelCount + currentTag]
        }

        return score
    }

    class Labels(val goldFeature: IntArray, val predFeature: IntArray)
}


/**
 * 感知机训练的样本
 * FeatureVector最后多一位是留个转移特征使用
 */
class TrainSample(
        val featureSequence: FeatureVectorSequence,
        val label: IntArray) {

    val size = featureSequence.size
}
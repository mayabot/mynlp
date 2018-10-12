package com.mayabot.nlp.perceptron.model

import com.carrotsearch.hppc.ObjectIntHashMap
import com.mayabot.nlp.algorithm.TopMaxK
import com.mayabot.nlp.collection.dat.DoubleArrayTrie
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder
import com.mayabot.nlp.perceptron.FeatureExtractor
import com.mayabot.nlp.perceptron.FeatureMap
import com.mayabot.nlp.perceptron.PerceptronModel
import com.mayabot.nlp.perceptron.lmpl.FeaturedSequenceLabel
import com.mayabot.nlp.perceptron.lmpl.StructuredPerceptronTrainer
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

class CostumisedPerceptron<T>(
        val featureMap: FeatureMap,
        var parameter: FloatArray,
        private val featureExtractor: FeatureExtractor<T>) : PerceptronModel<T> {


    fun update(data: FeaturedSequenceLabel,total: DoubleArray, timestamp: IntArray, current: Int){
        val length = data.size
        val guessLabel = IntArray(length)
        viterbiDecode(data, guessLabel)
        for (i in 0 until length) {
            val labels = featureToLabel(data,guessLabel,i)
            updateParameter(labels.goldFeature, labels.predFeature, total, timestamp, current)
        }
    }

    fun update(data: FeaturedSequenceLabel){
        val length = data.size
        val guessLabel = IntArray(length)
        viterbiDecode(data, guessLabel)
        for (i in 0 until length) {
            val labels = featureToLabel(data,guessLabel,i)
            updateOnline(labels.goldFeature, labels.predFeature)
        }
    }

    private fun featureToLabel(data: FeaturedSequenceLabel, guessLabel: IntArray,i: Int): Labels {
        //序号代替向量 替代数组
        val featureVector = data.featureMatrix[i]
        //正确标签
        val goldFeature = IntArray(featureVector.size)
        //预测标签
        val predFeature = IntArray(featureVector.size)
        for (j in 0 until featureVector.size - 1) {
            //特征 -> 对应标签
            goldFeature[j] = featureVector[j] * featureMap.tagSize()+ data.label[i]
            predFeature[j] = featureVector[j] * featureMap.tagSize() + guessLabel[i]
        }
        goldFeature[featureVector.size - 1] = (if (i == 0) featureMap.tagSize() else data.label[i - 1]) * featureMap.tagSize() + data.label[i]
        predFeature[featureVector.size - 1] = (if (i == 0) featureMap.tagSize() else guessLabel[i - 1]) * featureMap.tagSize() + guessLabel[i]
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

    fun compress(ratio: Double, vararg threshold: Double) {
        if (ratio <= 0 || ratio >= 1) {
            throw IllegalArgumentException("压缩比必须介于 0 和 1 之间")
        }
        val k = (ratio * featureMap.featureSize()).toInt()
        val heap = TopMaxK<String>(k, String::class.java)
        val temp = if (threshold.isEmpty()) 1e-3 else threshold[0]

        featureMap.featureMap.forEach {
            var score = 0f

            for (i in 0 until featureMap.tagSize()) {
                //
                try {
                    score += Math.abs(parameter[it.value * featureMap.tagSize() + i])
                } catch (e: IndexOutOfBoundsException) {
                }
            }
            if (score > temp) {
                if (it.value >= featureMap.tagSize())
                    heap.push(it.key, score)
            }
        }

        var para = ArrayList<Float>()
        val mapCompressed = ObjectIntHashMap<String>()
        //先写死看看
        mapCompressed.put("BL=B", 0)
        mapCompressed.put("BL=M", 1)
        mapCompressed.put("BL=E", 2)
        mapCompressed.put("BL=S", 3)
        mapCompressed.put("BL=_BL_", 4)

//        println(mapCompressed.size())
        for (i in 0 until 20) {
            para.add(parameter[i])
        }




        heap.result().forEach {
            mapCompressed.put(it.first, mapCompressed.size() + 1)
            for (i in 0 until featureMap.tagSize()) {
                para.add(parameter[featureMap.featureMap[it.first] * featureMap.tagSize() + i])
            }
        }
        parameter = para.toFloatArray()
        featureMap.featureMap = mapCompressed
    }

    override fun save(file: File) {
        val treeMap = TreeMap<String, Int>()
        featureMap.featureMap.forEach {
            treeMap[it.key] = it.value
        }
        val datBuilder = DoubleArrayTrieBuilder<Int>()
        val dat = datBuilder.build(treeMap)
        val output = DataOutputStream(FileOutputStream(file))
        output.use {
            DoubleArrayTrie.write(dat, it) { id, out -> out.write(id) }
        }

    }

    override fun load(file: File) {
        val inputStream = DataInputStream(file.inputStream().buffered())

        val dat = inputStream.use {
            DoubleArrayTrie.read(it) { dat -> dat.readInt() }
        }
    }

    override fun decode(sequence: Array<T>): String {
        val tagArray = IntArray(sequence.size)
        val tt = sequence.mapIndexed { index, _ ->
            featureExtractor.extractFeature(sequence, index, featureMap)
        }
        val temp = FeaturedSequenceLabel(tt, tagArray)
        viterbiDecode(temp, tagArray)

        val result = StringBuilder()
        result.append(sequence[0])

        for (i in 1 until tagArray.size) {
            if (tagArray[i] == 0 || tagArray[i] == 3) {
                result.append(" ")
            }
            result.append(sequence[i])
        }
        return result.toString()
    }

    fun decode2(sequence: Array<T>, tagSet: ArrayList<String>): String {
        val tagArray = IntArray(sequence.size)
        val tt = sequence.mapIndexed { index, _ ->
            featureExtractor.extractFeature(sequence, index, featureMap)
        }
        val temp = FeaturedSequenceLabel(tt, tagArray)
        viterbiDecode(temp, tagArray)

        val result = StringBuilder()
        for (i in 0 until tagArray.size){
            result.append(sequence[i])
            result.append("/")
            result.append(tagSet[tagArray[i]])
            result.append(" ")
        }

        return result.toString()
    }


    private fun viterbiDecode(sentence: FeaturedSequenceLabel, guessLabel: IntArray): Double {
        val labelSet = arrayOf(0, 1, 2, 3)
        val bos = 4
        val sentenceLength = sentence.size
        val labelSize = labelSet.size

        val preMatrix = Array(sentenceLength) { IntArray(labelSize) }
        val scoreMatrix = Array(2) { DoubleArray(labelSize) }

        for (i in 0 until sentenceLength) {

            val _i = i and 1//偶数得0 奇数得1
            val _i_1 = 1 - _i//偶数得1 奇数得0
            val allFeature = sentence.featureMatrix[i]
            val transitionFeatureIndex = allFeature.size - 1
            if (0 == i) {
                allFeature[transitionFeatureIndex] = bos//一定是4
                for (j in labelSet.indices) {
                    preMatrix[0][j] = j

                    val score = score(allFeature, j)

                    scoreMatrix[0][j] = score
                }
            } else {
                for (curLabel in labelSet.indices) {

                    var maxScore = Integer.MIN_VALUE.toDouble()

                    for (preLabel in labelSet.indices) {

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

        for (index in 1 until labelSet.size) {
            if (maxScore < scoreMatrix[sentenceLength - 1 and 1][index]) {
                maxIndex = index
                maxScore = scoreMatrix[sentenceLength - 1 and 1][index]
            }
        }

        for (i in sentenceLength - 1 downTo 0) {
            guessLabel[i] = labelSet[maxIndex]
            maxIndex = preMatrix[i][maxIndex]
        }

        return maxScore
    }

    private fun score(featureVector: IntArray, currentTag: Int): Double {
        var score = 0.0.toDouble()
        for (index in featureVector) {
            if (index == -1) {
                continue
            } else if (index < -1 || index >= featureMap.featureSize()) {
//            } else if (index < -1 || !featureMap.containsValue(index)) {

                throw IllegalArgumentException("在打分时传入了非法的下标")
            } else {
                val temp = index * featureMap.tagSize() + currentTag
                try {
                    score += parameter[temp]   // 其实就是特征权重的累加
                } catch (e: ArrayIndexOutOfBoundsException) {

                }
            }
        }
        return score
    }

    class Labels(val goldFeature: IntArray, val predFeature: IntArray){
    }
}
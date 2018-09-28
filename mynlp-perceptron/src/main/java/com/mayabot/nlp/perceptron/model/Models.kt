package com.mayabot.nlp.perceptron.model

import com.mayabot.nlp.collection.dat.DoubleArrayTrie
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder
import com.mayabot.nlp.perceptron.FeatureExtractor
import com.mayabot.nlp.perceptron.FeatureMap
import com.mayabot.nlp.perceptron.PerceptronModel
import com.mayabot.nlp.perceptron.lmpl.DefaultFeatureExtractor
import com.mayabot.nlp.perceptron.lmpl.FeaturedSequenecLabel
import com.mayabot.nlp.perceptron.lmpl.StructuredPerceptronTrainer
import java.io.*
import java.util.*
import java.util.function.BiConsumer

class CostumisedPerceptron<T>(
        val featureMap: FeatureMap<String>,
        val parameter: FloatArray,
        private val featureExtractor: FeatureExtractor<T>
) : PerceptronModel<T> {
    fun update(goldIndex: IntArray, predictIndex: IntArray, total: DoubleArray, timestamp: IntArray, current: Int) {
        for (i in goldIndex.indices) {
            if (goldIndex[i] == predictIndex[i])
                continue
            else {
                update(goldIndex[i], 1f, total, timestamp, current)//当预测的标注和实际标注不一致时 先更新权重
                if (predictIndex[i] >= 0 && predictIndex[i] < parameter.size)
                    update(predictIndex[i], -1f, total, timestamp, current)
                else {
                    throw IllegalArgumentException("更新参数时传入了非法的下标")
                }
            }
        }
    }

    private fun update(index: Int, value: Float, total: DoubleArray, timestamp: IntArray, current: Int) {
        val passed = current - timestamp[index]
        total[index] += passed * parameter[index].toDouble() //权重乘以该纬度经历时间  //其实是这里设计应该是不合理的
        parameter[index] += value// +1
        timestamp[index] = current
    }

    fun average(total: DoubleArray, timestamp: IntArray, current: Int) {
        for (i in 0 until parameter.size) {
            parameter[i] = ((total[i].toFloat() + (current.toFloat() - timestamp[i].toFloat()) * parameter[i]) / current)
        }
    }


    fun compress() {}

//    fun addFeatures(newWord: Array<T>, extractor: FeatureExtractor<T>) {
//        newWord.forEachIndexed { index, _ ->
//            extractor.featureExtract(newWord, index, featureMap)
//        }
//
//
//    }

    override fun save(file: File) {
        val treeMap = TreeMap<String, Int>()
        featureMap.featureMap.forEach {
            treeMap[it.key] = it.value
        }
        val datBuilder = DoubleArrayTrieBuilder<Int>()
        val dat = datBuilder.build(treeMap)
        val output = DataOutputStream(FileOutputStream(file))
        output.use {
            DoubleArrayTrie.write(dat, it) { id,out -> out.write(id) }
        }

    }

    override fun load(file: File) {
        val inputStream = DataInputStream(file.inputStream().buffered())

        val dat = inputStream.use {
            DoubleArrayTrie.read(it) { dat->dat.readInt()}
        }
    }

    override fun decode(sequence: Array<T>): String {
        val tagArray = IntArray(sequence.size)
        val tt = sequence.mapIndexed { index, t ->
            featureExtractor.featureExtract(sequence, index, featureMap)
        }
        val temp = FeaturedSequenecLabel(tt, tagArray)
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

    fun viterbiDecode(sentence: FeaturedSequenecLabel, guessLabel: IntArray): Double {
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

    fun score(featureVector: IntArray, currentTag: Int): Double {
        var score = 0.0.toDouble()
        for (index in featureVector) {
            if (index == -1) {
                continue
            } else if (index < -1 || index >= featureMap.featureSize()) {
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
}
/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mayabot.nlp.segment.plugins.personname

import com.mayabot.nlp.common.FastStringBuilder
import com.mayabot.nlp.hppc.IntArrayList
import com.mayabot.nlp.perceptron.*
import com.mayabot.nlp.segment.plugins.personname.NRPerceptronFeature.extractFeatureVector
import com.mayabot.nlp.segment.plugins.personname.NRPerceptronSample.forOnlineLearn
import com.mayabot.nlp.segment.plugins.personname.NRPerceptronSample.sentenceToSample
import com.mayabot.nlp.utils.CharNormUtils
import java.io.File
import java.io.InputStream
import java.util.function.Consumer

data class PersonName(val name: String, val offset: Int)

/**
 * 感知机人名识别
 *
 */
class PersonNamePerceptron(val model: PerceptronModel) {

    private val featureSet = model.featureSet()

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

    /**
     * 在线学习一个人名样例。
     *
     * 例子： "与 令计划 妻子"
     *
     * 除了名字其他都是单字，用空格分开。
     *
     */
    fun learn(pre: String?, name: String, last: String?) {

        fun String.bindTag(tag: String): List<String> {
            return this.toCharArray().map { "$it$tag" }.toList()
        }

        fun String.nrBindTag(): List<String> {
            val list = ArrayList<String>()
            list.add(this.first() + "B")
            if (this.length > 2) {
                for (j in 1 until length - 1) {
                    list.add(this[j] + "M")
                }
            }
            list.add(this.last() + "E")
            return list
        }

        val list = ArrayList<String>()

        if (pre != null) {
            list.addAll(pre.bindTag("X"))
        }

        list += name.nrBindTag()

        if (last != null) {
            list.addAll(last.bindTag("Y"))
        }


        val sentence = list.joinToString(separator = "﹍")

        val id = forOnlineLearn(sentence, model.featureSet())
        model.makeSureParameter(id)
        val sample = sentenceToSample(sentence, model.featureSet())
        model.onlineLearn(sample)
    }

    /**
     * 计算返回人名
     */
    fun findPersonName(sentence: CharArray): List<PersonName> {
        val result = ArrayList<PersonName>()
        val decode = decode(sentence, false)

        var p = -1
        for (i in 0 until decode.size) {
            when (decode[i]) {
                B -> p = i
                E -> if (p != -1) {
                    result += PersonName(String(sentence, p, i - p + 1), p)
                    p = -1
                }
                O -> p = -1
            }
        }

        return result
    }

    fun decode(sentence: CharArray, convert: Boolean): IntArray {

        if (convert) {
            CharNormUtils.convert(sentence)
        }

        val buffer = FastStringBuilder(4)
        val size = sentence.size

        val featureList = ArrayList<IntArrayList>(size)

        for (i in 0 until size) {
            featureList += extractFeatureVector(sentence, size, i, featureSet, buffer)
        }

        return model.decode(featureList)
    }

    companion object {

        const val B = 0
        const val E = 2
        const val O = 3

        @JvmStatic
        val tagList = listOf("B", "M", "E", "O", "X", "Y", "Z")

        @JvmStatic
        fun load(parameterBin: InputStream, featureBin: InputStream): PersonNamePerceptron {
            val model = PerceptronFileFormat.loadWithFeatureBin(parameterBin, featureBin)
            return PersonNamePerceptron(model)
        }

        @JvmStatic
        fun load(dir: File): PersonNamePerceptron {
            return load(File(dir, "parameter.bin").inputStream().buffered(),
                    File(dir, "feature.dat").inputStream().buffered())
        }

    }
}


object NRPerceptronSample {

    fun forOnlineLearn(ineInput: String, featureSet: FeatureSet): Int {
        val sentence = sample2Juzi(ineInput).toCharArray()

        CharNormUtils.convert(sentence)

        var max = 0
        for (i in 0 until sentence.size) {
            NRPerceptronFeature.extractFeature(sentence, sentence.size, i, Consumer { feature ->
                var fid = featureSet.featureId(feature)
                if (fid < 0) {
                    val id = featureSet.newExtId(feature)
                    if (id > max) {
                        max = id
                    }
                }
            })
        }

        return max
    }

    /**
     * 训练的样本，提取全出的句子
     */
    fun sample2Juzi(line: String): String {
        return line.split("﹍").map { it[0] }.joinToString(separator = "")
    }

    fun sample2JuziAndTag(text: String): Pair<String, IntArray> {
        val stringBuilder = StringBuilder()
        val labels = IntArrayList()
        text.split("﹍").forEach { item ->
            val zi = item[0]
            val lab = item[1].toString()
            stringBuilder.append(zi)
            labels.add(idOf(lab))
        }
        return stringBuilder.toString() to labels.toArray()!!
    }

    /**
     * 把一个句子，变化为TrainSample
     */
    fun sentenceToSample(text: String, featureSet: FeatureSet): TrainSample {

        val buffer = FastStringBuilder(4)

        val (juziString, tagList) = sample2JuziAndTag(text)
        val juzi = juziString.toCharArray()

        val len = juzi.size
        val list = mutableListOf<IntArrayList>()

        for (i in 0 until len) {
            val vec = extractFeatureVector(juzi, len, i, featureSet, buffer)
            list.add(vec)
        }

        return TrainSample(list, tagList)
    }


    private fun idOf(tag: String): Int {
        return when (tag) {
            "B" -> 0
            "M" -> 1
            "E" -> 2
            "O" -> 3
            "X" -> 4
            "Y" -> 5
            "Z" -> 6
            else -> 0
        }
    }
}


object NRPerceptronFeature {


    private const val CHAR_NULL = '\u0000'


    fun extractFeatureVector(sentence: CharArray, size: Int, position: Int, features: FeatureSet, buffer: FastStringBuilder): IntArrayList {
        val vector = IntArrayList(8)

//        buffer.clear()
        val lastIndex = size - position - 1

        val pre2Char = if (position > 1) sentence[position - 2] else CHAR_NULL
        val preChar = if (position > 0) sentence[position - 1] else CHAR_NULL
        val curChar = sentence[position]
        val nextChar = if (lastIndex > 0) sentence[position + 1] else CHAR_NULL
        val next2Char = if (lastIndex > 1) sentence[position + 2] else CHAR_NULL

        buffer.set2(curChar, '2')
        addFeature(features, vector, buffer)

        if (position > 0) {
            buffer.set2(preChar, '1')
            addFeature(features, vector, buffer)

            buffer.set4(preChar, '/', curChar, '5')
            addFeature(features, vector, buffer)

            if (position > 1) {
                buffer.set4(pre2Char, '/', preChar, '4')
                addFeature(features, vector, buffer)
            }
        }

        if (lastIndex > 0) {
            buffer.set2(nextChar, '3')
            addFeature(features, vector, buffer)

            buffer.set4(curChar, '/', nextChar, '6')
            addFeature(features, vector, buffer)

            if (lastIndex > 1) {
                buffer.set4(nextChar, '/', next2Char, '7')
                addFeature(features, vector, buffer)
            }
        }


        vector.add(0)
        return vector
    }

    private fun addFeature(features: FeatureSet, vector: IntArrayList, stringBuilder: FastStringBuilder) {

        val id = features.featureId(stringBuilder)

        stringBuilder.clear()
        if (id >= 0) {
            vector.add(id)
        }
    }

    /**
     * 在制作特征大集合的时候使用
     */
    fun extractFeature(sentence: CharArray, size: Int, position: Int, callBack: Consumer<String>) {
        val lastIndex = size - position - 1

        val pre2Char = if (position > 1) sentence[position - 2] else CHAR_NULL
        val preChar = if (position > 0) sentence[position - 1] else CHAR_NULL
        val curChar = sentence[position]
        val nextChar = if (lastIndex > 0) sentence[position + 1] else CHAR_NULL
        val next2Char = if (lastIndex > 1) sentence[position + 2] else CHAR_NULL

        if (position > 0) callBack.accept(preChar + "1")
        callBack.accept(curChar + "2")
        if (lastIndex > 0) callBack.accept(nextChar + "3")

        if (position > 1) callBack.accept(pre2Char + "/" + preChar + "4")
        if (position > 0) callBack.accept(preChar + "/" + curChar + "5")

        if (lastIndex > 0) callBack.accept(curChar + "/" + nextChar + "6")
        if (lastIndex > 1) callBack.accept(nextChar + "/" + next2Char + "7")
    }
}

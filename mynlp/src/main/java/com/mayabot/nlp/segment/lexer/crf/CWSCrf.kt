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
package com.mayabot.nlp.segment.lexer.crf

import com.mayabot.nlp.common.hppc.IntArrayList
import com.mayabot.nlp.common.utils.CharNormUtils
import com.mayabot.nlp.perceptron.FeatureSet
import com.mayabot.nlp.perceptron.PerceptronModel
import com.mayabot.nlp.segment.lexer.crf.FeatureTemplateGroup.Companion.BOS
import com.mayabot.nlp.segment.lexer.crf.FeatureTemplateGroup.Companion.EOS
import java.io.File
import java.io.InputStream
import java.util.*


fun main(args: Array<String>) {
    var cwsCrf = CWSCrf.load(File("data.work/crf/model"))

    //val x = cwsCrf.decode("商品和服务".toCharArray(), true)

    println(cwsCrf.decodeToWordList("中新网客户端北京12月12日电(张旭)随着寒冬来临，羽绒服也迎来了属于自己的旺季。然而近期市场对羽绒服两大企业国产波司登、加拿大鹅(Canada Goose)却有着截然不同的反应，加拿大鹅股价连日走低，波司登股价则大涨，创5年新高。\n" +
            "心肌细胞是心脏泵血的动力来源，心肌细胞出问题可能会导致严重疾病甚至死亡。因此，如果能让心脏中长出新的心肌细胞，替换掉有问题的细胞，以此修复心脏，无疑是医学上的一大突破。"))
}

class CWSCrf(val model: PerceptronModel, val labelList: Array<String>, val featureTemplateGroup: FeatureTemplateGroup) {

    // 提前确定S和E对应的ID

    private val S_ID: Int
    private val E_ID: Int

    init {
        var s_id = 0
        var e_id = 0
        labelList.forEachIndexed { index, s ->
            if (s == "S") {
                s_id = index
            }
            if (s == "E") {
                e_id = index
            }
        }

        S_ID = s_id
        E_ID = e_id
    }

    fun decodeToWordList(sentence: String): List<String> {
        val result = ArrayList<String>()
        val decode = decode(sentence.toCharArray(), true)
        var p = 0

        for (i in 0 until decode.size) {
            val f = decode[i]
            if (f == S_ID || f == E_ID) {
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

        val buffer = StringBuilder()
        val featureList = ArrayList<IntArrayList>(sentence.size)
        for (i in 0 until sentence.size) {
            featureList += CWSCrfFeature.extractFeatureVector(sentence, i, model.featureSet(), featureTemplateGroup, buffer)
        }
        return model.decode(featureList)
    }

    companion object {
        /**
         * 加载CRF模型
         */
        @JvmStatic
        fun load(dir: File): CWSCrf {
            val parameterBin = File(dir, "parameter.bin").inputStream().buffered()
            val featureBin = File(dir, "feature.dat").inputStream().buffered()
            val labelText = File(dir, "label.txt").inputStream().buffered()
            val featureTemplate = File(dir, "featureTemplate.txt").inputStream().buffered()
            return load(parameterBin, featureBin, labelText, featureTemplate)
        }

        /**
         * 加载CRF模型
         * @param parameterBin 参数的BIN文件
         * @param featureBin feature的DAT格式文件
         * @param labelText label文本文件
         * @param featureTemplate featureTemplate文本文件
         */
        @JvmStatic
        fun load(parameterBin: InputStream, featureBin: InputStream, labelText: InputStream,
                 featureTemplate: InputStream
        ): CWSCrf {
            val model = PerceptronModel.loadWithFeatureBin(parameterBin, featureBin)
            val labelList = labelText.use { it.bufferedReader().readLines() }

            val featureTemplateGroup = FeatureTemplateGroup(featureTemplate.use { it.bufferedReader().readLines() })

            return CWSCrf(model, labelList.toTypedArray(), featureTemplateGroup)
        }
    }

}


/**
 * 根据FeatureTemplate生成特征向量
 */
object CWSCrfFeature {

    fun extractFeatureVector(sentence: CharArray, position: Int, features: FeatureSet, featureTemplateGroup: FeatureTemplateGroup, sbFeature: StringBuilder): IntArrayList {
        val vector = IntArrayList(featureTemplateGroup.size + 1)

        sbFeature.clear()

        for (ft in featureTemplateGroup.list) {

            extFeature(sentence, position, sbFeature, ft)

            val id = features.featureId(sbFeature)
            if (id != -1) vector.add(id)

            sbFeature.clear()
        }

        vector.add(0)

        return vector
    }

    private fun extFeature(sentence: CharArray, position: Int, sbFeature: StringBuilder, ft: FeatureTemplate) {
        val senLen = sentence.size
        for (x in ft.list) {
            val type = x.type
            if (type == FeatureTemplateElementType.String) {
                sbFeature.append(x.value)
            } else {
                val offset = x.offset + position
                when {
                    offset < 0 -> sbFeature.append(BOS[-(offset + 1)])
                    offset >= senLen -> sbFeature.append(EOS[offset - senLen])
                    else -> sbFeature.append(sentence[offset])
                }
            }
        }
    }
}
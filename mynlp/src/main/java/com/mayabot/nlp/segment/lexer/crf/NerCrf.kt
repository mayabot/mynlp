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

import com.mayabot.nlp.hppc.IntArrayList
import com.mayabot.nlp.perceptron.FeatureSet
import com.mayabot.nlp.perceptron.PerceptronFileFormat
import com.mayabot.nlp.perceptron.PerceptronModel
import com.mayabot.nlp.perceptron.PerceptronModelImpl
import com.mayabot.nlp.segment.WordTerm
import com.mayabot.nlp.segment.lexer.crf.FeatureTemplateGroup.Companion.BOS
import com.mayabot.nlp.segment.lexer.crf.FeatureTemplateGroup.Companion.EOS
import java.io.File
import java.io.InputStream
import java.util.*

/**
 * 基于CRF算法的命名实体识别。
 */
class NerCrf(val model: PerceptronModel, val labels: Array<String>, val featureTemplateGroup: FeatureTemplateGroup) {

    val featureSet = model.featureSet()

    fun decode(sentence: List<WordTerm>) {

        val buffer = StringBuilder()
        val featureList = ArrayList<IntArrayList>(sentence.size)
        for (i in 0 until sentence.size) {
            featureList += NerCrfFeature.extractFeatureVector(sentence, i, featureSet, featureTemplateGroup, buffer)
        }

        val result = model.decode(featureList)

        for (i in 0 until sentence.size) {
            sentence[i].customFlag = labels[result[i]]
        }
    }

    companion object {
        /**
         * 加载CRF模型
         */
        @JvmStatic
        fun load(dir: File): NerCrf {
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
        fun load(parameterBin: InputStream,
                 featureBin: InputStream,
                 labelText: InputStream,
                 featureTemplate: InputStream
        ): NerCrf {
            val model = PerceptronFileFormat.loadWithFeatureBin(parameterBin, featureBin)
            val labelList = labelText.use { it.bufferedReader().readLines() }

            val featureTemplateGroup = FeatureTemplateGroup(featureTemplate.use { it.bufferedReader().readLines() })

            return NerCrf(model, labelList.toTypedArray(), featureTemplateGroup)
        }
    }

}


/**
 * 根据FeatureTemplate生成特征向量
 */
object NerCrfFeature {

    fun extractFeatureVector(sentence: List<WordTerm>, position: Int, features: FeatureSet, featureTemplateGroup: FeatureTemplateGroup, sbFeature: StringBuilder): IntArrayList {
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

    private fun extFeature(sentence: List<WordTerm>, position: Int, sbFeature: StringBuilder, ft: FeatureTemplate) {
        val senLen = sentence.size
        for (x in ft.list) {
            val type = x.type
            if (type == FeatureTemplateElementType.String) {
                sbFeature.append(x.value)
            } else {
                val offset = x.offset + position
                val col = x.col
                when {
                    offset < 0 -> sbFeature.append(BOS[-(offset + 1)])
                    offset >= senLen -> sbFeature.append(EOS[offset - senLen])
                    else -> sbFeature.append(
                            if (col == 0) sentence[offset].word else sentence[offset].natureString
                    )
                }
            }
        }
    }
}
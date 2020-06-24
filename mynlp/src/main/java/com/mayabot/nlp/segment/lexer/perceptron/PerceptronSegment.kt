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
package com.mayabot.nlp.segment.lexer.perceptron

import com.mayabot.nlp.perceptron.PerceptronFileFormat
import com.mayabot.nlp.perceptron.PerceptronModel
import com.mayabot.nlp.perceptron.PerceptronRunner
import java.io.File
import java.io.InputStream

/**
 * 用B M E S进行分词的感知机模型
 * @author jimichan
 */
class PerceptronSegment(val model: PerceptronModel) {

    val runner = PerceptronRunner(PerceptronSegmentDefinition())

    /**
     * 保存分词模型
     */
    fun save(dir: File) {
        dir.mkdirs()
        model.save(dir)
    }

    /**
     * 在线学习一个句子
     * 句子,词用空格分开
     */
    fun learn(learn: String) {
        val sentence = learn.replace(" ", "﹍")
        runner.learnModel(model, sentence)
    }

    fun decode(sentence: CharArray,convertChar:Boolean = true):IntArray {
        return runner.decode(model,sentence,convertChar)
    }

    fun decode(sentence: String): List<String> {
        val result = ArrayList<String>()
        val decode = runner.decode(model, sentence.toCharArray())
        var p = 0

        for (i in decode.indices) {
            var f = decode[i]
            // 字符不识别
            if(f == -1){
                f = S
            }
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

    companion object {
        // "B", "M", "E", "S"
        const val B = 0
        const val M = 1
        const val E = 2
        const val S = 3

        @JvmStatic
        fun load(parameterBin: InputStream, featureBin: InputStream): PerceptronSegment {
            val model = PerceptronFileFormat.loadWithFeatureBin(parameterBin, featureBin)
            return PerceptronSegment(model)
        }

        @JvmStatic
        fun load(dir: File): PerceptronSegment {
            return load(File(dir, "parameter.bin").inputStream().buffered(),
                    File(dir, "feature.dat").inputStream().buffered())
        }

    }
}


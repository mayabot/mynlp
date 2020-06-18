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
package com.mayabot.nlp.perceptron

import com.mayabot.nlp.MynlpEnv
import com.mayabot.nlp.Mynlps
import java.io.DataInputStream
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer

/**
 * 感知机保存的格式
 */
object PerceptronFileFormat {

    @JvmStatic
    @JvmOverloads
    fun loadFromClasspath(prefix: String, loader: ClassLoader = Thread.currentThread().contextClassLoader): PerceptronModel {
        val parameter = loader.getResourceAsStream("$prefix/parameter.bin")
        val feature = loader.getResourceAsStream("$prefix/feature.dat")
                ?: loader.getResourceAsStream("$prefix/feature.txt")

        check(parameter != null && feature != null)

        val isDat = loader.getResource("$prefix/feature.dat") != null

        return if (isDat) {
            loadWithFeatureBin(parameter, feature)
        } else {
            loadWithFeatureTxt(parameter, feature)
        }

    }

    @JvmStatic
    @JvmOverloads
    fun loadFromNlpResource(prefix: String, nlpEnv: MynlpEnv = Mynlps.get().env): PerceptronModel {

        val parameter = nlpEnv.loadResource("$prefix/parameter.bin")
        val fd = nlpEnv.tryLoadResource("$prefix/feature.dat")
        val feature =  fd ?: nlpEnv.loadResource("$prefix/feature.txt")

        check(parameter != null && feature != null)

        val isDat = fd != null

        return if (isDat) {
            loadWithFeatureBin(parameter.inputStream(), feature.inputStream())
        } else {
            loadWithFeatureTxt(parameter.inputStream(), feature.inputStream())
        }

    }

    @JvmStatic
    fun loadWithFeatureBin(parameterBin: InputStream, featureBin: InputStream): PerceptronModel {
        return load(parameterBin, featureBin, null)
    }

    @JvmStatic
    fun loadWithFeatureTxt(parameterBin: InputStream, featureTxt: InputStream): PerceptronModel {
        return load(parameterBin, null, featureTxt)
    }

    /**
     * 正常训练完成的模型，是保存在文件夹。
     * 里面包含featureBin和featureTxt
     * 我们发布的模型bin和txt是二选一的
     * 使用这种加载方式，是可以对模型进行压缩的。
     */
    @JvmStatic
    fun load(dir: File): PerceptronModel {
        fun loadIfExit(name: String): File? {
            val f = File(dir, name)
            return if (f.exists()) f else null
        }

        fun load(parameterFile: File, featureBin: File?, featureText: File?): PerceptronModel {
            return load(
                    parameterFile.inputStream().buffered(),
                    featureBin?.inputStream()?.buffered(),
                    featureText?.inputStream()?.buffered()
            )
        }

        return load(
                File(dir, "parameter.bin"),
                loadIfExit("feature.dat"),
                loadIfExit("feature.txt")
        )
    }

    private fun load(parameterBin: InputStream, featureBin: InputStream?,
                     featureText: InputStream?): PerceptronModel {

        check(!(featureBin == null && featureText == null)) { "featureBin不可以同时为空" }

        var labelCount = 0
        var parameter = FloatArray(0)

        parameterBin.use { x ->

            val input = DataInputStream(x)
            labelCount = input.readInt()

            val pSize = input.readInt()
            parameter = FloatArray(pSize)

            val buffer = ByteArray(4 * 1024 * 4)
            val wrap = ByteBuffer.wrap(buffer)
            var point = 0
            while (true) {
                val n = input.read(buffer)
                if (n == -1) {
                    break
                }
                if (n % 4 != 0) {
                    throw java.lang.RuntimeException("Error Size $n")
                }

                wrap.flip()
                wrap.limit(n)

                for (i in 0 until n / 4) {
                    parameter[point++] = wrap.float
                }
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
                FeatureSet.readFromTextButNotSave(featureText)
            } else {
                throw RuntimeException("featureText featureBin 不可以同时为空")
            }
        }

        return PerceptronModelImpl(fs, labelCount, parameter)
    }
}
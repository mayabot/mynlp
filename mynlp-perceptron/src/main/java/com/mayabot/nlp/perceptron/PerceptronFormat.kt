package com.mayabot.nlp.perceptron

import java.io.DataInputStream
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer

/**
 * 感知机保存的格式
 */
object PerceptronFormat {

    @JvmStatic
    fun load(parameterBin: InputStream, featureBinOrTxt: InputStream, featureDatIsBin: Boolean): Perceptron {
        return if (featureDatIsBin) {
            load(parameterBin, featureBinOrTxt, null)
        } else {
            load(parameterBin, null, featureBinOrTxt)
        }

    }

    @JvmStatic
    fun load(parameterFile: File, featureBin: File?, featureText: File?): Perceptron {
        return load(
                parameterFile.inputStream().buffered(),
                featureBin?.inputStream()?.buffered(),
                featureText?.inputStream()?.buffered()
        )
    }

    @JvmStatic
    fun load(dir: File): Perceptron {
        fun loadIfExit(name: String): File? {
            val f = File(dir, name)
            return if (f.exists()) f else null
        }

        return load(
                File(dir, "parameter.bin"),
                loadIfExit("feature.dat"),
                loadIfExit("feature.txt")
        )
    }

    @JvmStatic
    fun load(parameterBin: InputStream, featureBin: InputStream?, featureText: InputStream?): Perceptron {

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
                    println("Error Size")
                    System.exit(0)
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
                FeatureSet.read(featureText)
            } else {
                throw RuntimeException()
            }
        }

        return PerceptronModel(fs, labelCount, parameter)
    }
}
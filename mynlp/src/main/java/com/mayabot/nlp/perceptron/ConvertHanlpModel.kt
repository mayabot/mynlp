package com.mayabot.nlp.perceptron

import com.mayabot.nlp.algorithm.collection.dat.DoubleArrayTrie
import java.io.DataInputStream
import java.io.File

/**
 * 转换hanlp lager/cws.bin里面的信息为mynlp的自定义格式
 * 从里面提取出来的features.txt 是 key 和 id的键值对
 * parameter.bin是每个id对应的BMES参数，第一行是数组总大小
 *
 * 在LinearModel加载是，发现tagLabel=4的时候，
 * 访问featureMap.entrySet();获得键值对
 */
object ConvertHanlpModel {

    @JvmStatic
    fun main(args: Array<String>) {


        val featureMap = HashMap<String, Int>()

        File("data/hanlp/features.txt").bufferedReader().useLines { lines ->

            lines.forEach { line ->
                val split = line.split("\t")
                var key = split[0]
                var id = split[1].toInt()

                if (id == 8) {
                    println()
                }

                if (id == 0) {
                    println()
                }
                key = when (key) {
                    "BL=B" -> "\u0001\u0001BL=0"
                    "BL=M" -> "\u0001\u0001BL=1"
                    "BL=E" -> "\u0001\u0001BL=2"
                    "BL=S" -> "\u0001\u0001BL=3"
                    "BL=_BL_" -> "\u0001\u0001BL=4"
                    else -> key
                }

                featureMap[key] = id
            }
        }

        val pIn = DataInputStream(File("data/hanlp/parameter.bin").inputStream().buffered())

        val parameters = FloatArray(pIn.readInt())

        for (i in 0 until parameters.size) {
            parameters[i] = pIn.readFloat()
        }

        pIn.close()


        val sortedList = featureMap.keys.sorted()

        println(sortedList.size)

        val newMap = sortedList.zip(0 until sortedList.size).toMap()

        val newParameters = FloatArray(parameters.size)

        featureMap.forEach { key, id ->
            if ("\u0001\u0001BL=0" == key) {
                println()
            }
            val newId = newMap[key]!!
            for (i in 0 until 4) {
                newParameters[newId * 4 + i] = parameters[id * 4 + i]
            }
        }


        val newFeatureMap = DoubleArrayTrie(sortedList)
        val x = FeatureSet(newFeatureMap, sortedList)
        val model = PerceptronModelImpl(x, 4, newParameters)

        model.save(File("data/hanlp/model"))


    }

}
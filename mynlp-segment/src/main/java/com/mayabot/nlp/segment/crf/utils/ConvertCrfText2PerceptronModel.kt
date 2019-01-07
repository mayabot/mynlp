package com.mayabot.nlp.segment.crf.utils

import com.mayabot.nlp.perceptron.FeatureSet
import java.io.DataOutputStream
import java.io.File

/**
 * 一个把CRF++输出的文本模型，转换为感知机使用的模型文件.
 * 注意事项：
 * 1.特征格式为了加快DAT构建，U05:”/白/酒 转换为 ”/白/酒:U05
 * 2.参数数组，增加了一个长度为labelSize的空白。
 * 前一段为labelSize*labelSize的长度，载入的是标签转移信息。
 * 后面增加了一个段空白前一段为labelSize。再后面是每个特征对应的各个标签的概率信息
 */
object ConvertCrfText2PerceptronModel {

    @JvmStatic
    fun main(args: Array<String>) {
        convert(File("data.work/crf/model_c5_f5.txt"), File("data.work/crf/model"))

        val fs = FeatureSet.readFromText(File("data.work/crf/model/feature.txt").inputStream().buffered())
        fs.save(File("data.work/crf/model/feature.dat"), null)
    }

    /**
     * 转换CRF++的txt文本格式.转换为感知机模型的格式
     */
    fun convert(txtModel: File, outputDir: File) {

        val labelWriter = File(outputDir, "label.txt").bufferedWriter()
        val featureTemplate = File(outputDir, "featureTemplate.txt").bufferedWriter()


        outputDir.mkdir()

        var reader = txtModel.bufferedReader()


        println("Version ${reader.readLine()}")
        println("${reader.readLine()}")

        val maxid = Integer.parseInt(reader.readLine().substring("maxid:".length).trim({ it <= ' ' }))

        println("${reader.readLine()}")

        //read bank
        reader.readLine()

        var labelSize = 0
        var line = reader.readLine()
        while (line.isNotEmpty()) {
            labelWriter.write(line)
            labelWriter.write("\n")
            line = reader.readLine()
            labelSize++
        }

        // FeatureTemplate
        line = reader.readLine()
        while (line != "B") {

            featureTemplate.write(line)
            featureTemplate.write("\n")
            line = reader.readLine()
        }

        // read bank
        reader.readLine()
        // 0 B
        reader.readLine()

        val parameterWriter = DataOutputStream(File(outputDir, "parameter.bin").outputStream().buffered())

        val featureMap = HashMap<String, Int>()
        //features
        var count2 = 0
        for (i in 0..labelSize) {
            featureMap["\u0000\u0001BL=$i"] = count2++
        }

        line = reader.readLine()


        while (line.isNotEmpty()) {
            val f = line.split(" ")[1]
            // U09:天/桥 转换为 天/桥:U09

            val ii = f.indexOf(":")
            val part1 = f.substring(0, ii)
            val part2 = f.substring(ii + 1, f.length)
            val change2 = part2 + ":" + part1

            featureMap[change2] = count2++

            //把U09后置，有利于构建DAT

            line = reader.readLine()
        }


        labelWriter.close()
        featureTemplate.close()


        var parameterArray = FloatArray(maxid + labelSize)
        var p = 0
//        parameterWriter.writeInt(labelSize)
//        parameterWriter.writeInt(maxid + labelSize)
//        //参数
        for (i in 0 until labelSize * labelSize) {
            val line2 = reader.readLine()!!
            val weight = line2.toFloat()
            parameterArray[p++] = weight
        }
//     //这里会多一个Lable,但是用不到
        for (i in 0 until labelSize) {
            parameterArray[p++] = 0f
        }

        line = reader.readLine()
        while (line != null && line.isNotEmpty()) {
            val weight = line.toFloat()
            parameterArray[p++] = weight

            line = reader.readLine()
        }


        val sortedFatureList = featureMap.keys.sorted()
        val featureWriter = File(outputDir, "feature.txt").bufferedWriter()
        for (x in sortedFatureList) {
            featureWriter.append(x)
            featureWriter.append("\n")
        }

        featureWriter.close()

        //调整顺序
        var parameterArray2 = FloatArray(parameterArray.size)


        var newid = -1
        for (f in sortedFatureList) {
            val oldId = featureMap[f]!!
            newid++

            for (i in 0 until labelSize) {
                parameterArray2[newid * labelSize + i] = parameterArray[oldId * labelSize + i]
            }
        }


        parameterWriter.writeInt(labelSize)
        parameterWriter.writeInt(parameterArray2.size)
        for (x in parameterArray2) {
            parameterWriter.writeFloat(x)
        }

        reader.close()
        parameterWriter.close()
    }
}
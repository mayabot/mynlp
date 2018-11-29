package com.mayabot.nlp.segment.perceptron

import java.io.DataInputStream
import java.io.File

object ReadHanlpCwsBinFile {
    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("data/hanlp/cws.bin")


        // fisrst tag set

        val input = DataInputStream(file.inputStream().buffered())

        //TaskType
        val taskType = input.readInt()
        println(taskType)

        val tagSize = input.readInt()
        println(tagSize)

        for (i in 0 until tagSize) {
            println(input.readUTF())
        }

        val featureSetSize = input.readInt()

        println(featureSetSize)

        println(input.readInt())


        input.close()
//        val featureOut = File("data/hanlp/feature.txt").bufferedWriter()
//        for (i in 0 until featureSetSize) {
//            featureOut.write(input.readUTF()+"\n")
//        }
//        featureOut.flush()
//        featureOut.close()


    }
}
package com.mayabot.nlp.cli

import com.google.common.base.Charsets
import com.google.common.io.Files
import com.mayabot.mynlp.fasttext.FastText
import com.mayabot.nlp.classification.FasttextClassification
import java.io.File

object TestClassification {
    @JvmStatic
    fun main(args: Array<String>) {
        val trainFile = File("data/test/trainFile.txt")


        val modelTrain = FasttextClassification.train(trainFile, 100, 0.05, 20)

        modelTrain.saveModel("data/test/search.model")


        //test

        run {
            val model = FastText.loadModel("data/test/search.model", false)


            val testFile = File("data/test/testFile.txt")

            val examples = Files.readLines(testFile, Charsets.UTF_8)

            var total = 0
            var success = 0

            for (i in examples.indices) {
                val line = examples[i]

                val parts = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                var label: String? = null

                for (part in parts) {
                    if (part.startsWith("__label__")) {
                        label = part
                    }
                }
                val result = FasttextClassification.predict(model, line)

                if (label == result) {
                    success++
                } else {
                }

                total++
            }

            println("Total $total")
            println("Success $success")

            println("正确率 " + String.format("%.2f", success * 100.0 / total) + "%")
        }
    }
//    @JvmStatic
//    fun main(args: Array<String>) {
//
//        val all = ArrayList<String>()
//
//        all.addAll( "data/test/wenhao.txt".toFile().readLines().toSet().map { it+" __lable__wh" } )
//        all.addAll( "data/test/ywtb.txt".toFile().readLines().toSet().map { it+" __lable__ywtb" } )
//        all.addAll( "data/test/zwgk.txt".toFile().readLines().toSet().map { it+" __lable__zwgk" } )
//
//        val trainFile = "data/test/trainFile.txt".toFile().bufferedWriter()
//        val testFile = "data/test/testFile.txt".toFile().bufferedWriter()
//
//        var c1=0
//        var c2=0
//        var c3=0
//        all.forEach { line->
//            when{
//                line.contains("__lable__wh") -> c1++
//                line.contains("__lable__ywtb") -> c2++
//                line.contains("__lable__zwgk") -> c3++
//            }
//            if(Random.nextFloat()<=0.8){
//                trainFile.write(line+"\n")
//            }else{
//                testFile.write(line+"\n")
//            }
//        }
//
//        testFile.close()
//        trainFile.close()
//
//        println("wn $c1")
//        println("ywtb $c2")
//        println("zwgk $c3")
//    }
}
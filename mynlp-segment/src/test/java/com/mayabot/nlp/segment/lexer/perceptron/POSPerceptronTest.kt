package com.mayabot.nlp.segment.lexer.perceptron

import com.mayabot.nlp.segment.plugins.pos.POSPerceptronTrainer
import com.mayabot.nlp.utils.CharNormUtils
import java.io.File

fun main(args: Array<String>) {
    val model = POSPerceptronTrainer().train(File("data/pku/199801.txt"), File("data/cncorpus/cncorpus_9.txt"), 1, 1)
    model.save(File("data/pos/model"))


//////
//    println(model.decode("陈汝烨"))

//    val model = POSPerceptron.load(File("data/pos/model"))
    val words = "陈汝烨 余额宝 的 规模 增长 一直 呈现 不断 加速 , 的 状态".split(" ")
//
////    val train = POSPerceptronTrainer()
////    train.train(File("data/pku"),1,4)
////    val sampleList = train.loadSamples(File("data/pku").allFiles())
////    val eva = POSEvaluateRunner(0, sampleList)
////    eva.run(model.model)
//
//
    val words2 = CharNormUtils.convert("陈汝烨 陈勤勤 余额宝 的 规模 增长 一直 呈现 不断 加速 , 的 状态 四十 年 , 我 的 心里 从未 这么 安静 过").split(" ")
    val result = model.decode(words2)
    println(words2.zip(result))

//    val lines = File("data/pos/model/feature.txt").readLines()
//
//    var index = DoubleArrayTrie(lines)
//
//
//    println(index.wordId("望京☺"))
}

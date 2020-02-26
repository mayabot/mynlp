package com.mayabot.nlp.segment.lexer.perceptron.inner

import com.mayabot.nlp.perceptron.PerceptronRunner
import com.mayabot.nlp.segment.lexer.perceptron.PerceptronSegmentDefinition
import java.io.File

/**
 * 参数
 * Iter 150
 * thread 2
 */
fun main() {

    val runner = PerceptronRunner(PerceptronSegmentDefinition())

//        val trainFile = File("data.work/corpus.segment/backoff2005/msr_training.txt")
//        val evaluateFile = File("data.work/corpus.segment/backoff2005/msr_test_gold.txt")
//
    val trainFile = File("data.work/cws/pku/199801.txt")
    val evaluateFile = File("data.work/cws/pku/199802.txt")

    var model = runner.train(
            trainFile,
            evaluateFile,
            10, 8)

    println("compress")
    model = model.compress(0.2, 1e-3)

    println("After compress ...")
    val evlResult = runner.evaluateModel(model,evaluateFile)
    println(evlResult)

    model.save(File("data.work/cws-model"))
}
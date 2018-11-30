package com.mayabot.nlp.segment.perceptron

import com.mayabot.nlp.perceptron.solution.ner.NERPerceptron
import com.mayabot.nlp.perceptron.solution.ner.NERPerceptronTrainer
import com.mayabot.nlp.segment.MynlpTokenizers
import java.io.File


object NERPerceptronTest {

    @JvmStatic
    fun main(args: Array<String>) {
        train()
//        test()
    }

    fun train() {
        val trainer = NERPerceptronTrainer()

        val trainFile = File("data/pku")
        val evaluateFile = File("data/pku/199802.txt")

        val model = trainer.train(
                trainFile, evaluateFile,
                5, 2)


        // model.save(File("data/ner/model"))
    }

    fun test() {
        val evaluateFile = File("data/pku/199802.txt")

        val tokenizer = MynlpTokenizers.coreTokenizer()
        val text = "这是陈建国的快递,来自上海万行信息科技有限公司的报告"

        val termList = tokenizer.tokenToTermList(text)

        println(termList)

        val ner = NERPerceptron.load(File("data/ner/model"))

        var result = ner.decode(termList)

        println(result.joinToString(separator = ","))

    }
}

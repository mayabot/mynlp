package com.mayabot.nlp.fasttext

import com.mayabot.nlp.fasttext.args.InputArgs
import com.mayabot.nlp.fasttext.args.ModelName
import com.mayabot.nlp.fasttext.loss.LossName
import com.mayabot.nlp.fasttext.utils.disableLog
import java.io.File

class TestSup {

    val trainFile = File("data/agnews/ag.train")
    val testFile = File("data/agnews/ag.test")

    fun testSub(){
        disableLog()
        val lossNames = listOf(LossName.softmax,LossName.ns,LossName.hs,LossName.ova)

        lossNames.forEach { loss->
            check(test(loss)){
                "Loss Name ${loss.name} ERROR"
            }
        }
    }


    fun test(lossName: LossName) : Boolean {
//        val trainSources = listOf(loadTrainFile("ag.train.txt"))
//        val testSources = loadTrainFile("ag.test.txt")

        val trainArgs = InputArgs()
        trainArgs.loss = lossName

        val fastText = FastText.train(trainFile,ModelName.sup,trainArgs)

        val meter = fastText.test(testFile)

        return meter.f1Score() > 0.9
    }

}
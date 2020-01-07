package com.mayabot.nlp.fasttext

import com.mayabot.nlp.fasttext.args.InputArgs
import com.mayabot.nlp.fasttext.loss.LossName
import java.io.File


val trainFile = File("fasttext/data/agnews/ag.train")
val testFile = File("fasttext/data/agnews/ag.test")
fun main() {

    val args = InputArgs().apply {
        this.loss = LossName.softmax
        lr = 0.1
        dim = 100
        minn = 0
        maxn = 0
    }

    val fastText = FastText.trainSupervised(trainFile, args)

//    fastText.saveModel("fasttext/data/agnews/model")
//
//    val qFastText = fastText.quantize(dsub = 10)
//    qFastText.saveModel("fasttext/data/agnews/model.q")
//////
    fastText.test(testFile, 1)
//    qFastText.test(testFile, 1)

//    val fastText = FastText.loadModel(File("fasttext/data/agnews/model"),true)
//    val fastText = FastText.loadCppModel(File("fasttext/data/agnews/model.ftz"))
//    fastText.test(testFile, 1)




}

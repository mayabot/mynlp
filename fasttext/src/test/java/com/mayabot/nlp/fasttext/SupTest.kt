package com.mayabot.nlp.fasttext

import com.mayabot.nlp.fasttext.args.TrainArgs
import com.mayabot.nlp.fasttext.loss.LossName
import com.mayabot.nlp.fasttext.train.FastTextTrain
import org.junit.Test
import java.io.File


val trainFile = File("fasttext/data/agnews/ag.train")
val testFile = File("fasttext/data/agnews/ag.test")
fun main() {
        val args = TrainArgs().apply {
            this.loss = LossName.ova
            lr = 0.1
            dim = 100
        }
        val fastText = FastTextTrain.trainSupervised(trainFile,args)

    fastText.test(testFile,1)
}

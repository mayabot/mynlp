package com.mayabot.nlp.fasttext

import com.mayabot.nlp.fasttext.args.TrainArgs
import com.mayabot.nlp.fasttext.loss.LossName
import com.mayabot.nlp.fasttext.train.FastTextTrain
import org.junit.Test
import java.io.File


val trainFile = File("fasttext/data/agnews/ag.train")
fun main() {
        val args = TrainArgs().apply {
            this.loss = LossName.hs
        }
        val fastText = FastTextTrain.trainSupervised(trainFile,args)
}

package starspace

import com.mayabot.nlp.starspace.*
import java.io.File
import kotlin.io.forEachLine

/**
 * Ag news 测试数据集
 *
Loading dict from model file : data/agnews/ag.tsv
Number of words in dictionary:  95810
Number of labels in dictionary: 4
Setting dim from Tsv file to: 20
Initialized model weights. StarSpace length :
matrix : 95814 20
Prediction use 4 known labels.
total=7600
right=6977
rate 0.9180263157894737
 */

fun main(args: Array<String>) {

    if (!File("example.data/agnews/model").exists()) {
        AgNews.train()
    }

    AgNews.test()
}

object AgNews {

    fun train() {
        val trainArgs = trainArgs()

        val trainer = ModelTrainer(trainArgs, "example.data/agnews/ag.train")

        val train = trainer.train()


        train.saveModel("example.data/agnews/model")
    }

    fun test() {
        val trainArgs = trainArgs()

        val model = StarSpace.loadModel("example.data/agnews/model")

        val ssp = model.prediction()

        var total = 0
        var right = 0

        File("example.data/agnews/ag.test").forEachLine { line ->

            val i = line.indexOf(',')

            if (i < 0) return@forEachLine

            val label = line.substring(0, i).trim { it <= ' ' }

            val text = line.substring(i + 1)

            total++

            val r = ssp.predictOne(text)

            val pairs = ssp.baseDocs[r[0].second]
            val guess = model.dict.getSymbol(pairs[0].first)

            if (guess == label) {
                right++
            }
        }

        println("total=$total")
        println("right=$right")
        println("rate " + right * 1.0 / total)
    }


    fun trainArgs() = Args().apply {
        dim = 100
        similarity = Similarity.Dot
        maxNegSamples = 3
        negSearchLimit = 5
        thread = 10
        epoch = 8
        lr = 0.01
        ngrams = 1
        adagrad = false
        initRandSd = 0.01
        trainMode = TrainMode.Mode0
    }
}

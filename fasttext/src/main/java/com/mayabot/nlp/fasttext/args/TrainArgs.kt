package com.mayabot.nlp.fasttext.args

import com.mayabot.nlp.fasttext.loss.LossName
import java.io.File


class TrainArgs {

    // The following arguments for training are optional:
    /**
     * learn rate
     */
    var lr: Double? = null

    /**
     * change the rate of updates for the learning rate [100]
     */
    var lrUpdateRate: Int? = null

    /**
     * size of word vectors [100]
     */
    var dim: Int? = null

    /**
     * size of the context window [5]
     */
    var ws: Int? = null

    /**
     * number of epochs [5]
     */
    var epoch: Int? = null

    /**
     * number of negatives sampled [5]
     */
    var neg: Int? = null

    /**
     * loss function {ns, hs, softmax} [softmax]
     */
    var loss: LossName? = null

    /**
     * number of threads [12]
     */
    var thread: Int? = null

    /**
     * pretrained word vectors for supervised learning
     */
    var pretrainedVectors: File? = null


    // The following arguments for the dictionary are optional:
    var minCount :Int? = null
    var minCountLabel:Int? = null
    var wordNgrams :Int? = null
    var bucket:Int? = null
    var minn :Int? = null
    var maxn :Int? = null
    var t :Double? = null
    var label:String? = null

    var maxVocabSize:Int = 500000

    fun toComputedTrainArgs(model: ModelName) = ComputedTrainArgs(model,this)
}

class ComputedTrainArgs(val model:ModelName,trainArgs: TrainArgs){

    val maxVocabSize:Int = trainArgs.maxVocabSize

    val seed:Int = 0

    val thread = trainArgs.thread ?: Math.max(Runtime.getRuntime().availableProcessors() - 2, 2)

    val label = trainArgs.label ?: "__label__"

    val lr = trainArgs.lr ?: if(model == ModelName.sup) 0.1 else 0.05

    val preTrainedVectors:File? = trainArgs.pretrainedVectors

    val modelArgs = run {
        var temp = ModelArgs(model = model)

        if (model == ModelName.sup) {
            temp = temp.copy(
                    loss = LossName.softmax,
                    minCount = 1,
                    minn = 0,
                    maxn = 0
            )
        }
        trainArgs.minCount?.let {
            temp = temp.copy(minCount = it)
        }

        trainArgs.minCountLabel?.let {
            temp = temp.copy(minCountLabel = it)
        }

        trainArgs.wordNgrams?.let {
            temp = temp.copy(wordNgrams = it)
        }

        trainArgs.bucket?.let {
            temp = temp.copy(bucket = it)
        }

        trainArgs.minn?.let {
            temp = temp.copy(minn = it)
        }

        trainArgs.maxn?.let {
            temp = temp.copy(maxn = it)
        }
        trainArgs.t?.let {
            temp = temp.copy(t = it)
        }

        trainArgs.lrUpdateRate?.let {
            temp = temp.copy(lrUpdateRate = it)
        }

        trainArgs.dim?.let {
            temp = temp.copy(dim = it)
        }

        trainArgs.ws?.let {
            temp = temp.copy(ws = it)
        }

        trainArgs.epoch?.let {
            temp = temp.copy(epoch = it)
        }

        trainArgs.neg?.let {
            temp = temp.copy(neg = it)
        }

        trainArgs.loss?.let {
            temp = temp.copy(loss = it)
        }

        temp
    }
}

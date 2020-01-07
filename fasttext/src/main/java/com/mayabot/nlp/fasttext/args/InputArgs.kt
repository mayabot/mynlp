package com.mayabot.nlp.fasttext.args

import com.mayabot.nlp.fasttext.loss.LossName
import java.io.File


class InputArgs {

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
    var minCount: Int? = null
    var minCountLabel: Int? = null
    var wordNgrams: Int? = null
    var bucket: Int? = null
    var minn: Int? = null
    var maxn: Int? = null
    var t: Double? = null
    var label: String? = null

    var maxVocabSize: Int? = null
    val seed: Int? = null
    val preTrainedVectors:File? = null

    /**
     * validation file to be used for evaluation
     */
    val autotuneValidationFile: File? = null

    /**
     *  metric objective {f1, f1:labelname}
     */
    val autotuneMetric:String? = null

    /**
     * number of predictions used for evaluation
     */
    val autotunePredictions:Int? = null

    /**
     * maximum duration in seconds. default 5 minutes
     */
    val autotuneDuration:Int? = null

    /**
     * constraint model file size
     */
    val autotuneModelSize:String? = null


    fun parse(model: ModelName): Args {
        var temp = Args(model = model)

        if (model == ModelName.sup) {
            temp = temp.copy(
                    loss = LossName.softmax,
                    minCount = 1,
                    minn = 0,
                    maxn = 0,
                    lr = 0.1
            )
        }

        this.maxVocabSize?.let {
            temp.setManual("maxVocabSize")
            temp = temp.copy(maxVocabSize=it)
        }
        this.seed?.let {
            temp.setManual("seed")
            temp = temp.copy(seed=it)
        }
        this.preTrainedVectors?.let {
            temp.setManual("preTrainedVectors")
            temp = temp.copy(preTrainedVectors=it)
        }

        this.autotuneValidationFile?.let {
            temp.setManual("autotuneValidationFile")
            temp = temp.copy(autotuneValidationFile=it)
        }

        this.autotuneMetric?.let {
            temp.setManual("autotuneMetric")
            temp = temp.copy(autotuneMetric=it)
        }

        this.autotunePredictions?.let {
            temp.setManual("autotunePredictions")
            temp = temp.copy(autotunePredictions=it)
        }

        this.autotuneDuration?.let {
            temp.setManual("autotuneDuration")
            temp = temp.copy(autotuneDuration=it)
        }

        this.autotuneModelSize?.let {
            temp.setManual("autotuneModelSize")
            temp = temp.copy(autotuneModelSize=it)
        }

        this.lr?.let {
            temp.setManual("lr")
            temp = temp.copy(lr = it)
        }

        this.minCount?.let {
            temp.setManual("minCount")
            temp = temp.copy(minCount = it)
        }

        this.minCountLabel?.let {
            temp.setManual("minCountLabel")
            temp = temp.copy(minCountLabel = it)
        }

        this.wordNgrams?.let {
            temp.setManual("wordNgrams")
            temp = temp.copy(wordNgrams = it)
        }

        this.bucket?.let {
            temp.setManual("bucket")
            temp = temp.copy(bucket = it)
        }

        this.minn?.let {
            temp.setManual("minn")
            temp = temp.copy(minn = it)
        }

        this.maxn?.let {
            temp.setManual("maxn")
            temp = temp.copy(maxn = it)
        }
        this.t?.let {
            temp.setManual("t")
            temp = temp.copy(t = it)
        }

        this.lrUpdateRate?.let {
            temp.setManual("lrUpdateRate")
            temp = temp.copy(lrUpdateRate = it)
        }

        this.dim?.let {
            temp.setManual("dim")
            temp = temp.copy(dim = it)
        }

        this.ws?.let {
            temp.setManual("ws")
            temp = temp.copy(ws = it)
        }

        this.epoch?.let {
            temp.setManual("epoch")
            temp = temp.copy(epoch = it)
        }

        this.neg?.let {
            temp.setManual("neg")
            temp = temp.copy(neg = it)
        }

        this.loss?.let {
            temp.setManual("loss")
            temp = temp.copy(loss = it)
        }

        /////////////////////////////////////////////
        if (temp.wordNgrams <= 1 && temp.maxn == 0) {
            temp = temp.copy(bucket = 0)
        }

        return temp
    }

}
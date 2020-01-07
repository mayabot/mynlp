package com.mayabot.nlp.fasttext.args

import com.mayabot.nlp.fasttext.args.ComputedTrainArgs.Companion.kUnlimitedModelSize
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
    var minCount: Int? = null
    var minCountLabel: Int? = null
    var wordNgrams: Int? = null
    var bucket: Int? = null
    var minn: Int? = null
    var maxn: Int? = null
    var t: Double? = null
    var label: String? = null

    var maxVocabSize: Int = 500000

    private val manualArgs = HashSet<String>()

    /**
     * validation file to be used for evaluation
     */
    var autotuneValidationFile: File? = null

    /**
     *  metric objective {f1, f1:labelname}
     */
    var autotuneMetric:String = "f1"

    /**
     * number of predictions used for evaluation
     */
    var autotunePredictions:Int = 1

    /**
     * maximum duration in seconds. default 5 minutes
     */
    var autotuneDuration:Int = 60*5

    /**
     * constraint model file size
     */
    var autotuneModelSize:String = ""

    fun hasAutotune(): Boolean{
        val f = autotuneValidationFile
        return f!=null && f.exists()
    }
    fun isManual(argName:String):Boolean {
        return manualArgs.contains(argName)
    }

    fun setManual(argName: String) {
        manualArgs.add(argName)
    }

    enum class MetricName{
        f1score,labelf1score
    }

    fun getAutotuneMetric():MetricName{
        if (autotuneMetric.startsWith("f1:")) {
            return MetricName.labelf1score
        }else if (autotuneMetric == "f1") {
            return MetricName.f1score
        }
        error("Unknown metric : " + autotuneMetric)
    }
    fun getAutotuneMetricLabel(): String {
        if (getAutotuneMetric() == MetricName.labelf1score) {
            val label = autotuneMetric.substring(3)
            if (label.isEmpty()) {
                error("Empty metric label : " + autotuneMetric)
            }
            return label
        }
        return ""
    }
    fun getAutotuneModelSize(): Long {
        var modelSize = autotuneModelSize
        if (modelSize.isEmpty()) {
            return kUnlimitedModelSize.toLong()
        }
        val units = HashMap<Char,Int>()
        units += 'k' to 1000
        units += 'K' to 1000
        units += 'm' to 1000000
        units += 'M' to 1000000
        units += 'g' to 1000000000
        units += 'G' to 1000000000

        var multiplier = 1
        val lastCharacter = modelSize.last()
        if (units.containsKey(lastCharacter)) {
            multiplier = units[lastCharacter]!!
            modelSize = modelSize.substring(0,modelSize.length - 1)
        }
        var size: Long = modelSize.toLong()
        return size * multiplier
    }

    fun toComputedTrainArgs(model: ModelName) = ComputedTrainArgs(model, this)
}

class ComputedTrainArgs(val model: ModelName, trainArgs: TrainArgs) {

    val maxVocabSize: Int = trainArgs.maxVocabSize

    val seed: Int = 0

    val thread = trainArgs.thread ?: Math.max(Runtime.getRuntime().availableProcessors() - 2, 2)

    val label = trainArgs.label ?: "__label__"

    val lr = trainArgs.lr ?: if (model == ModelName.sup) 0.1 else 0.05

    val preTrainedVectors: File? = trainArgs.pretrainedVectors

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

        if (temp.wordNgrams <= 1 && temp.maxn == 0) {
            temp = temp.copy(bucket = 0)
        }

        temp
    }

    companion object {
        val kUnlimitedModelSize = -1.0f
    }
}

package com.mayabot.nlp.fasttext.args

import com.mayabot.nlp.fasttext.loss.LossName
import com.mayabot.nlp.fasttext.utils.AutoDataInput
import com.mayabot.nlp.fasttext.utils.writeDouble
import com.mayabot.nlp.fasttext.utils.writeInt
import java.io.DataInputStream
import java.io.File
import java.io.IOException
import java.nio.channels.FileChannel

data class Args(

        /**
         * minimal number of word occurrences [1]
         */
        val minCount: Int = 5,
        /**
         * minimal number of label occurrences [0]
         */
        val minCountLabel: Int = 0,
        /**
         * max length of word ngram [1]
         */
        val wordNgrams: Int = 1,
        /**
         * number of buckets [2000000]
         */
        val bucket: Int = 2000000,
        /**
         * min length of char ngram [0]
         */
        val minn: Int = 3,
        /**
         * max length of char ngram [0]
         */
        val maxn: Int = 6,

        /**
         * sampling threshold [0.0001]
         */
        val t: Double = 1e-4,

        // The following arguments for training are optional:
        /**
         * change the rate of updates for the learning rate [100]
         */
        val lrUpdateRate: Int = 100,
        /**
         * size of word vectors [100]
         */
        val dim: Int = 100,
        /**
         * size of the context window [5]
         */
        val ws: Int = 5,

        /**
         * number of epochs [5]
         */
        val epoch: Int = 5,

        /**
         * number of negatives sampled [5]
         */
        val neg: Int = 5,

        /**
         * loss function {ns, hs, softmax} [softmax]
         */
        val loss: LossName = LossName.ns,


        val model: ModelName = ModelName.sg,

        /////////////////////////////////////////////////////////

        val maxVocabSize: Int = 500000,

        val seed: Int = 0,

        val thread:Int =  Math.max(Runtime.getRuntime().availableProcessors() - 2, 2),

        val label: String = "__label__",

        val lr:Double =  0.05,

        val preTrainedVectors: File? = null,

        //////////////////////// autotune //////////////////////

        /**
         * validation file to be used for evaluation
         */
        var autotuneValidationFile: File? = null,

        /**
         *  metric objective {f1, f1:labelname}
         */
        var autotuneMetric:String = "f1",

        /**
         * number of predictions used for evaluation
         */
        var autotunePredictions:Int = 1,

        /**
         * maximum duration in seconds. default 5 minutes
         */
        var autotuneDuration:Int = 60*5,

        /**
         * constraint model file size
         */
        var autotuneModelSize:String = ""



) {

    private val manualArgs = HashSet<String>()

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
        val size: Long = modelSize.toLong()
        return size * multiplier
    }

    /**
     * 参数写入到Java模型文件里面
     */
    @Throws(IOException::class)
    fun save(ofs: FileChannel) {

        ofs.writeInt(dim)
        ofs.writeInt(ws)
        ofs.writeInt(epoch)
        ofs.writeInt(minCount)
        ofs.writeInt(neg)
        ofs.writeInt(wordNgrams)
        ofs.writeInt(loss.value)
        ofs.writeInt(model.value)
        ofs.writeInt(bucket)
        ofs.writeInt(minn)
        ofs.writeInt(maxn)
        ofs.writeInt(lrUpdateRate)
        ofs.writeDouble(t)
    }

    companion object {

        @Throws(IOException::class)
        fun load(file: File): Args {
            return file.inputStream().buffered().use { ins ->
                load(AutoDataInput(DataInputStream(ins)))
            }
        }

        @Throws(IOException::class)
        fun load(input: AutoDataInput) = Args (
                dim = input.readInt(),
                ws = input.readInt(),
                epoch = input.readInt(),
                minCount = input.readInt(),
                neg = input.readInt(),
                wordNgrams = input.readInt(),
                loss = LossName.fromValue(input.readInt()),
                model = ModelName.fromValue(input.readInt()),
                bucket = input.readInt(),
                minn = input.readInt(),
                maxn = input.readInt(),
                lrUpdateRate = input.readInt(),
                t = input.readDouble()
        )

        val kUnlimitedModelSize = -1.0f

    }

}



/**
 *
 */
enum class ModelName constructor(val value: Int) {

    /**
     * CBOW
     */
    cbow(1),

    /**
     * skipgram
     */
    sg(2),

    /**
     * supervised 文本分类模型
     */
    sup(3);


    companion object {

        @Throws(IllegalArgumentException::class)
        fun fromValue(vue: Int): ModelName {
            var value = vue
            try {
                value -= 1
                return values()[value]
            } catch (e: ArrayIndexOutOfBoundsException) {
                throw IllegalArgumentException("Unknown ModelName enum second :$value")
            }

        }
    }
}


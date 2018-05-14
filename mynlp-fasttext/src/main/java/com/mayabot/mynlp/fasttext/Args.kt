package com.mayabot.mynlp.fasttext


import java.io.IOException
import java.nio.channels.FileChannel

class Args {

    /**
     * size of word vectors [100]
     */
    var dim = 100

    var ws = 5
    var epoch = 5
    var minCount = 5
    var minCountLabel = 0
    var neg = 5
    var wordNgrams = 1
    @JvmField var loss = LossName.ns
    @JvmField var model = ModelName.sg
    var bucket = 2000000
    var minn = 3
    var maxn = 6
    var lrUpdateRate = 100
    var t = 1e-4

    //不保存的参数
    var thread = Math.max(Runtime.getRuntime().availableProcessors() - 2, 2)
    var label = "__label__"
    var verbose = 2
    var lr = 0.05

    var qout: Boolean = false

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

    @Throws(IOException::class)
    fun loadClang(input: AutoDataInput):Args {
        dim = input.readInt()
        ws = input.readInt()
        epoch = input.readInt()
        minCount = input.readInt()
        neg = input.readInt()
        wordNgrams = input.readInt()
        loss = LossName.fromValue(input.readInt())
        model = ModelName.fromValue(input.readInt())
        bucket = input.readInt()
        minn = input.readInt()
        maxn = input.readInt()
        lrUpdateRate = input.readInt()
        t = input.readDouble()
        return this
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("Args ")
        builder.append(", lr=")
        builder.append(lr)
        builder.append(", lrUpdateRate=")
        builder.append(lrUpdateRate)
        builder.append(", dim=")
        builder.append(dim)
        builder.append(", ws=")
        builder.append(ws)
        builder.append(", epoch=")
        builder.append(epoch)
        builder.append(", minCount=")
        builder.append(minCount)
        builder.append(", minCountLabel=")
        builder.append(minCountLabel)
        builder.append(", neg=")
        builder.append(neg)
        builder.append(", wordNgrams=")
        builder.append(wordNgrams)
        builder.append(", loss=")
        builder.append(loss)
        builder.append(", model=")
        builder.append(model)
        builder.append(", bucket=")
        builder.append(bucket)
        builder.append(", minn=")
        builder.append(minn)
        builder.append(", maxn=")
        builder.append(maxn)
        builder.append(", thread=")
        builder.append(thread)
        builder.append(", t=")
        builder.append(t)
        builder.append(", label=")
        builder.append(label)
        builder.append(", verbose=")
        builder.append(verbose)
        builder.append("]")
        return builder.toString()
    }

}

class TrainArgs {

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
    var pretrainedVectors: String = ""

    fun setLr(lr: Double?): TrainArgs {
        this.lr = lr
        return this
    }

    fun setLrUpdateRate(lrUpdateRate: Int?): TrainArgs {
        this.lrUpdateRate = lrUpdateRate
        return this
    }

    fun setDim(dim: Int?): TrainArgs {
        this.dim = dim
        return this
    }

    fun setWs(ws: Int?): TrainArgs {
        this.ws = ws
        return this
    }

    fun setEpoch(epoch: Int?): TrainArgs {
        this.epoch = epoch
        return this
    }

    fun setNeg(neg: Int?): TrainArgs {
        this.neg = neg
        return this
    }

    fun setLoss(loss: LossName): TrainArgs {
        this.loss = loss
        return this
    }

    fun setThread(thread: Int?): TrainArgs {
        this.thread = thread
        return this
    }

    fun setPretrainedVectors(pretrainedVectors: String): TrainArgs {
        this.pretrainedVectors = pretrainedVectors
        return this
    }
}


enum class LossName private constructor(var value: Int) {
    hs(1), ns(2), softmax(3);


    companion object {

        @Throws(IllegalArgumentException::class)
        fun fromValue(value: Int): LossName {
            var value = value
            try {
                value -= 1
                return values()[value]
            } catch (e: ArrayIndexOutOfBoundsException) {
                throw IllegalArgumentException("Unknown LossName enum second :$value")
            }

        }
    }
}


enum class ModelName private constructor(var value: Int) {

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
        fun fromValue(value: Int): ModelName {
            var value = value
            try {
                value -= 1
                return values()[value]
            } catch (e: ArrayIndexOutOfBoundsException) {
                throw IllegalArgumentException("Unknown ModelName enum second :$value")
            }

        }
    }
}
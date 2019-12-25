package com.mayabot.nlp.fasttext.args

import com.mayabot.nlp.fasttext.blas.AutoDataInput
import com.mayabot.nlp.fasttext.loss.LossName
import com.mayabot.nlp.fasttext.writeDouble
import com.mayabot.nlp.fasttext.writeInt
import java.io.IOException
import java.nio.channels.FileChannel

data class ModelArgs(
        //The following arguments for the dictionary are optional:
        /**
         * minimal number of word occurrences [1]
         */
        val minCount:Int = 5,
        /**
         * minimal number of label occurrences [0]
         */
        val minCountLabel:Int = 0,
        /**
         * max length of word ngram [1]
         */
        val wordNgrams:Int = 1,
        /**
         * number of buckets [2000000]
         */
        val bucket:Int = 2000000,
        /**
         * min length of char ngram [0]
         */
        val minn:Int = 3,
        /**
         * max length of char ngram [0]
         */
        val maxn:Int = 6,

        /**
         * sampling threshold [0.0001]
         */
        val t:Double = 1e-4,

        // The following arguments for training are optional:
        /**
         * change the rate of updates for the learning rate [100]
         */
        val lrUpdateRate:Int = 100,
        /**
         * size of word vectors [100]
         */
        val dim:Int = 100,
        /**
         * size of the context window [5]
         */
        val ws :Int= 5,

        /**
         * number of epochs [5]
         */
        val epoch:Int = 5,

        /**
         * number of negatives sampled [5]
         */
        val neg:Int = 5,

        /**
         * loss function {ns, hs, softmax} [softmax]
         */
        val loss: LossName = LossName.ns,


        val model: ModelName = ModelName.sg
){
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

    companion object{
        @Throws(IOException::class)
        fun loadFromCppModel(input: AutoDataInput)=  ModelArgs (
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
    }
}

package com.mayabot.nlp.fasttext

import com.mayabot.nlp.fasttext.args.ModelArgs
import com.mayabot.nlp.fasttext.args.ModelName
import com.mayabot.nlp.fasttext.blas.loadFloatArrayMatrix
import com.mayabot.nlp.fasttext.blas.loadFloatArrayMatrixCPP
import com.mayabot.nlp.fasttext.dictionary.Dictionary
import com.mayabot.nlp.fasttext.loss.createLoss
import com.mayabot.nlp.fasttext.quant.loadQuantMatrix
import com.mayabot.nlp.fasttext.utils.AutoDataInput
import java.io.DataInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.ByteOrder


/**
 * 从C语言版本的FastText产生的模型文件
 */
object CppFastTextSupport {

    /**
     * Load binary model file. 这个二进制版本是C语言版本的模型
     * 从流读取，因为生产环境可能从classpath里面读取模型文件
     * @param input C语言版本的模型的InputStream
     * @return FastTextModel
     * @throws Exception
     */
    @Throws(Exception::class)
    fun loadCModel(input: InputStream): FastText {

        val ins = DataInputStream(input.buffered(1024 * 1024))

        ins.use {
            val buffer = AutoDataInput(it, ByteOrder.LITTLE_ENDIAN)

            //check model
            val magic = buffer.readInt()
            val version = buffer.readInt()

            if (magic != 793712314) {
                throw RuntimeException("Model file has wrong file format!")
            }

            if (version > 12) {
                throw RuntimeException("Model file has wrong file format! version is $version")
            }

            //Args
            val args = run {
                var args_ = ModelArgs.load(buffer)

                if (version == 11 && args_.model == ModelName.sup) {
                    // backward compatibility: old supervised models do not use char ngrams.
                    args_ = args_.copy(maxn = 0)
                }
                args_
            }


            //dictionary
            val dictionary = Dictionary.loadModel(args, buffer)

//
//            var input: FloatMatrix = FloatMatrix.floatArrayMatrix(0, 0)
//            var qinput: QMatrix? = null
//

            val quantInput = buffer.readUnsignedByte() != 0
            val quant_ = quantInput


            val input = if (quantInput) {
                loadQuantMatrix(buffer)
            } else {
                loadFloatArrayMatrixCPP(buffer)
            }
//
            if (!quantInput && dictionary.isPruned()) {
                throw RuntimeException("Invalid model file.\n"
                        + "Please download the updated model from www.fasttext.cc.\n"
                        + "See issue #332 on Github for more information.\n")
            }

            val qout = buffer.readUnsignedByte() != 0

            val output = if (quantInput && qout) {
                loadQuantMatrix(buffer)
            } else {
                loadFloatArrayMatrixCPP(buffer)
            }

            val loss = createLoss(args, output, args.model, dictionary)

            val normalizeGradient = args.model == ModelName.sup

            return FastText(args, dictionary, Model(input, output, loss, normalizeGradient), quant_)
        }
    }

    /**
     * Load binary model file. 这个二进制版本是C语言版本的模型
     * @param modelPath
     * @return FastTextModel
     * @throws Exception
     */
    @Throws(Exception::class)
    fun load(modelFile: File): FastText {

        if (!(modelFile.exists() && modelFile.isFile && modelFile.canRead())) {
            throw IOException("Model file cannot be opened for loading!")
        }

        return loadCModel(modelFile.inputStream())
    }
}




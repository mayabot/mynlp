package com.mayabot.nlp.fasttext

import com.mayabot.nlp.fasttext.args.ModelArgs
import com.mayabot.nlp.fasttext.args.ModelName
import com.mayabot.nlp.fasttext.args.TrainArgs
import com.mayabot.nlp.fasttext.blas.*
import com.mayabot.nlp.fasttext.blas.Vector
import com.mayabot.nlp.fasttext.dictionary.Dictionary
import com.mayabot.nlp.fasttext.dictionary.EOS
import com.mayabot.nlp.fasttext.dictionary.buildFromFile
import com.mayabot.nlp.fasttext.loss.createLoss
import com.mayabot.nlp.fasttext.train.FastTextTrain
import com.mayabot.nlp.fasttext.train.FileSampleLineIterable
import com.mayabot.nlp.fasttext.train.SampleLine
import com.mayabot.nlp.fasttext.train.loadPreTrainVectors
import com.mayabot.nlp.fasttext.utils.AutoDataInput
import com.mayabot.nlp.fasttext.utils.IntArrayList
import com.mayabot.nlp.fasttext.utils.loggerln
import com.mayabot.nlp.fasttext.utils.openDataInputStream
import java.io.DataInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.ByteOrder
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.exp

class FastText(
        val args: ModelArgs,
        val dict: Dictionary,
        val model: Model,
        val quant: Boolean
) {

    private val input: Matrix = model.wi
    val output: Matrix = model.wo

    /**
     * 预测分类标签
     *
     * @param tokens
     * @param k
     * @return
     */
    fun predict(tokens: List<String>, k: Int, threshold: Float): List<ScoreLabelPair> {

        // 要附加一个EOS标记

        val tokens2 = tokens.toMutableList()
        tokens2.add(EOS)

        val words = IntArrayList()
        val labels = IntArrayList()

        dict.getLine(tokens2, words, labels)

        if (words.isEmpty) {
            return emptyList()
        }

        val result = predict(k, words, threshold)


        return result.map { x -> ScoreLabelPair(exp(x.score), dict.getLabel(x.id)) }
    }

    fun predict(k: Int, words: IntArrayList, threshold: Float): List<ScoreIdPair> {
        if (words.isEmpty) {
            return emptyList()
        }

        if (args.model != ModelName.sup) {
            error("Model needs to be supervised for prediction!")
        }
        val state = Model.State(args.dim, dict.nlabels, 0)
        val predictions = ArrayList<ScoreIdPair>()
        model.predict(words, k, threshold, predictions, state)

        return predictions
    }


    private fun findNN(wordVectors: DenseMatrix, queryVec: Vector, k: Int, sets: Set<String>): List<ScoreLabelPair> {

        var queryNorm = queryVec.norm2()
        if (abs(queryNorm) < 1e-8) {
            queryNorm = 1f
        }

        val mostSimilar = (0 until k).map { ScoreLabelPair(-1f, "") }.toList().toTypedArray()
        val mastSimilarLast = mostSimilar.size - 1

        for (i in 0 until dict.nwords) {
            val dp = wordVectors[i] * queryVec / queryNorm
            val last = mostSimilar[mastSimilarLast]
            if (dp > last.score) {
                last.score = dp
                last.label = dict.getWord(i)

                mostSimilar.sortByDescending { it.score }
            }
        }

        val result = ArrayList<ScoreLabelPair>()
        for (r in mostSimilar) {
            if (r.score != -1f && !sets.contains(r.label)) {
                result.add(r)
            }
        }

        return result
    }

    private val wordVectors: DenseMatrix by lazy {
        /**
         * 计算所有词的向量。
         * 之所以向量都除以norm进行归一化。因为使用者。使用dot表达相似度，也会除以query vector的norm。然后归一化。
         * 最后距离结构都是0 ~ 1 的数字
         * @param wordVectors
         */
        fun preComputeWordVectors(wordVectors: DenseMatrix) {
            val vec = floatArrayVector(args.dim)
            wordVectors.zero()
            for (i in 0 until dict.nwords) {
                val word = dict.getWord(i)
                getWordVector(vec, word)
                val norm = vec.norm2()
                if (norm > 0) {
                    wordVectors[i] += 1.0f / norm to vec
                }
            }
        }

        val matrix = floatArrayMatrix(dict.nwords, args.dim)
        val s1 = System.currentTimeMillis()
        preComputeWordVectors(matrix)
        val s2 = System.currentTimeMillis()
        loggerln("Init wordVectors martix use time ${s2 - s1} ms")
        matrix
    }


    /**
     * NearestNeighbor
     */
    fun nearestNeighbor(wordQuery: String, k: Int): List<ScoreLabelPair> {
        val queryVec = getWordVector(wordQuery)
        val sets = HashSet<String>()
        sets.add(wordQuery)
        return findNN(wordVectors, queryVec, k, sets)
    }

    /**
     * Query triplet (A - B + C)?
     * @param A
     * @param B
     * @param C
     * @param k
     */
    fun analogies(A: String, B: String, C: String, k: Int): List<ScoreLabelPair> {

        val buffer = floatArrayVector(args.dim)
        val query = floatArrayVector(args.dim)

        getWordVector(buffer, A)
        query += buffer

        getWordVector(buffer, B)
        query += -1f to buffer

        getWordVector(buffer, C)
        query += buffer

        val sets = hashSetOf(A, B, C)

        return findNN(wordVectors, query, k, sets)
    }


    /**
     * 把词向量填充到一个Vector对象里面去
     *
     * @param vec
     * @param word
     */
    fun getWordVector(vec: Vector, word: String) {
        vec.zero()
        val ngrams = dict.getSubwords(word)
        val buffer = ngrams.buffer
        var i = 0
        val len = ngrams.size()
        while (i < len) {
            addInputVector(vec, buffer[i])
            i++
        }

        if (ngrams.size() > 0) {
            vec *= 1.0f / ngrams.size()
        }
    }

    fun getWordVector(word: String): Vector {
        val vec = floatArrayVector(args.dim)
        getWordVector(vec, word)
        return vec
    }

    fun test(file: File, k: Int = 1, threshold: Float = 0.0f): Meter {
        return test(FileSampleLineIterable(file),k,threshold)
    }

    fun test(file: Iterable<SampleLine>, k: Int = 1, threshold: Float = 0.0f): Meter {
        val line = IntArrayList()
        val labels = IntArrayList()
        val meter = Meter()
//        val state = Model.State(args.dim,dict.nlabels,0)
        for (sample in file) {
            line.clear()
            labels.clear()
            dict.getLine(sample.words, line, labels)
            if (!labels.isEmpty && !line.isEmpty) {
                val predictions = predict(k, line, threshold)
                meter.log(labels, predictions)

//                if (labels[0] == 1 && predictions[0].id == 1) {
//
//                    line.forEach2 {
//                        loggerln(dict.getWord(it))
//                    }
//                    loggerln("----")
//                }
            }
        }
        meter.print(dict, k, true)
        return meter
    }


    /**
     * 计算句子向量
     * @return 句子向量
     */
    fun getSentenceVector(tokens: Iterable<String>): Vector {
        val svec = floatArrayVector(args.dim)
        getSentenceVector(svec, tokens)
        return svec
    }


    /**
     * 句子向量
     *
     * @param svec
     * @param tokens
     */
    private fun getSentenceVector(svec: Vector, tokens: Iterable<String>) {
        svec.zero()
        if (args.model == ModelName.sup) {
            val line = IntArrayList()
            val labels = IntArrayList()
            dict.getLine(tokens, line, labels)

            for (i in 0 until line.size()) {
                addInputVector(svec, line.get(i))
            }

            if (!line.isEmpty) {
                svec *= (1.0f / line.size())
            }
        } else {
            val vec = floatArrayVector(args.dim)
            var count = 0
            for (word in tokens) {
                getWordVector(vec, word)
                val norm = vec.norm2()
                if (norm > 0) {
                    vec *= (1.0f / norm)
                    svec += vec
                    count++
                }
            }
            if (count > 0) {
                svec *= (1.0f / count)
            }
        }
    }

    private fun addInputVector(vec: Vector, ind: Int) {
        input.addRowToVector(vec, ind)
    }


    /**
     * 把词向量另存为文本格式
     *
     * @param file
     */
    @Throws(Exception::class)
    fun saveVectors(path: String) {
        var fileName = path
        if (!fileName.endsWith("vec")) {
            fileName += ".vec"
        }

        val file = File(fileName).apply {
            if (exists()) delete()
            if (parentFile != null) parentFile.mkdirs()
        }

        val vec = floatArrayVector(args.dim)
        val df = DecimalFormat("0.#####")

        file.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write("${dict.nwords} ${args.dim}\n")
            for (i in 0 until dict.nwords) {
                val word = dict.getWord(i)
                getWordVector(vec, word)
                writer.write(word)
                writer.write(" ")
                for (j in 0 until vec.length()) {
                    writer.write(df.format(vec[j].toDouble()))
                    writer.write(" ")
                }
                writer.write("\n")
            }
        }


    }


    /**
     * 保存为自有的文件格式(多文件）
     */
    @Throws(Exception::class)
    fun saveModel(file: String) {
        val path = File(file)
        if (path.exists()) {
            path.deleteRecursively()
        }
        path.mkdirs()

        //dict
        File(path, "dict.bin").outputStream().channel.use {
            dict.save(it)
        }

        //args
        File(path, "args.bin").outputStream().channel.use {
            args.save(it)
        }

        val qInputFlag = if (input is QuantMatrix) "q" else ""
        input.save(File(path, "${qInputFlag}input.matrix"))

        val qOutputFlag = if (output is QuantMatrix) "q" else ""
        output.save(File(path, "${qOutputFlag}output.matrix"))

    }

    /**
     * 对分类模型进行压缩
     */
    @JvmOverloads
    fun quantize(dsub: Int = 2,
//                 cutoff:Int=0,
//                 retrain:Int,
                 qnorm: Boolean = false,
                 qout: Boolean = false): FastText {

//        fun selectEmbedding(): IntArray{
//            val norms = floatArrayVector(input.row)
//            (input as DenseMatrix).l2NormRow(norms)
//            val idx = IntArray(input.row){0}
//            iota(idx)
//            val eosid = dict.getWordId(EOS)
//
//            idx.sortedWith(Comparator { i1, i2 ->
//                if (i1 == eosid && i2 == eosid) {
//                    1
//                }else{
//                    if(eosid == i1){
//                        -1
//                    }else{
//                        if(eosid !=i2){
//                            -(norms[i1].compareTo(i2))
//                        }else{
//                            -1
//                        }
//                    }
//                }
//            })
//
//            return idx.takeLast(idx.size-cutoff).toIntArray()
//        }

        if (this.args.model != ModelName.sup) {
            error("For now we only support quantization of supervised models")
        }

        val normalizeGradient = args.model == ModelName.sup

        // 暂时不实现cutoff
//        if (cutoff > 0 && cutoff < input.row) {
//            val idx = selectEmbedding()
//
//        }

        val input_ = buildQMatrix(this.input as DenseMatrix, dsub, qnorm)

        var output_: Matrix = this.output
        var loss_ = this.model.loss
        if (qout) {
            output_ = buildQMatrix(this.output as DenseMatrix, 2, qnorm)
            loss_ = createLoss(args, output_, args.model, this.dict)
        }

        return FastText(args, dict, Model(input_, output_, loss_, normalizeGradient), true)
    }


    companion object {

        @JvmStatic
        fun trainSupervised(file: File, trainArgs: TrainArgs = TrainArgs()) = train(file, ModelName.sup, trainArgs)

        @JvmStatic
        fun trainCow(file: File, trainArgs: TrainArgs = TrainArgs()) = train(file, ModelName.cbow, trainArgs)

        @JvmStatic
        fun trainSkipgram(file: File, trainArgs: TrainArgs = TrainArgs()) = train(file, ModelName.sg, trainArgs)


        @JvmStatic
        fun train(file: File, modelName: ModelName, trainArgs: TrainArgs): FastText {

            val args = trainArgs.toComputedTrainArgs(modelName)

            fun prepareSources(): List<Iterable<SampleLine>> {
                val parent = FileSampleLineIterable(file)
                var thread = args.thread

                val Size50M = 100 * 1024 * 1024
                var lines = -1
                if (file.length() < Size50M) {
                    lines = parent.lines()

                    // 数量太少
                    if (lines <= thread * 10) {
                        thread = 1
                    }

                }

                return if (thread == 1) {
                    if (file.length() < Size50M) {
                        listOf(parent.toMemList())
                    } else {
                        listOf(parent)
                    }
                } else {
                    if (file.length() < Size50M) {
                        parent.splitMutiFiles(thread).map { it.toMemList() }
                    } else {
                        parent.splitMutiFiles(thread)
                    }
                }

            }

            return train(prepareSources(),modelName,trainArgs)
        }

        @JvmStatic
        fun train(sources: List<Iterable<SampleLine>>, modelName: ModelName, trainArgs: TrainArgs): FastText {

            val args = trainArgs.toComputedTrainArgs(modelName)
            val modelArgs = args.modelArgs

            try {
                val dict = buildFromFile(args, sources, args.maxVocabSize)

                val input = if (args.preTrainedVectors != null) {
                    loadPreTrainVectors(dict, args.preTrainedVectors, args)
                } else {
                    floatArrayMatrix(dict.nwords + modelArgs.bucket, modelArgs.dim)
                            .apply {
                                uniform(1.0f / modelArgs.dim)
                            }
                }

                dict.init()

                val output = floatArrayMatrix(
                        if (ModelName.sup == args.model) dict.nlabels else dict.nwords,
                        modelArgs.dim
                ).apply {
                    zero()
                }

                val loss = createLoss(modelArgs, output, args.model, dict)
                val normalizeGradient = args.model == ModelName.sup

                val model = Model(input, output, loss, normalizeGradient)

                val fastText = FastText(modelArgs, dict, model, false)

                FastTextTrain(args, fastText).startThreads(sources)

                return fastText

            } finally {

                for (source in sources) {
                    if (source is FileSampleLineIterable) {
                        source.file.delete()
                    }
                }
            }
        }

        fun loadCppModel(file: File): FastText {
            return CppFastTextSupport.load(file)
        }

        fun loadCppModel(ins: InputStream): FastText {
            return CppFastTextSupport.loadCModel(ins)
        }

        /**
         * 加载Java模型,[moduleDir]是目录
         */
        fun loadModel(moduleDir: File, mmap: Boolean = false): FastText {

            check(moduleDir.exists() && moduleDir.isDirectory)

            val args = ModelArgs.load(File(moduleDir, "args.bin"))

            val dict = File(moduleDir, "dict.bin").openDataInputStream().use {
                Dictionary.loadModel(args, AutoDataInput(it))
            }

            val quant = File(moduleDir, "qinput.matrix").exists()

            val input = if (quant) {
                loadQuantMatrix(File(moduleDir, "qinput.matrix"))
            } else {
                loadDenseMatrix(File(moduleDir, "input.matrix"), mmap)
            }


            if (!quant && dict.isPruned()) {
                error("Invalid model file.\n" +
                        "Please download the updated model from www.fasttext.cc.\n" +
                        "See issue #332 on Github for more information.\n")
            }

            val output = if (File(moduleDir, "qoutput.matrix").exists()) {
                loadQuantMatrix(File(moduleDir, "qoutput.matrix"))
            } else {
                loadDenseMatrix(File(moduleDir, "output.matrix"), mmap)
            }

            val loss = createLoss(args, output, args.model, dict)

            val normalizeGradient = args.model == ModelName.sup

            return FastText(args, dict, Model(input, output, loss, normalizeGradient), quant)
        }

    }

}

data class ScoreIdPair(val score: Float, val id: Int) {
    override fun toString(): String {
        return "[$id,$score]"
    }
}

data class ScoreLabelPair(var score: Float, var label: String) {
    override fun toString(): String {
        return "[$label,$score]"
    }
}

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
    fun loadCModel(inputStream: InputStream): FastText {

        val ins = DataInputStream(inputStream.buffered(1024 * 1024))

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
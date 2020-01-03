package com.mayabot.nlp.fasttext

import com.carrotsearch.hppc.IntArrayList
import com.google.common.base.Charsets
import com.google.common.base.Stopwatch
import com.google.common.collect.ImmutableList
import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.mayabot.nlp.fasttext.args.ModelArgs
import com.mayabot.nlp.fasttext.args.ModelName
import com.mayabot.nlp.fasttext.args.TrainArgs
import com.mayabot.nlp.fasttext.blas.*
import com.mayabot.nlp.fasttext.blas.Vector
import com.mayabot.nlp.fasttext.dictionary.Dictionary
import com.mayabot.nlp.fasttext.dictionary.EOS
import com.mayabot.nlp.fasttext.dictionary.buildFromFile
import com.mayabot.nlp.fasttext.loss.createLoss
import com.mayabot.nlp.fasttext.quant.QuantMatrix
import com.mayabot.nlp.fasttext.quant.buildQMatrix
import com.mayabot.nlp.fasttext.quant.loadQuantMatrix
import com.mayabot.nlp.fasttext.train.*
import com.mayabot.nlp.fasttext.utils.AutoDataInput
import com.mayabot.nlp.fasttext.utils.openDataInputStream
import java.io.File
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.exp

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

@ExperimentalUnsignedTypes
class FastText(
        val args: ModelArgs,
        val dict: Dictionary,
        val model: Model,
        val quant: Boolean
) {

    val input: Matrix = model.wi
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
        val tokens2 = Iterables.concat(tokens, listOf(EOS))

        val words = IntArrayList()
        val labels = IntArrayList()

        dict.getLine(tokens2, words, labels)

        if (words.isEmpty) {
            return ImmutableList.of()
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

        val result = Lists.newArrayList<ScoreLabelPair>()
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
        val stopwatch = Stopwatch.createStarted()
        preComputeWordVectors(matrix)
        stopwatch.stop()
        println("Init wordVectors martix use time ${stopwatch.elapsed(TimeUnit.MILLISECONDS)} ms")
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

        val sets = Sets.newHashSet(A, B, C)

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
        val line = IntArrayList()
        val labels = IntArrayList()
        val meter = Meter()
//        val state = Model.State(args.dim,dict.nlabels,0)
        for (sample in TrainSampleList(file)) {
            line.clear()
            labels.clear()
            dict.getLine(sample.words, line, labels)
            if (!labels.isEmpty && !line.isEmpty) {
                val predictions = predict(k, line, threshold)
                meter.log(labels, predictions)

//                if (labels[0] == 1 && predictions[0].id == 1) {
//
//                    line.forEach2 {
//                        println(dict.getWord(it))
//                    }
//                    println("----")
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
//        if (quant) {
//            model.qinput.addToVector(vec, ind)
//        } else {
//            vec += input[ind]
//        }

        // vec += input[ind]
        input.addRowToVector(vec, ind)
    }


    /**
     * 把词向量另存为文本格式
     *
     * @param file
     */
    @Throws(Exception::class)
    fun saveVectors(file: String) {
        var fileName = file
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
        fun trainSupervised(file: File, trainArgs: TrainArgs = TrainArgs(), wordSplitter: WordSplitter = whitespaceSplitter) = train(file, ModelName.sup, trainArgs, wordSplitter)

        @JvmStatic
        fun trainCow(file: File, trainArgs: TrainArgs = TrainArgs(), wordSplitter: WordSplitter = whitespaceSplitter) = train(file, ModelName.cbow, trainArgs, wordSplitter)

        @JvmStatic
        fun trainSkipgram(file: File, trainArgs: TrainArgs = TrainArgs(), wordSplitter: WordSplitter = whitespaceSplitter) = train(file, ModelName.sg, trainArgs, wordSplitter)

        @JvmStatic
        fun train(file: File, modelName: ModelName, trainArgs: TrainArgs, wordSplitter: WordSplitter): FastText {
            val args = trainArgs.toComputedTrainArgs(modelName)
            val modelArgs = args.modelArgs

            val sources: List<TrainSampleList> = processAndSplit(file, wordSplitter, args.thread)

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

            for (source in sources) {
                source.file.delete()
            }

            return fastText
        }

        /**
         * 加载Java模型,[file]是目录
         */
        fun loadModel(file: File, mmap: Boolean = false): FastText {

            check(file.exists() && file.isDirectory)

            val args = ModelArgs.load(File(file, "args.bin"))

            val dict = File(file, "dict.bin").openDataInputStream().use {
                Dictionary.loadModel(args, AutoDataInput(it))
            }

            val quant = File(file, "qinput.matrix").exists()

            val input = if (quant) {
                loadQuantMatrix(File(file, "qinput.matrix"))
            } else {
                loadDenseMatrix(File(file, "input.matrix"), mmap)
            }


            if (!quant && dict.isPruned()) {
                error("Invalid model file.\n" +
                        "Please download the updated model from www.fasttext.cc.\n" +
                        "See issue #332 on Github for more information.\n")
            }

            val output = if (File(file, "qoutput.matrix").exists()) {
                loadQuantMatrix(File(file, "qoutput.matrix"))
            } else {
                loadDenseMatrix(File(file, "output.matrix"), mmap)
            }

            val loss = createLoss(args, output, args.model, dict)

            val normalizeGradient = args.model == ModelName.sup

            return FastText(args, dict, Model(input, output, loss, normalizeGradient), quant)
        }
    }


}
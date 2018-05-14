package com.mayabot.mynlp.fasttext

import com.carrotsearch.hppc.IntArrayList
import com.google.common.base.Charsets
import com.google.common.base.Stopwatch
import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.google.common.io.Files
import com.google.common.primitives.Floats
import fasttext.QMatrix
import fasttext.pages
import java.io.DataInputStream
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.exp
import kotlin.system.exitProcess

const val FASTTEXT_VERSION = 12
const val FASTTEXT_FILEFORMAT_MAGIC_INT32 = 793712314

data class FloatIntPair(@JvmField var first: Float, @JvmField var second: Int)
data class FloatStringPair(@JvmField var first: Float, @JvmField var second: String)

class FastText(internal val args: Args,
               internal val dict: Dictionary,
               internal val input: FloatMatrix,
               internal val output: FloatMatrix,
               internal val model: Model,
               /**
                * 是否量化
                */
               internal var quant: Boolean = false,
               internal val qinput: QMatrix = QMatrix(),
               internal val qoutput: QMatrix? = null) {

    /**
     * 预测分类标签
     *
     * @param tokens
     * @param k
     * @return
     */
    fun predict(tokens: Iterable<String>, k: Int): List<FloatStringPair> {
        val words = IntArrayList()
        val labels = IntArrayList()

        dict.getLine(tokens, words, labels)

        if (words.isEmpty) {
            return ImmutableList.of()
        }
        val hidden = MutableByteBufferVector(args.dim)
        val output = MutableByteBufferVector(dict.nlabels())

        val modelPredictions = Lists.newArrayListWithCapacity<FloatIntPair>(k)

        model.predict(words, k, modelPredictions, hidden, output)

        return modelPredictions.map { x -> FloatStringPair(x.first, dict.getLabel(x.second)) }
    }


    private fun findNN(wordVectors: FloatMatrix, queryVec: Vector, k: Int, sets: Set<String>): List<FloatStringPair> {

        var queryNorm = queryVec.norm2()
        if (Math.abs(queryNorm) < 1e-8) {
            queryNorm = 1f
        }

        val mostSimilar = (0 until k).map { FloatStringPair(-1f,"") }.toList().toTypedArray()
        val mastSimilarLast = mostSimilar.size - 1

        for (i in 0 until dict.nwords()) {
            val dp = wordVectors[i] *queryVec / queryNorm
            val last = mostSimilar[mastSimilarLast]
            if (dp > last.first) {
                last.first = dp
                last.second = dict.getWord(i)

                mostSimilar.sortByDescending { it.first }
            }
        }

        val result = Lists.newArrayList<FloatStringPair>()
        for (r in mostSimilar) {
            if (r.first != -1f && !sets.contains(r.second)) {
                result.add(r)
            }
        }

        return result
    }

    lateinit var wordVectors: FloatMatrix

    /**
     * NearestNeighbor
     */
    fun nearestNeighbor(wordQuery: String, k: Int): List<FloatStringPair> {
        if (!this::wordVectors.isInitialized) {
            val stopwatch = Stopwatch.createStarted()
            wordVectors = FloatMatrix.floatArrayMatrix(dict.nwords,args.dim).apply {
                precomputeWordVectors(this)
            }
            stopwatch.stop()
            println("Init wordVectors martix use time ${stopwatch.elapsed(TimeUnit.MILLISECONDS)} ms")
        }
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
    fun analogies(A: String, B: String, C: String, k: Int): List<FloatStringPair> {
        if (!this::wordVectors.isInitialized) {
            val stopwatch = Stopwatch.createStarted()
            wordVectors = FloatMatrix.floatArrayMatrix(dict.nwords,args.dim).apply {
                precomputeWordVectors(this)
            }
            stopwatch.stop()
            println("Init wordVectors martix use time ${stopwatch.elapsed(TimeUnit.MILLISECONDS)} ms")
        }

        val buffer = Vector.floatArrayVector(args.dim)
        val query = Vector.floatArrayVector(args.dim)

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
     * 计算所有词的向量。
     * 之所以向量都除以norm进行归一化。因为使用者。使用dot表达相似度，也会除以query vector的norm。然后归一化。
     * 最后距离结构都是0 ~ 1 的数字
     * @param wordVectors
     */
    fun precomputeWordVectors(wordVectors: MutableFloatMatrix) {
        val vec = Vector.floatArrayVector(args.dim)
        wordVectors.fill(0f)
        for (i in 0 until dict.nwords()) {
            val word = dict.getWord(i)
            getWordVector(vec, word)
            val norm = vec.norm2()
            if (norm > 0) {
                wordVectors[i] += 1.0f/norm to vec
                //wordVectors.addRow(vec, i, 1.0f / norm)
            }
        }
    }

        /**
     * 把词向量填充到一个Vector对象里面去
     *
     * @param vec
     * @param word
     */
    fun getWordVector(vec: MutableVector, word: String) {
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
        val vec = MutableByteBufferVector(args.dim)
        getWordVector(vec, word)
        return vec
    }


    fun getSentenceVector(tokens: Iterable<String>): Vector {
        val svec = MutableByteBufferVector(args.dim)
        getSentenceVector(svec, tokens)
        return svec
    }


    /**
     * 句子向量
     *
     * @param svec
     * @param tokens
     */
    private fun getSentenceVector(svec: MutableVector, tokens: Iterable<String>) {
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
            val vec = MutableByteBufferVector(args.dim)
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

    private fun addInputVector(vec: MutableVector, ind: Int) {
        if (quant) {
            qinput.addToVector(vec, ind)
        } else {
            vec += input[ind]
        }
    }


    /**
     * 保存词到向量文本
     *
     * @param file
     */
    @Throws(Exception::class)
    fun saveVectors(fileName: String) {
        var fileName = fileName
        if (!fileName.endsWith("vec")) {
            fileName += ".vec"
        }

        val file = File(fileName)
        if (file.exists()) {
            file.delete()
        }
        if (file.parentFile != null) {
            file.parentFile.mkdirs()
        }

        val vec = MutableByteBufferVector(args.dim)
        val df = DecimalFormat("0.#####")

        Files.asByteSink(file).asCharSink(Charsets.UTF_8).openBufferedStream().use { writer ->
            writer.write("${dict.nwords()} ${args.dim}\n")
            for (i in 0 until dict.nwords()) {
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

    @Throws(Exception::class)
    fun saveModel(path: String) {
        var path = File(path)
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

        if (!quant) {
            //input float matrix
            File(path, "input.matrix").outputStream().channel.use {
                it.writeInt(input.rows())
                it.writeInt(input.cols())
                input.write(it)
            }
        } else {
            File(path, "qinput.matrix").outputStream().channel.use {
                qinput.save(it)
            }
        }

        if (quant && args.qout) {
            File(path, "qoutput.matrix").outputStream().channel.use {
                qoutput!!.save(it)
            }
        } else {
            File(path, "output.matrix").outputStream().channel.use {
                it.writeInt(output.rows())
                it.writeInt(output.cols())
                output.write(it)
            }
        }
    }


    companion object {

        /**
         * 加载facebook官方C程序保存的文件模型，支持bin和ftz模型
         *
         * @param modelPath
         * @throws IOException
         */
        @JvmStatic
        @Throws(Exception::class)
        fun loadFasttextBinModel(modelPath: String): FastText {
            return LoadFastTextFromClangModel.loadCModel(modelPath)
        }

        /**
         * 加载java程序保存的文件模型.
         * path应该是一个目录，下面保存各个细节的文件
         */
        @JvmOverloads
        @JvmStatic
        fun loadModel(modelPath: String, mmap: Boolean = true): FastText {
            val dir = File(modelPath)

            if (!dir.exists() || dir.isFile) {
                println("error file $dir")
                exitProcess(0)
            }

            val args = Args().loadClang(File(dir, "args.bin").openAutoDataInput())

            val dictionary = Dictionary(args).load(File(dir, "dict.bin").openAutoDataInput())

            fun loadMatrix(file: File): FloatMatrix {

                return if (mmap) {
                    file.inputStream().channel.use {
                        val rows = it.readInt()
                        val cols = it.readInt()

                        //一个区域可以容纳多少行
                        var areaRows = 0
                        while (areaRows * cols < 268435456) {
                            areaRows += 10
                        }

                        val fileSize = it.size()
                        val arrayBytes = fileSize - 8
                        val areaCount = pages(arrayBytes, 4 * areaRows * cols)
                        val areaBytes = areaRows * cols * 4
                        val lastBytes = arrayBytes % (areaRows * cols * 4)

                        val list = ArrayList<ByteBuffer>()
                        for (a in 0 until areaCount) {
                            val len = if (a == areaCount - 1) lastBytes else areaBytes.toLong()
                            list += it.map(FileChannel.MapMode.READ_ONLY, 8 + a.toLong() * areaBytes, len)
                        }
                        AreaByteBufferMatrix(rows, cols, list)
                    }
                } else {
                    val dataInput = file.openAutoDataInput()
                    val rows = dataInput.readInt()
                    val cols = dataInput.readInt()
                    val floatArray = FloatArray(rows * cols)
                    for (i in 0 until rows * cols) {
                        floatArray[i] = dataInput.readFloat()
                    }
                    FloatMatrix.readOnlyFloatArrayMatrix(rows, cols, floatArray)
                }

            }

            val quant = File(dir, "qinput.matrix").exists()

            var input: FloatMatrix = FloatMatrix.floatArrayMatrix(0, 0)
            var qinput: QMatrix? = null

            if (quant) {
                qinput = QMatrix.load(File(dir, "qinput.matrix").openAutoDataInput())
            } else {
                input = loadMatrix(File(dir, "input.matrix"))
            }

            val quantInput = quant
            if (!quantInput && dictionary.isPruned()) {
                throw RuntimeException("Invalid model file.\n"
                        + "Please download the updated model from www.fasttext.cc.\n"
                        + "See issue #332 on Github for more information.\n")
            }

            var output: FloatMatrix = FloatMatrix.floatArrayMatrix(0, 0)
            var qoutput: QMatrix? = null

            args.qout = File(dir, "qoutput.matrix").exists()
            if (quant && args.qout) {
                qoutput = QMatrix.load(File(dir, "qoutput.matrix").openAutoDataInput())
            } else {
                output = loadMatrix(File(dir, "output.matrix"))
            }

            val model = Model(input, output, args, 0)
            model.quant = quantInput

            model.setQuantizePointer(qinput, qoutput, args.qout)


            if (args.model == ModelName.sup) {
                model.setTargetCounts(dictionary.getCounts(EntryType.label))
            } else {
                model.setTargetCounts(dictionary.getCounts(EntryType.word))
            }


            return if (model.quant) {
                FastText(args, dictionary, input, output, model, true, qinput!!, qoutput)
            } else {
                FastText(args, dictionary, input, output, model)
            }
        }



        @JvmOverloads
        @Throws(Exception::class)
        @JvmStatic
        fun train(trainFile: File, model_name: ModelName = ModelName.sup, args: TrainArgs = TrainArgs()): FastText {
            return FastTextTrain().train(trainFile, model_name, args)
        }
    }
}

class Model(private val inputMatrix: FloatMatrix
            , private val outputMatrix: FloatMatrix,
            args_: Args,
            seed: Int) : BaseModel(args_, seed, outputMatrix.rows()) {

    var quant: Boolean = false

    private val hsz: Int = args_.dim // dim

    private val comparePairs = { o1: FloatIntPair, o2: FloatIntPair -> Floats.compare(o2.first, o1.first) }

    private var qinput = QMatrix()
    private var qoutput = QMatrix()


    fun setQuantizePointer(qinput: QMatrix?, qoutput: QMatrix?, qout: Boolean) {
        qinput?.let {
            this.qinput = qinput
        }
        qoutput?.let {
            this.qoutput = it
            if (qout) {
                this.osz = qoutput.m
            }
        }
    }

    fun predict(input: IntArrayList, k: Int,
                heap: MutableList<FloatIntPair>,
                hidden: MutableVector,
                output: MutableVector) {
        checkArgument(k > 0)
        //		if (heap instanceof ArrayList) {
        //			((ArrayList<FloatIntPair>) heap).ensureCapacity(k + 1);
        //		}
        computeHidden(input, hidden)
        if (args_.loss == LossName.hs) {
            dfs(k, 2 * osz - 2, 0.0f, heap, hidden)
        } else {
            findKBest(k, heap, hidden, output)
        }
        Collections.sort(heap, comparePairs)
    }

    fun findKBest(k: Int, heap: MutableList<FloatIntPair>, hidden: Vector, output: MutableVector) {
        computeOutputSoftmax(hidden, output)
        for (i in 0 until osz) {
            if (heap.size == k && log(output.get(i)) < heap[heap.size - 1].first) {
                continue
            }
            heap.add(FloatIntPair(log(output.get(i)), i))
            Collections.sort(heap, comparePairs)
            if (heap.size > k) {
                Collections.sort(heap, comparePairs)
                heap.removeAt(heap.size - 1) // pop last
            }
        }
    }

    fun dfs(k: Int, node: Int, score: Float, heap: MutableList<FloatIntPair>, hidden: Vector) {
        if (heap.size == k && score < heap[heap.size - 1].first) {
            return
        }

        if (tree[node].left == -1 && tree[node].right == -1) {
            heap.add(FloatIntPair(score, node))
            Collections.sort(heap, comparePairs)
            if (heap.size > k) {
                Collections.sort(heap, comparePairs)
                heap.removeAt(heap.size - 1) // pop last
            }
            return
        }

//        val f = sigmoid(outputMatrix.dotRow(hidden, node - osz))
        var f = if (quant && args_.qout) {
            qoutput.dotRow(hidden, node - osz)
        } else {
            outputMatrix[node - osz] * hidden
        }
        f = 1.0f / (1 + exp(-f))


        dfs(k, tree[node].left, score + log(1.0f - f), heap, hidden)
        dfs(k, tree[node].right, score + log(f), heap, hidden)
    }


    private fun computeHidden(input: IntArrayList, hidden: MutableVector) {
        checkArgument(hidden.length() == hsz)
        hidden.zero()

        val buffer = input.buffer
        var i = 0
        val size = input.size()
        while (i < size) {
            val it = buffer[i]
            if (quant) {
                qinput.addToVector(hidden, it)
            } else {
                hidden += inputMatrix[it]
            }
            i++
        }
        hidden *= (1.0f / input.size())
    }

    private fun computeOutputSoftmax(hidden: Vector, output: MutableVector) {
        if (quant && args_.qout) {
            matrixMulVector(qoutput, hidden, output)
        } else {
            matrixMulVector(outputMatrix, hidden, output)
        }

        var max = output[0]
        var z = 0.0f
        for (i in 1 until osz) {
            max = Math.max(output.get(i), max)
        }
        for (i in 0 until osz) {
            output[i] = Math.exp((output[i] - max).toDouble()).toFloat()
            z += output[i]
        }
        for (i in 0 until osz) {
            output[i] = output[i] / z
        }
    }

}

/**
 * 训练模型和计算模型都需要一个setTargetCounts方法。构建negative sampling或者hierarchical softmax
 */
open class BaseModel(@JvmField val args_: Args, seed: Int, @JvmField var osz: Int) {
    // used for negative sampling:
    @JvmField
    protected var negatives: IntArray = IntArray(0)
    @JvmField
    protected var negpos: Int = 0

    // used for hierarchical softmax:
    @JvmField
    protected var paths: MutableList<IntArray> = ArrayList()
    @JvmField
    protected var codes: MutableList<BooleanArray> = ArrayList()
    @JvmField
    protected var tree: MutableList<Node> = ArrayList()


    @Transient
    @JvmField
    val rng: Random = Random(seed.toLong())

    fun setTargetCounts(counts: LongArray) {
        checkArgument(counts.size == osz)
        if (args_.loss == LossName.ns) {
            initTableNegatives(counts)
        } else if (args_.loss == LossName.hs) {
            buildTree(counts)
        }
    }

    private fun initTableNegatives(counts: LongArray) {
        val negatives_ = IntArrayList(counts.size)

        var z = counts.map { sqrt(it) }.sum()
        val size = counts.size

        val xxn = NEGATIVE_TABLE_SIZE / z
        for (i in 0 until size) {
            val c = sqrt(counts[i])
            var j = 0
            while (j < c * xxn) {
                negatives_.add(i)
                j++
            }
        }
        negatives = negatives_.toArray()
        shuffle(negatives, rng)
    }

    private fun buildTree(counts: LongArray) {
        val pathsLocal = ArrayList<IntArray>(osz)
        val codesLocal = ArrayList<BooleanArray>(osz)
        val treeLocal = ArrayList<Node>(2 * osz - 1)

        for (i in 0 until 2 * osz - 1) {
            treeLocal.add(Node().apply {
                this.parent = -1
                this.left = -1
                this.right = -1
                this.count = 1000000000000000L// 1e15f;
                this.binary = false
            })
        }

        for (i in 0 until osz) {
            treeLocal[i].count = counts[i]
        }

        var leaf = osz - 1
        var node = osz
        for (i in osz until 2 * osz - 1) {
            val mini = IntArray(2)
            for (j in 0..1) {
                if (leaf >= 0 && treeLocal[leaf].count < treeLocal[node].count) {
                    mini[j] = leaf--
                } else {
                    mini[j] = node++
                }
            }
            treeLocal[i].apply {
                this.left = mini[0]
                this.right = mini[1]
                this.count = treeLocal[mini[0]].count + treeLocal[mini[1]].count
            }
            treeLocal[mini[0]].parent = i
            treeLocal[mini[1]].parent = i
            treeLocal[mini[1]].binary = true
        }

        for (i in 0 until osz) {
            val path = ArrayList<Int>()
            val code = ArrayList<Boolean>()

            var j = i
            while (treeLocal[j].parent != -1) {
                path.add(treeLocal[j].parent - osz)
                code.add(treeLocal[j].binary)
                j = treeLocal[j].parent
            }
            pathsLocal.add(path.toIntArray())
            codesLocal.add(code.toBooleanArray())
        }

        this.paths = pathsLocal
        this.codes = codesLocal
        this.tree = treeLocal
    }

    companion object {
        private val tSigmoid: FloatArray = FloatArray(SIGMOID_TABLE_SIZE + 1, { i ->
            val x = (i * 2 * MAX_SIGMOID).toFloat() / SIGMOID_TABLE_SIZE - MAX_SIGMOID
            (1.0f / (1.0f + Math.exp((-x).toDouble()))).toFloat()
        })

        private val tLog: FloatArray = FloatArray(LOG_TABLE_SIZE + 1, { i ->
            val x = (i.toFloat() + 1e-5f) / LOG_TABLE_SIZE
            Math.log(x.toDouble()).toFloat()
        })

        fun log(x: Float): Float {
            if (x > 1.0f) {
                return 0.0f
            }
            val i = (x * LOG_TABLE_SIZE).toInt()
            return tLog[i]
        }

        fun sigmoid(x: Float): Float {
            return when {
                x < -MAX_SIGMOID -> 0.0f
                x > MAX_SIGMOID -> 1.0f
                else -> {
                    val i = ((x + MAX_SIGMOID) * SIGMOID_TABLE_SIZE / MAX_SIGMOID.toFloat() / 2f).toInt()
                    tSigmoid[i]
                }
            }
        }
    }
}

/**
 * 从C语言版本的FastText产生的模型文件
 */
object LoadFastTextFromClangModel {

    /**
     * Load binary model file. 这个二进制版本是C语言版本的模型
     * @param modelPath
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun loadCModel(modelPath: String): FastText {

        val modeFile = File(modelPath)

        if (!(modeFile.exists() && modeFile.isFile && modeFile.canRead())) {
            throw IOException("Model file cannot be opened for loading!")
        }

        modeFile.inputStream().buffered(1024 * 1024).use {
            val buffer = AutoDataInput(DataInputStream(it), ByteOrder.LITTLE_ENDIAN)

            //check model
            val magic = buffer.readInt()
            val version = buffer.readInt()

            if (magic != FASTTEXT_FILEFORMAT_MAGIC_INT32) {
                throw RuntimeException("Model file has wrong file format!")
            }

            if (version > FASTTEXT_VERSION) {
                throw RuntimeException("Model file has wrong file format! version is $version")
            }

            //Args
            val args_ = Args()
            args_.loadClang(buffer)

            if (version == 11 && args_.model == ModelName.sup) {
                // backward compatibility: old supervised models do not use char ngrams.
                args_.maxn = 0
            }

            //dictionary
            val dictionary = Dictionary(args_)
            dictionary.load(buffer)

            var input: FloatMatrix = FloatMatrix.floatArrayMatrix(0, 0)
            var qinput: QMatrix? = null

            val quantInput = buffer.readUnsignedByte() != 0
            if (quantInput) {
                qinput = QMatrix.load(buffer)
            } else {
                input = buffer.loadFloatMatrix()
            }

            if (!quantInput && dictionary.isPruned()) {
                throw RuntimeException("Invalid model file.\n"
                        + "Please download the updated model from www.fasttext.cc.\n"
                        + "See issue #332 on Github for more information.\n")
            }

            var output: FloatMatrix = FloatMatrix.floatArrayMatrix(0, 0)
            var qoutput: QMatrix? = null

            args_.qout = buffer.readUnsignedByte().toInt() != 0
            if (quantInput && args_.qout) {
                qoutput = QMatrix.load(buffer)
            } else {
                output = buffer.loadFloatMatrix()
            }

            val model = Model(input, output, args_, 0)
            model.quant = quantInput

            model.setQuantizePointer(qinput, qoutput, args_.qout)


            if (args_.model == ModelName.sup) {
                model.setTargetCounts(dictionary.getCounts(EntryType.label))
            } else {
                model.setTargetCounts(dictionary.getCounts(EntryType.word))
            }


            return if (model.quant) {
                FastText(args_, dictionary, input, output, model, true, qinput!!, qoutput)
            } else {
                FastText(args_, dictionary, input, output, model)
            }
        }
    }

}

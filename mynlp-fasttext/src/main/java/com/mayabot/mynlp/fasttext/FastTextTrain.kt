package com.mayabot.mynlp.fasttext

import com.carrotsearch.hppc.IntArrayList
import com.google.common.base.CharMatcher
import com.google.common.base.Charsets
import com.google.common.base.Splitter
import com.google.common.collect.Lists
import com.google.common.io.Files
import com.google.common.primitives.Ints
import com.google.common.util.concurrent.AtomicDouble
import fasttext.QMatrix
import java.io.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.collections.ArrayList


const val SIGMOID_TABLE_SIZE = 512
const val MAX_SIGMOID = 8
const val LOG_TABLE_SIZE = 512
const val NEGATIVE_TABLE_SIZE = 10000000

class FastTextTrain {

    lateinit var args:Args
    lateinit var dict: Dictionary

    private var tokenCount = AtomicLong(0)
    private var loss = AtomicDouble(-1.0)

    private var startTime = 0L

    var file: File? = null
    var input: MutableFloatMatrix? = null
    var output: MutableFloatMatrix? = null

    /**
     * 分类模型量化
     *
     * @param out
     */
    fun quantize(fastText: FastText,qout:Boolean=false,input:String,
                 cout:Int=0,dsub:Int=2,retrain:Boolean=false,qnorm:Boolean=false) {


        if (fastText.quant) {
            println("该模型已经被量化过")
            return
        }

        dict = fastText.dict
        args = fastText.args

        val qinput = QMatrix(fastText.input.rows(),fastText.input.cols(), dsub, qnorm)
        qinput.quantize(fastText.input as MutableFloatMatrix)

        fastText.quant = true


//        if (qargs.cutoff > 0 && qargs.cutoff < input_->size(0)) {
//            auto idx = selectEmbeddings(qargs.cutoff);
//            dict_->prune(idx);
//            std::shared_ptr<Matrix> ninput =
//            std::make_shared<Matrix>(idx.size(), args_->dim);
//            for (auto i = 0; i < idx.size(); i++) {
//            for (auto j = 0; j < args_->dim; j++) {
//            ninput->at(i, j) = input_->at(idx[i], j);
//        }
//        }
//            input_ = ninput;
//            if (qargs.retrain) {
//                args_->epoch = qargs.epoch;
//                args_->lr = qargs.lr;
//                args_->thread = qargs.thread;
//                args_->verbose = qargs.verbose;
//                startThreads();
//            }
//        }

    }

    fun train(file: File,modelName: ModelName, trainArgs: TrainArgs): FastText {
        println("Train file ${file.absolutePath}")
        args = Args().apply {
            model = modelName
            if (modelName == ModelName.sup) {
                minCount = 1
                loss = LossName.softmax
                minCount = 1
                minn = 0
                maxn = 0
                lr = 0.1
            }

            thread = trainArgs.thread ?: thread
            dim = trainArgs.dim ?: dim
            epoch = trainArgs.epoch ?: epoch
            loss = trainArgs.loss ?: loss
            lr = trainArgs.lr ?: lr
            lrUpdateRate = trainArgs.lrUpdateRate ?: lrUpdateRate
            neg = trainArgs.neg ?: neg
            ws = trainArgs.ws ?: ws

            // -wordNgrams         max length of word ngram [1]
            // -maxn               max length of char ngram [0]

            if (wordNgrams <= 1 && maxn == 0) {
                bucket = 0
            }
        }

        dict = com.mayabot.mynlp.fasttext.Dictionary(args)
        dict.buildFromFile(file)

        val output: MutableFloatMatrix = if (ModelName.sup == args.model) {//分类模型
            FloatMatrix.floatArrayMatrix(dict.nlabels(), args.dim)
        } else {
            FloatMatrix.floatArrayMatrix(dict.nwords(), args.dim)
        }

        val input: MutableFloatMatrix

        val pretrainedVectors: File? = if (!trainArgs.pretrainedVectors.isNullOrBlank()) {
            val filePre = File(trainArgs.pretrainedVectors)
            if (filePre.exists() && filePre.canRead()) {
                filePre
            } else {
                throw RuntimeException("Not found File " + trainArgs.pretrainedVectors)
            }
        } else {
            null
        }

        if (pretrainedVectors != null) {
            input = loadVectors(pretrainedVectors)
        } else {
            input = FloatMatrix.floatArrayMatrix(dict.nwords() + args.bucket, args.dim)
            input.uniform(1.0f / args.dim)
        }

        output.fill(0f)

        this.file = file
        this.input = input
        this.output = output

        startThreads()

        val fastText = FastText(args,dict, input, output, Model(input, output, args, 0).apply {
            if (args.model == ModelName.sup) {
                this.setTargetCounts(dict.getCounts(EntryType.label))
            } else {
                this.setTargetCounts(dict.getCounts(EntryType.word))
            }
        })


        println("Train use time ${System.currentTimeMillis() - startTime} ms")
        return fastText
    }

    @Throws(Exception::class)
    private fun startThreads() {
        startTime = System.currentTimeMillis()

        tokenCount = AtomicLong(0)
        loss = AtomicDouble(-1.0)

        val threads = Lists.newArrayList<Thread>()
        for (i in 0 until args.thread) {
            threads.add(Thread(TrainThread(i)))
        }

        for (i in 0 until args.thread) {
            threads[i].start()
        }

        val ntokens = dict.ntokens()
        // Same condition as trainThread
        while (tokenCount.toLong() < args.epoch * ntokens) {

            Thread.sleep(100)
            if (loss.toFloat() >= 0 && args.verbose > 1) {
                val progress = tokenCount.toFloat() / (args.epoch * ntokens)

                print("\r")
                printInfo(progress, loss)
            }
        }

        for (i in 0 until args.thread) {
            threads[i].join()
        }

        if (args.verbose > 0) {
            print("\r")
            printInfo(1.0f, loss)
            println()
        }
    }

    internal inner class TrainThread(private val threadId: Int) : Runnable {

        override fun run() {
            try {
                LoopReader((threadId * file!!.length() / args.thread).toInt(), file!!).use { loopReader ->

                    val model = TrainModel(input!!, output!!, args, threadId)
                    val rng = model.rng
                    // setTargetCounts 相当耗时

                    if (args.model == ModelName.sup) {
                        model.setTargetCounts(dict.getCounts(EntryType.label))
                    } else {
                        model.setTargetCounts(dict.getCounts(EntryType.word))
                    }

                    val ntokens = dict.ntokens() //文件中词语的总数量(非排重)
                    var localTokenCount: Long = 0
                    val up_ = args.epoch * ntokens

                    val line = IntArrayList()
                    val labels = IntArrayList()

                    if (args.model == ModelName.sup) {
                        while (tokenCount.toLong() < up_) {
                            val progress = tokenCount.toFloat() / up_ //总的进度
                            val lr = args.lr.toFloat() * (1.0f - progress) //学习率自动放缓

                            val tokens = loopReader.readLineTokens()

                            localTokenCount += dict.getLine(tokens, line, labels).toLong()
                            supervised(model, lr, line, labels)

                            if (localTokenCount > args.lrUpdateRate) {
                                tokenCount.addAndGet(localTokenCount)
                                localTokenCount = 0
                                if (threadId == 0) {
                                    loss.set(model.getLoss().toDouble())
                                }
                            }
                        }
                    }
                    if (args.model == ModelName.cbow) {
                        while (tokenCount.toLong() < up_) {
                            val progress = tokenCount.toFloat() / up_ //总的进度
                            val lr = args.lr.toFloat() * (1.0f - progress) //学习率自动放缓

                            val tokens = loopReader.readLineTokens()

                            localTokenCount += dict.getLine(tokens, line, rng).toLong()
                            cbow(model, lr, line)

                            if (localTokenCount > args.lrUpdateRate) {
                                tokenCount.addAndGet(localTokenCount)
                                localTokenCount = 0
                                if (threadId == 0) {
                                    loss.set(model.getLoss().toDouble())
                                }
                            }
                        }
                    }
                    if (args.model == ModelName.sg) {
                        while (tokenCount.toLong() < up_) {
                            val progress = tokenCount.toFloat() / up_ //总的进度
                            val lr = args.lr.toFloat() * (1.0f - progress) //学习率自动放缓

                            val tokens = loopReader.readLineTokens()

                            localTokenCount += dict.getLine(tokens, line, rng).toLong()
                            skipgram(model, lr, line)

                            if (localTokenCount > args.lrUpdateRate) {
                                tokenCount.addAndGet(localTokenCount)
                                localTokenCount = 0
                                if (threadId == 0) {
                                    loss.set(model.getLoss().toDouble())
                                }
                            }
                        }
                    }

                    if (threadId == 0) {
                        loss.set(model.getLoss().toDouble())
                    }

                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }


        internal fun supervised(
                model: TrainModel,
                lr: Float,
                line: IntArrayList,
                labels: IntArrayList) {
            if (labels.size() == 0 || line.size() == 0) {
                return
            }
            val i = if (labels.size() == 1) 0 else model.rng.nextInt(labels.size())
            model.update(line, labels.get(i), lr)
        }


        private fun cbow(model: TrainModel, lr: Float,
                         line: IntArrayList) {
            val bow = IntArrayList()

            // std::uniform_int_distribution<> uniform(1, args_->ws);
            for (w in 0 until line.size()) {
                val boundary = model.rng.nextInt(args.ws) + 1 // 1~5
                bow.clear()
                for (c in -boundary..boundary) {
                    if (c != 0 && w + c >= 0 && w + c < line.size()) {
                        val ngrams = dict.getSubwords(line.get(w + c))
                        bow.addAll(ngrams)
                    }
                }
                model.update(bow, line.get(w), lr)
            }
        }

        private fun skipgram(model: TrainModel, lr: Float,
                             line: IntArrayList) {
            for (w in 0 until line.size()) {
                val boundary = model.rng.nextInt(args.ws) + 1 // 1~5

                val ngrams = dict.getSubwords(line.get(w))
                for (c in -boundary..boundary) {
                    if (c != 0 && w + c >= 0 && w + c < line.size()) {
                        model.update(ngrams, line.get(w + c), lr)
                    }
                }
            }
        }

    }

    private fun printInfo(progress: Float, loss: AtomicDouble) {
        var progress = progress
        // clock_t might also only be 32bits wide on some systems
        val t = ((System.currentTimeMillis() - startTime) / 1000).toDouble()
        val lr = args.lr * (1.0 - progress)
        var wst = 0.0
        var eta = (720 * 3600).toLong() // Default to one month
        if (progress > 0 && t >= 0) {
            eta = (t / progress * (1 - progress) / args.thread).toInt().toLong()
            wst = tokenCount.toFloat() / t
        }
        val etah = eta / 3600
        val etam = eta % 3600 / 60
        val etas = eta % 3600 % 60
        progress *= 100
        val sb = StringBuilder()
        sb.append("Progress: " +
                String.format("%2.2f", progress) + "% words/sec/thread: " + String.format("%8.0f", wst))
        sb.append(String.format(" lr: %2.5f", lr))
        sb.append(String.format(" loss: %2.5f", loss.toFloat()))
        sb.append(" ETA: " + etah + "h " + etam + "m " + etas + "s")

        print(sb)
    }


    @Throws(Exception::class)
    private fun loadVectors(filename: File): MutableFloatMatrix {

        var n: Int = 0
        var dim: Int = 0

        val firstLine = filename.firstLine()!!
        run {
            val strings = Splitter.on(CharMatcher.whitespace()).splitToList(firstLine)
            n = Ints.tryParse(strings[0])!!
            dim = Ints.tryParse(strings[1])!!
        }
        if (n == 0 || dim == 0) {
            throw Exception("Error format for " + filename.name + ",First line must be rows and dim arg")
        }
        if (dim != args.dim) {
            throw Exception("Dimension of pretrained vectors " + dim + " does not match dimension (" + args.dim + ")")
        }

        val mat = MutableByteBufferMatrix(n, dim)

        val sp = Splitter.on(" ").omitEmptyStrings()

        val words = Lists.newArrayListWithExpectedSize<String>(n)
        val charSource = Files.asCharSource(filename, Charsets.UTF_8)
        charSource.openBufferedStream().use { reader ->
            reader.readLine()//first line
            for (i in 0 until n) {
                val line = reader.readLine()
                var parts: MutableList<String> = sp.splitToList(line)
                if (parts.size != dim + 1) {
                    if (parts.size == dim) {
                        parts = Lists.newArrayList(line.substring(0, line.indexOf(parts[0]) - 1))
                        parts.addAll(sp.splitToList(line))
                    } else {
                        throw RuntimeException("line $line parse error")
                    }

                }

                val word = parts[0]
                dict.add(word)
                words.add(word)
                val row = mat[i]
                var x = 0
                for (j in 1..dim) {
                    row[x++] = parts[j].toFloat()
                }
            }
        }

        dict.threshold(1, 0)
        val input = FloatMatrix.floatArrayMatrix(dict.nwords() + args.bucket, args.dim)
        input.uniform(1.0f / args.dim)

        for (i in 0 until n) {
            val idx = dict.getId(words[i])
            if (idx < 0 || idx > dict.nwords()) {
                continue
            }

            input[idx](mat[i])

//            System.arraycopy(matrixData, i * dim, input.getData(), idx * dim, dim)
            //            for (int j = 0; j < dim; j++) {
            //                input.set(idx, j, mat.get(i, j));
            //            }

        }

        return input

    }

}


class TrainModel(
        private val inputMatrix: MutableFloatMatrix // input
        , private val outputMatrix: MutableFloatMatrix // output
        , args_: Args, seed: Int
) : BaseModel(args_, seed, outputMatrix.rows()) {

    private val hidden = Vector.floatArrayVector(args_.dim)
    private val output = Vector.floatArrayVector(outputMatrix.rows())
    private val grad = Vector.floatArrayVector(args_.dim)

    private val hsz: Int = args_.dim // dim
    //    private val isz: Int = inputMatrix.rows()// input vocabSize
    private var loss = 0f
    private var nexamples = 1L

    fun getLoss(): Float {
        return loss / nexamples
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

    fun update(input: IntArrayList, target: Int, lr: Float) {
        checkArgument(target >= 0)
        checkArgument(target < osz)
        if (input.size() == 0) {
            return
        }
        computeHidden(input, hidden)

        loss += when (args_.loss) {
            LossName.ns -> negativeSampling(target, lr)
            LossName.hs -> hierarchicalSoftmax(target, lr)
            LossName.softmax -> softmax(target, lr)
        }
        nexamples += 1

        if (args_.model == ModelName.sup) {
            grad *= (1.0f / input.size())
        }

        val buffer = input.buffer
        var i = 0
        val size = input.size()
        while (i < size) {
            val it = buffer[i]
            inputMatrix[it] += grad
            i++
        }
    }

    private fun computeHidden(input: IntArrayList, hidden: MutableVector) {
        checkArgument(hidden.length() == hsz)
        hidden.zero()

        val buffer = input.buffer
        var i = 0
        val size = input.size()
        while (i < size) {
            val it = buffer[i]
            hidden += inputMatrix[it]
            i++
        }
        hidden *= (1.0f / input.size())
    }

    private fun negativeSampling(target: Int, lr: Float): Float {
        var loss = 0.0f
        grad.zero()
        for (n in 0..args_.neg) {
            loss += if (n == 0) {
                binaryLogistic(target, true, lr)
            } else {
                binaryLogistic(getNegative(target), false, lr)
            }
        }
        return loss
    }

    private fun binaryLogistic(target: Int, label: Boolean, lr: Float): Float {
        val score = sigmoid(outputMatrix[target] * hidden)
        val alpha = lr * ((if (label) 1.0f else 0.0f) - score)
        grad += alpha to outputMatrix[target]

        outputMatrix[target] += alpha to hidden

        return if (label) {
            -log(score)
        } else {
            -log(1.0f - score)
        }
    }

    private fun getNegative(target: Int): Int {
        var negative: Int
        do {
            negative = negatives[negpos]
            negpos = (negpos + 1) % negatives.size
        } while (target == negative)
        return negative
    }


    private fun hierarchicalSoftmax(target: Int, lr: Float): Float {
        var loss = 0.0f
        grad.zero()
        val binaryCode = codes[target]
        val pathToRoot = paths[target]
        for (i in pathToRoot.indices) {
            loss += binaryLogistic(pathToRoot[i], binaryCode[i], lr)
        }
        return loss
    }

    private fun softmax(target: Int, lr: Float): Float {
        grad.zero()
        computeOutputSoftmax()
        for (i in 0 until osz) {
            val label = if (i == target) 1.0f else 0.0f
            val alpha = lr * (label - output[i])
            grad += alpha to outputMatrix[i]
            outputMatrix[i] += alpha to hidden
        }
        return -log(output[target])
    }

    @JvmOverloads
    private fun computeOutputSoftmax(hidden: Vector = this.hidden, output: MutableVector = this.output) {
        matrixMulVector(outputMatrix, hidden, output)
        var max = output[0]
        var z = 0.0f
        for (i in 1 until osz) {
            max = Math.max(output[i], max)
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


class Node {
    @JvmField
    var parent: Int = 0
    @JvmField
    var left: Int = 0
    @JvmField
    var right: Int = 0
    @JvmField
    var count: Long = 0
    @JvmField
    var binary: Boolean = false
}


class LoopReader @Throws(IOException::class)
constructor(internal var pos: Int, private val file: File) : AutoCloseable {

    private var reader: BufferedReader? = null

    internal var splitter = Splitter.on(CharMatcher.whitespace()).omitEmptyStrings().trimResults()

    init {
        val `in` = FileInputStream(file)
        `in`.skip(pos.toLong())
        reader = BufferedReader(InputStreamReader(`in`, kotlin.text.Charsets.UTF_8))
    }

    @Throws(IOException::class)
    fun readLineTokens(): List<String> {
        var line = loopLine()
        while (line!!.isEmpty()) { //skip empty line
            line = loopLine()
        }
        return line2Tokens(line)
    }

    @Throws(IOException::class)
    private fun loopLine(): String? {
        var line: String? = reader!!.readLine()
        if (line == null) {
            reader!!.close()
            val `in` = FileInputStream(file)
            reader = BufferedReader(InputStreamReader(`in`, kotlin.text.Charsets.UTF_8))
            line = reader!!.readLine()
        }
        return line
    }

    private fun line2Tokens(line: String): List<String> {
        val list = Lists.newArrayList(splitter.split(line))
        list.add(EOS)
        return list
        //        return splitter.splitToList(line);
    }

    @Throws(Exception::class)
    override fun close() {
        if (reader != null) {
            reader!!.close()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val thread = 8
            val progress = 0.1f
            val eta = (20 / progress * (1 - progress)).toInt().toLong()
            println(eta)
        }
    }
}
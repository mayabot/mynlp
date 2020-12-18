package com.mayabot.nlp.starspace

import java.io.Writer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.min
import kotlin.system.exitProcess

/**
 * test 模型质量
 */
class Evaluate(
    private val model: StarSpace,
    testFile: String,
    val K: Int,
    val basedocFile: String?,
    /**
     * testFile判断结果的输出文件
     */
    val predictionFile: String?
) {

    private val args: Args = model.args.apply {
        // set dropout probability to 0 in test case
        this.dropoutLHS = 0.0
        this.dropoutRHS = 0.0
    }

    private var parser: DataParser = when (args.fileFormat) {
        FileFormat.FastText -> FastTextDataParser(model.dict, args)
        FileFormat.LabelDoc -> LayerDataParser(model.dict, args)
    }

    private var testData: DataHandler = when (args.fileFormat) {
        FileFormat.FastText -> InternDataHandler(parser, args)
        FileFormat.LabelDoc -> LayerDataHandler(parser, args)
    }.apply {
        loadFromFile(testFile)
    }

    private val random = Random()


    fun evaluate() {

        if (args.trainMode == TrainMode.Mode5) {
            println("Test is undefined in trainMode 5. Please use other trainMode for testing.")
            exitProcess(0)
        }


        val baseDocs = model.loadBaseDocs(basedocFile)

        val N = testData.size()

        val numThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 2)

        val metrics = Array(numThreads, { Metrics() })

        val numPerThread = Math.ceil(N * 1.0 / numThreads).toInt()
        checkArgument(numPerThread > 0)

        val examples = ArrayList<ParseResult>()
        testData.getNextKExamples(N, examples)

        val threads = ArrayList<Thread>(numThreads)

        val predictions = ArrayList<ArrayList<Prediction>>(N).apply {
            for (i in 0 until N) {
                add(ArrayList())
            }
        }


        for (idx in 0 until numThreads) {
            val start = min(idx * numPerThread, N)
            val end = min(start + numPerThread, N)
            checkArgument(end >= start)

            threads.add(thread(start = true) {
                metrics[idx].clear()
                println("$start to $end")
                for (i in start until end) {
                    if (idx == 0 && i % 50 == 0) println(i)
                    val ex = examples[i]
                    val s = evaluateOne(baseDocs, ex.lhsTokens, ex.rhsTokens, predictions[i])
                    metrics[idx] += s
                }
                print("OK ,$start to $end")
            })
        }

        for (t in threads) t.join()

        val result = Metrics()
        for (i in 0 until numThreads) {
            result += metrics[i]
        }

        result.average()
        println(result)

        predictionFile?.let {
            Files.newBufferedWriter(Paths.get(it), Charsets.UTF_8).use { writer ->

                for (i in 0 until N) {
                    writer.write("Example $i:\nLHS:\n")
                    printDoc(writer, examples[i].lhsTokens)
                    writer.write("RHS: \n")
                    printDoc(writer, examples[i].rhsTokens)

                    writer.write("Predictions: \n")

                    for (pred in predictions[i]) {
                        if (pred.second == 0) {
                            writer.write("(++) [${pred.score}]\t")
                            printDoc(writer, examples[i].rhsTokens)
                        } else {
                            writer.write("(--) [${pred.score}]\t")
                            printDoc(writer, baseDocs.baseDocs[pred.second - 1])
                        }
                    }

                }

            }
        }

    }


    private fun evaluateOne(
        baseDocs: BaseDocs,
        lhs: List<XPair>,
        rhs: List<XPair>,
        pred: MutableList<Prediction>
    ): Metrics {

        val lhsM = model.projectLHS(lhs)
        val rhsM = model.projectLHS(rhs)
        // Our evaluation function currently assumes there is only one correct label.
        // TODO: generalize this to the multilabel case.

        val score = model.similarity(lhsM, rhsM)

        var rank = 1

        val heap = Array(K, init = {
            Prediction(-1f, -1)
        })

        for (i in 0 until baseDocs.baseDocVectors.size) {

            // in the case basedoc labels are not provided, all labels become basedoc,
            // and we skip the correct label for comparison.
            if (basedocFile.isNullOrBlank() && i == rhs[0].first - model.dict.nwords()) {
                continue
            }

            val cur_score = model.similarity(lhsM, baseDocs.baseDocVectors[i])

            if (cur_score > score) {
                rank++
            } else if (cur_score == score) {
                if (random.nextFloat() > 0.5) {
                    rank++
                }
            }

            if (cur_score > heap.last().score) {
                heap.last().apply {
                    this.score = cur_score
                    this.second = i + 1
                }
                heap.sortedByDescending { it.score }
            }
        }

        heap.filter { it.second != -1 }.forEach {
            pred += it
        }

        return Metrics().apply {
            this.clear()
            this.update(rank)
        }
    }

    fun printDoc(ofs: Writer, tokens: List<XPair>) {
        for (t in tokens) {
            // skip ngram tokens
            if (t.first < model.dict.size) {
                ofs.write(model.dict.getSymbol(t.first))
                ofs.write(" ")
            }
        }
        ofs.write("\n")
    }
}

data class Metrics(
    var hit1: Float = 0f,
    var hit10: Float = 0f,
    var hit20: Float = 0f,
    var hit50: Float = 0f,
    var rank: Float = 0f,
    var count: Int = 0
) {

    fun clear() {
        hit1 = 0f
        hit10 = 0f
        hit20 = 0f
        hit50 = 0f
        rank = 0f
        count = 0
    }

    operator fun plusAssign(b: Metrics) {
        hit1 += b.hit1
        hit10 += b.hit10
        hit20 += b.hit20
        hit50 += b.hit50
        rank += b.rank
        count += b.count
    }

    fun average() {
        if (count != 0) {
            hit1 /= count
            hit10 /= count
            hit20 /= count
            hit50 /= count
            rank /= count
        }
    }

    override fun toString(): String =
        "Evaluation Metrics : \nhit@1: $hit1 hit@10: $hit10 hit@20: $hit20 hit@50: $hit50 mean ranks : $rank Total examples : $count"

    operator fun invoke(cur_rank: Int) {
        update(cur_rank)
    }

    fun update(cur_rank: Int) {
        if (cur_rank == 1) {
            hit1++
        }
        if (cur_rank <= 10) {
            hit10++
        }
        if (cur_rank <= 20) {
            hit20++
        }
        if (cur_rank <= 50) {
            hit50++
        }
        rank += cur_rank
        count++
    }

}
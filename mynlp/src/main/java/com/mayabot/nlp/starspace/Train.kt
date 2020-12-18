package com.mayabot.nlp.starspace


import com.mayabot.nlp.blas.ByteBufferMatrix
import com.mayabot.nlp.blas.DenseMatrix
import com.mayabot.nlp.blas.DenseVector
import com.mayabot.nlp.blas.Vector
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

class TrainModel(dict: Dictionary, args_: Args) {

    val lhsEmbeddings: SparseLinear
    val lhsMatrix: DenseMatrix

    val rhsEmbeddings: SparseLinear
    val rhsMatrix: DenseMatrix

    val lhsUpdates: FloatArray

    val rhsUpdates: FloatArray

    val args = args_


    init {
        var numLhs = dict.nwords() + dict.nlabels()

        if (args.ngrams > 1) {
            numLhs += args.bucket
        }

        lhsMatrix = ByteBufferMatrix(numLhs, args.dim, direct = true).apply {
            if (args.initRandSd.toFloat() > 0) {
                gaussRandom(args.initRandSd)
            }
        }

        lhsEmbeddings = SparseLinear(lhsMatrix)

        rhsEmbeddings = if (args.shareEmb) {
            lhsEmbeddings
        } else {
            SparseLinear(
                ByteBufferMatrix(numLhs, args.dim, direct = true).apply {
                    if (args.initRandSd.toFloat() > 0) {
                        gaussRandom(args.initRandSd)
                    }
                })
        }

        rhsMatrix = rhsEmbeddings.matrix

        println(
            "Initialized model weights. StarSpace Model length :\n" +
                    "matrix : " + lhsEmbeddings.numRows() + ' '.toString() + lhsEmbeddings.numCols()
        )
    }

    init {
        if (args.adagrad) {
            lhsUpdates = FloatArray(lhsEmbeddings.numRows())
            rhsUpdates = FloatArray(rhsEmbeddings.numRows())
        } else {
            lhsUpdates = FloatArray(0)
            rhsUpdates = FloatArray(0)
        }
    }

    fun projectLHS(ws: List<XPair>): Vector {
        val vec = lhsEmbeddings.forward(ws)

        if (ws.isNotEmpty()) {
            val norm = args.norm2Computer(ws, vec)
            vec /= norm
        }

        return vec
    }

    fun projectRHS(ws: List<XPair>): Vector {
        val vec = rhsEmbeddings.forward(ws)
        if (ws.isNotEmpty()) {
            val norm = args.norm2Computer(ws, vec)
            vec /= norm
        }
        return vec
    }
}


class ModelTrainer(val args: Args, trainFile: String, validationFile: String? = null) {

    private var dict: Dictionary = Dictionary(args)

    private var parser: DataParser = when (args.fileFormat) {
        FileFormat.FastText -> FastTextDataParser(dict, args)
        FileFormat.LabelDoc -> LayerDataParser(dict, args)
    }

    private var trainData: DataHandler = when (args.fileFormat) {
        FileFormat.FastText -> InternDataHandler(parser, args)
        FileFormat.LabelDoc -> LayerDataHandler(parser, args)
    }

    private var trainMethod: (
        data: DataHandler,
        items: List<XPair>,
        labels: List<XPair>,
        negSearchLimit: Int,
        rate0: Float,
        trainWord: Boolean
    ) -> Float = when (args.loss) {
        LossFunction.SoftMax -> ::trainNLL
        LossFunction.Hinge -> ::trainOne
    }

    private val isDebug = args.debug

    init {
        println("Start to initialize starspace model.\n")
        // build dict
        dict.readFromFile(trainFile)

        // init train data class
        trainData.loadFromFile(trainFile)
    }

    // init model with trainArgs and dict
    private val startSpace = TrainModel(dict, args)

    private val lhsEmbeddings = startSpace.lhsEmbeddings
    private val lhsEmbeddingMatrix = startSpace.lhsMatrix
    private val rhsEmbeddings = startSpace.rhsEmbeddings
    private val rhsEmbeddingMatrix = startSpace.rhsMatrix

    private val lhsUpdates: FloatArray = startSpace.lhsUpdates
    private val rhsUpdates: FloatArray = startSpace.rhsUpdates


    @Throws(Exception::class)
    @JvmOverloads
    fun train(): StarSpace {

        //        if (!Strings.isNullOrEmpty(args.validationFile)) {
        //            validData = initData();
        //            validData.loadFromFile(args.validationFile, parser);
        //        }

        var rate = args.lr.toFloat()

        val decrPerEpoch = (rate - 1e-9) / args.epoch

        val t_start = System.currentTimeMillis()

        for (i in 0 until args.epoch) {
            //            if (args.saveEveryEpoch && i > 0) {
            //                String filename = args.tempModel;
            //                if (args.saveTempModel) {
            //                    filename = filename + "_epoch" + i;
            //                }
            ////                 saveModel(filename);
            ////                 saveModelTsv(filename+".tsv");
            //            }

            println(
                "Training epoch " + i + ": " + String.format("%.3f", rate) + ' '.toString() + String.format(
                    "%.3f",
                    decrPerEpoch
                )
            )

            val err = train(trainData, args.thread, t_start, i, rate, rate - decrPerEpoch)


            println(
                String.format(
                    "\n ---+++ %20s %4d \tTrain error : %3.8f +++--- ☃\n",
                    "Epoch", i, err
                )
            )

            //TODO need
            //            if (validData != null) {
            //                boolean valid_err = model.test(validData, args.thread);
            //                System.out.println("Validation error: " + valid_err);
            //            }

            rate -= decrPerEpoch.toFloat()

            val totSpent = System.currentTimeMillis() - t_start

            if (totSpent > args.maxTrainTime * 1000) {
                println("MaxTrainTime exceeded.")
                break
            }

        }

        return StarSpace(dict, args, lhsEmbeddingMatrix, rhsEmbeddingMatrix)
    }


    /**
     * 轮次的迭代
     */
    @Throws(InterruptedException::class)
    fun train(
        trainData: DataHandler,
        numThreads: Int,
        t_start: Long,
        epochs_done: Int,
        rate: Float,
        finishRate: Double
    ): Float {
        var numThreads = numThreads

        checkArgument(rate >= finishRate)
        checkArgument(rate >= 0.0f)

        var rate = rate.toDouble()
        val rateLocker = ReentrantLock()


        // Use a layer of indirection when accessing the corpus to allow shuffling.
        val numSamples = trainData.size()
        val indices = ArrayList<Int>()
        for (i in 0 until numSamples) {
            indices.add(i)
        }
        indices.shuffle()

        // If we decrement after *every* sample, precision causes us to lose the
        // update.

        val kDecrStep = 1000
        val decrPerKSample = (rate.toFloat() - finishRate) / (numSamples * 1.0 / kDecrStep)
        val negSearchLimit = Math.min(numSamples, args.negSearchLimit)

        numThreads = Math.max(numThreads, 2) - 1
        numThreads = Math.min(numThreads, numSamples)

        val losses = FloatArray(numThreads)
        val counts = LongArray(numThreads)

        val threads = ArrayList<Thread>()
        val doneTraining = AtomicBoolean(false)

        val numPerThread = Math.ceil((numSamples * 1.0f / numThreads).toDouble()).toInt()
        checkArgument(numPerThread > 0)

        val partition = indices.chunked(numPerThread)

        for (t in 0 until numThreads) {

            val threadIndex = partition[t]
            val thread = thread(start = true) {
                val amMaster = t == 0
                val elapsed: Long
                val t_epoch_start = System.currentTimeMillis()
                losses[t] = 0.0f
                counts[t] = 0

                val lastP = threadIndex.size - 1
                for (ip in threadIndex.indices) {
                    val i = threadIndex[ip]

                    var thisLoss: Float

                    if (args.trainMode === TrainMode.Mode5 || args.trainWord) {
                        val exs = ArrayList<ParseResult>()
                        trainData.getWordExamples(i, exs)
                        for (ex in exs) {
                            thisLoss = trainOneExample(trainData, ex, negSearchLimit, rate.toFloat(), true)
                            checkArgument(thisLoss >= 0.0)
                            counts[t]++
                            losses[t] += thisLoss
                        }
                    }

                    if (args.trainMode !== TrainMode.Mode5) {
                        val ex = ParseResult()
                        trainData.getExampleById(i, ex)
                        thisLoss = trainOneExample(trainData, ex, negSearchLimit, rate.toFloat(), false)
                        checkArgument(thisLoss >= 0.0)
                        counts[t]++
                        losses[t] += thisLoss
                    }

                    // update rate racily.
                    if (i % kDecrStep == kDecrStep - 1) {
                        //rate -= decrPerKSample;
                        rateLocker.withLock {
                            rate -= decrPerKSample
                        }
                    }

                    if (amMaster && (ip % 100 == 99 || ip == lastP)) {
                        val t_end = System.currentTimeMillis()
                        val t_epoch_spent = t_end - t_epoch_start
                        val ex_done_this_epoch = ip.toLong()

                        val ex_left = threadIndex.size * (args.epoch - epochs_done) - ex_done_this_epoch

                        val ex_done = (epochs_done * threadIndex.size + ex_done_this_epoch).toDouble()
                        val time_per_ex = (t_epoch_spent * 1f / ex_done_this_epoch).toDouble()
                        var eta = (time_per_ex * ex_left).toLong()

                        val tot_spent = t_end - t_start

                        if (tot_spent > args.maxTrainTime * 1000) {
                            break
                        }

                        val epoch_progress = (ex_done_this_epoch * 1.0f / threadIndex.size).toDouble()
                        var progress = ex_done / (ex_done + ex_left)
                        if (eta > args.maxTrainTime * 1000 - tot_spent) {
                            eta = args.maxTrainTime * 1000 - tot_spent
                            progress = (tot_spent / (eta + tot_spent)).toDouble()
                        }
                        //单位变成秒
                        eta /= 1000

                        val etah = eta / 3600
                        val etam = (eta - etah * 3600) / 60
                        val etas = eta - etah * 3600 - etam * 60
                        val toth = tot_spent / 3600
                        val totm = (tot_spent - toth * 3600) / 60
                        val tots = tot_spent - toth * 3600 - totm * 60

                        //Epoch: 100.0%  lr: 0.008117  loss: 0.007617  eta: <1min   tot: 0h0m1s  (20.0%)

                        val log = StringBuilder()
                        log.append(String.format("\rEpoch: %.1f", 100 * epoch_progress))
                        log.append(String.format("\tlr: %.6f", rate.toFloat()))
                        log.append(String.format("\tloss: %.6f", losses[t] / counts[t]))

                        if (eta < 60) {
                            log.append("\teta: <1min ")
                        } else {
                            log.append("\teta: " + etah + "h" + etam + "m")
                        }
                        log.append("\ttot: " + toth + "h" + totm + "m" + tots + "s ")
                        log.append(" (" + String.format("%2.1f", 100 * progress) + "%)")
                        print("\r")
                        print(log.toString())

                    }


                }
            }

            threads.add(thread)
        }

        // .. and a norm truncation thread. It's not worth it to slow
        // down every update with truncation, so just work our way through
        // truncating as needed on a separate thread.

        val truncator = thread(start = true) {
            var i = 0
            while (!doneTraining.get()) {
                val wIdx = i % lhsEmbeddings.numRows()
                trunc(lhsEmbeddingMatrix[wIdx], args.norm)
//                trunc(lhsEmbeddings[wIdx], args.norm)
                i++
            }
        }

        for (thread in threads) {
            thread.join()
        }

        doneTraining.set(true)

        truncator.join()

        var totLoss = 0f
        for (loss in losses) {
            totLoss += loss
        }

        var totCount: Long = 0
        for (count in counts) {
            totCount += count
        }
        return totLoss / totCount * 1.0f
    }

    private fun trainOneExample(
        data: DataHandler,
        example: ParseResult,
        negSearchLimit: Int,
        rate: Float,
        trainWord: Boolean
    ): Float {
        if (example.rhsTokens.size == 0 || example.lhsTokens.size == 0) {
            return 0f
        }

        if (isDebug) {
            print("vec: ")
            for ((first, second) in example.lhsTokens) {
                print(first.toString() + ":" + second + " ")
            }
            println()
            print("vec: ")
            for ((first, second) in example.rhsTokens) {
                print(first.toString() + ":" + second + " ")
            }
            println()
        }

        val wRate = example.weight * rate

        return trainMethod(data, example.lhsTokens, example.rhsTokens, negSearchLimit, wRate, trainWord)
    }

    /**
     * hinge
     *
     * @return
     */
    private fun trainOne(
        data: DataHandler,
        items: List<XPair>, labels: List<XPair>,
        negSearchLimit: Int, rate0: Float, trainWord: Boolean
    ): Float {
        if (items.size == 0) {
            return 0.0f
        }

        val lhs = startSpace.projectLHS(items)
        check(lhs)
        val cols = lhs.length()

        val rhsP = startSpace.projectRHS(labels)
        check(rhsP)

        val posSim = args.similarity(lhs, rhsP)

        //Real negSim = std::numeric_limits<Real>::min();
        val negSim = java.lang.Float.MIN_VALUE


        // Some simple helpers to characterize the current triple we're
        // considering.

        val tripleLoss = { posSim_: Double, negSim_: Double ->
            val `val` = args.margin - posSim_ + negSim_

            checkArgument(!posSim_.isNaN() && !posSim_.isInfinite())
            checkArgument(!negSim_.isNaN() && !negSim_.isInfinite())

            // We want the max representable loss to have some wiggle room to
            // compute with.
            val kMaxLoss = 10e7

            Math.max(Math.min(`val`, kMaxLoss), 0.0).toFloat()
        }


        //Select negative example
        var loss = 0.0f
        val negs = ArrayList<Vector>()
        val negLabelsBatch = ArrayList<List<XPair>>()
        val negMean = DenseVector(cols)

        var i = 0
        while (i < negSearchLimit && negs.size < args.maxNegSamples) {

            val negLabels = ArrayList<XPair>()
            do {
                data.getRandomRHS(negLabels, trainWord)
            } while (deepEquals(negLabels, labels))

            val rhsN = startSpace.projectRHS(negLabels)
            check(rhsN)

            val thisLoss = tripleLoss(posSim.toDouble(), args.similarity(lhs, rhsN).toDouble())
            if (thisLoss > 0.0) {
                loss += thisLoss
                negs.add(rhsN)
                negLabelsBatch.add(negLabels)
                negMean += rhsN
                assert(loss >= 0.0)
            }
            i++
        }
        loss /= negSearchLimit.toFloat()
        //negMean.matrix /= negs.length();
        negMean /= negs.size.toFloat()

        // Couldn't find a negative example given reasonable effort, so
        // give up.
        if (negs.size == 0) return 0.0f
        checkArgument(!java.lang.Float.isInfinite(loss))

        if (rate0 == 0f) {
            return loss
        }


        // Let w be the average of the input features, t+ be the positive
        // example and t- be the average of the negative examples.
        // Our error E is:
        //
        //    E = k - dot(w, t+) + dot(w, t-)
        //
        // Differentiating term-by-term we get:
        //
        //     dE / dw  = t- - t+
        //     dE / dt- = w
        //     dE / dt+ = -w
        //
        // This is the innermost loop, so cache misses count. Please do some perf
        // testing if you end up modifying it.

        // gradW = \sum_i t_i- - t+. We're done with negMean, so reuse it.
        negMean += -1.0f to rhsP

        val nRate = rate0 / negs.size

        val negRate = FloatArray(negs.size)

        Arrays.fill(negRate, nRate)

        backward(
            items, labels, negLabelsBatch,
            negMean, lhs,
            rate0, -rate0, negRate
        )

        return loss
    }

    /**
     * softmax
     *
     * @return
     */
    private fun trainNLL(
        data: DataHandler,
        items: List<XPair>, labels: List<XPair>,
        negSearchLimit: Int, rate0: Float, trainWord: Boolean
    ): Float {
        if (items.size == 0) {
            return 0f
        }

        val lhs = startSpace.projectLHS(items)
        check(lhs)

        val rhsP = startSpace.projectRHS(labels)
        check(rhsP)

        // Label is treated as class 0
        val numClass = args.negSearchLimit + 1
        val prob = FloatArray(numClass)
        val negClassVec = ArrayList<Vector>()
        val negLabelsBatch = ArrayList<List<XPair>>()

        prob[0] = lhs * rhsP

        var max = prob[0]

        for (i in 1 until numClass) {
            val negLabels = ArrayList<XPair>()
            do {
                data.getRandomRHS(negLabels, trainWord)
            } while (deepEquals(negLabels, labels))

            val rhsN = startSpace.projectRHS(negLabels)
            check(rhsN)
            negClassVec.add(rhsN)
            negLabelsBatch.add(negLabels)

            prob[i] = lhs * rhsN
            max = Math.max(prob[i], max)
        }

        var base = 0f
        for (i in 0 until numClass) {
            prob[i] = Math.exp((prob[i] - max).toDouble()).toFloat()
            base += prob[i]
        }

        // normalize the probabilities
        for (i in 0 until numClass) {
            prob[i] = prob[i] / base
        }

        val loss = (-Math.log(prob[0].toDouble())).toFloat()

        // Let w be the average of the words in the post, t+ be the
        // positive example (the tag the post has) and t- be the average
        // of the negative examples (the tags we searched for with submarginal
        // separation above).
        // Our error E is:
        //
        //    E = - log P(t+)
        //
        // Where P(t) = exp(dot(w, t)) / (\sum_{t'} exp(dot(w, t')))
        //
        // Differentiating term-by-term we get:
        //
        //    dE / dw = t+ (P(t+) - 1)
        //    dE / dt+ = w (P(t+) - 1)
        //    dE / dt- = w P(t-)

        rhsP *= (prob[0] - 1)
        for (i in 0 until numClass - 1) {
//            rhsP.add(negClassVec[i], prob[i + 1])
            rhsP += prob[i + 1] to negClassVec[i]
        }

        val negRate = FloatArray(numClass - 1)
        for (i in negRate.indices) {
            negRate[i] = prob[i + 1] * rate0
        }
        backward(items, labels, negLabelsBatch, rhsP, lhs, rate0, (prob[0] - 1) * rate0, negRate)


        return loss

    }

    private fun update(
        cols: Int,
        dest: Vector,
        src: Vector,
        rate: Float,
        weight: Float,
        adagradWeight: FloatArray,
        idx: Int
    ) {
        if (args.adagrad) {
            checkArgument(idx < adagradWeight.size)

            adagradWeight[idx] += weight / cols
            val rate1 = rate / Math.sqrt(adagradWeight[idx] + 1e-6).toFloat()

            dest -= rate1 to src
        } else {
            dest -= rate to src
        }
    }

    private fun backward(
        items: List<XPair>,
        labels: List<XPair>,
        negLabels: List<List<XPair>>,
        gradW: Vector,
        lhs: Vector,
        rate_lhs: Float,
        rate_rhsP: Float,
        rate_rhsN: FloatArray
    ) {

        val cols = lhs.length()

        var n1 = 0f
        var n2 = 0f

        if (args.adagrad) {
            n1 = gradW * gradW
            n2 = lhs * lhs
        }

        // Update input items.
        for (w in items) {
            val row = lhsEmbeddingMatrix[index(w)]
            update(
                cols, row, gradW, rate_lhs * weight(w), n1,
                lhsUpdates, index(w)
            )
        }

        // Update positive example.
        for (la in labels) {
            val row = rhsEmbeddingMatrix[index(la)]
            update(cols, row, lhs, rate_rhsP * weight(la), n2, rhsUpdates, index(la))
        }

        // Update negative example.
        for (i in negLabels.indices) {
            for (la in negLabels[i]) {
                val row = rhsEmbeddingMatrix[index(la)]
                update(cols, row, lhs, rate_rhsN[i] * weight(la), n2, rhsUpdates, index(la))
            }
        }
    }


    private fun deepEquals(a: List<XPair>?, b: List<XPair>?): Boolean {
        if (a != null && b != null && a.size == b.size) {
            for (i in a.indices) {
                val eq = a[i] == b[i]
                if (!eq) {
                    return false
                }
            }
            return true
        }
        return false
    }

    private fun check(vector: Vector) {
        vector.check()
    }

    private fun trunc(row: Vector, maxNorm: Double) {
        val norm = args.norm2(row)
        if (norm > maxNorm) {
            row *= ((maxNorm / norm).toFloat())
        }
    }

    private fun index(idxWeightPair: XPair): Int {
        return idxWeightPair.first
    }

    private fun weight(idxWeightPair: XPair): Float {
        return idxWeightPair.second
    }

    private fun weight(idx: Int): Float {
        return 1.0f
    }

    private fun checkArgument(expression: Boolean) {
        if (!expression) {
            throw IllegalArgumentException()
        }
    }

}
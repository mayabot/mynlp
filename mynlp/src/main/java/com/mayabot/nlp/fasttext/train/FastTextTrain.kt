package com.mayabot.nlp.fasttext.train

import com.mayabot.nlp.fasttext.FastText
import com.mayabot.nlp.fasttext.Model
import com.mayabot.nlp.fasttext.args.Args
import com.mayabot.nlp.fasttext.args.ModelName
import com.mayabot.nlp.fasttext.loss.LossName
import com.mayabot.nlp.fasttext.utils.IntArrayList
import com.mayabot.nlp.fasttext.utils.logger
import com.mayabot.nlp.fasttext.utils.loggerln
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicLong



class FastTextTrain(
        val trainArgs: Args,
        val fastText: FastText
) {

    private val tokenCount = AtomicLong(0)
    private val loss = ShareDouble(-1.0)
    private var startTime = System.currentTimeMillis()

    var trainException: Exception? = null

    val dict = fastText.dict

    val args = trainArgs

    val ntokens = dict.ntokens

//    private val thread = trainArgs.thread

    private val wantProcessTotalTokens = args.epoch * ntokens

    private fun keepTraining() =
            tokenCount.toLong() < wantProcessTotalTokens && trainException == null

    private fun progress() = tokenCount.toFloat() / wantProcessTotalTokens

    fun startThreads(sources: List<Iterable<SampleLine>>) {
        val thread = sources.size
        val threads = ArrayList<Thread>()
        for (i in 0 until thread) {
            threads.add(Thread(TrainThread(i, sources[i])))
        }

        for (i in 0 until thread) {
            threads[i].start()
        }

        val ntokens = dict.ntokens

        //printInfo(0f, loss)
        // Same condition as trainThread
        while (keepTraining()) {
            sleep(100)
            if (loss.toFloat() >= 0) {
                val progress = progress()
                logger("\r")
                printInfo(progress, loss, false)
            }
        }

        for (i in 0 until thread) {
            threads[i].join()
        }

        trainException?.let {
            throw it
        }

        logger("\r")
        printInfo(1.0f, loss, true)
        loggerln()

        loggerln("Train use time ${System.currentTimeMillis() - startTime} ms")

    }

    internal inner class TrainThread(
            private val threadId: Int,
            private val parts: Iterable<SampleLine>

    ) : Runnable {

        val state = Model.State(args.dim, fastText.output.row, trainArgs.seed)

        val ntokens = dict.ntokens

        var localTokenCount = 0

        override fun run() {
            var emptyCount = 0
            val reader = LoopReader(parts)
            try {
                val line = IntArrayList()
                val labels = IntArrayList()

                while (keepTraining()) {
                    val progress = progress()

                    val lr = (trainArgs.lr * (1.0 - progress)).toFloat()

                    if (reader.hasNext()) {
                        val sample = reader.next()
                        if (sample.words.isEmpty()) {
                            emptyCount++
                            if (emptyCount > 1000) {
                                break
                            }
                            continue
                        } else {
                            emptyCount = 0
                            val tokens = sample.words
                            when (args.model) {
                                ModelName.sup -> {
                                    localTokenCount += dict.getLine(tokens, line, labels)
                                    supervised(state, fastText.model, lr, line, labels)
                                }
                                ModelName.cbow -> {
                                    localTokenCount += dict.getLine(tokens, line, state.rng)
                                    cbow(state, fastText.model, lr, line)
                                }
                                ModelName.sg -> {
                                    localTokenCount += dict.getLine(tokens, line, state.rng)
                                    skipgram(state, fastText.model, lr, line)
                                }
                            }

                            if (localTokenCount > args.lrUpdateRate) {
                                tokenCount.addAndGet(localTokenCount.toLong())
                                localTokenCount = 0
                                if (threadId == 0) {
                                    loss.set(state.loss.toDouble())
                                }
                            }
                        }

                    } else {
                        error("不可能为空")
                    }
                }

            } catch (e: Exception) {
                trainException = e
            }
        }


        private fun supervised(
                state: Model.State,
                model: Model,
                lr: Float,
                line: IntArrayList,
                labels: IntArrayList) {
            if (labels.size() == 0 || line.size() == 0) {
                return
            }
            if (args.loss == LossName.ova) {
                model.update(line, labels, Model.kAllLabelsAsTarget, lr, state)
            } else {
                val i = state.rng.nextInt(labels.size())
                model.update(line, labels, i, lr, state)
            }
        }


        private fun cbow(state: Model.State, model: Model, lr: Float,
                         line: IntArrayList) {
            val bow = IntArrayList()

            // std::uniform_int_distribution<> uniform(1, args_->ws);
            for (w in 0 until line.size()) {
                val boundary = state.rng.nextInt(args.ws) + 1 // 1~5
                bow.clear()
                for (c in -boundary..boundary) {
                    if (c != 0 && w + c >= 0 && w + c < line.size()) {
                        val ngrams = dict.getSubwords(line.get(w + c))
                        bow.addAll(ngrams)
                    }
                }
                model.update(bow, line, w, lr, state)
            }
        }

        private fun skipgram(state: Model.State, model: Model, lr: Float,
                             line: IntArrayList) {
            for (w in 0 until line.size()) {
                val boundary = state.rng.nextInt(args.ws) + 1 // 1~5

                val ngrams = dict.getSubwords(line.get(w))
                for (c in -boundary..boundary) {
                    if (c != 0 && w + c >= 0 && w + c < line.size()) {
                        model.update(ngrams, line, w + c, lr, state)
                    }
                }
            }
        }

    }

    /**
     *
     */
    private fun printInfo(progress: Float, loss: ShareDouble, stop: Boolean) {
        var progress = progress
        // clock_t might also only be 32bits wide on some systems
        val t = ((System.currentTimeMillis() - startTime) / 1000).toDouble()
        val lr = trainArgs.lr * (1.0 - progress)
        var wst = 0.0
        // 按照 0.2 版本修复ETA问题
        var eta = (720 * 3600).toLong() // Default to one month
        if (progress > 0 && t >= 0) {
            progress *= 100
            eta = (t * (100.0f - progress) / progress).toLong()
            wst = tokenCount.toDouble() / t / trainArgs.thread.toDouble()
        }

        val etah = eta / 3600
        val etam = eta % 3600 / 60
        val etas = eta % 3600 % 60

        val sb = StringBuilder()
        sb.append("Progress: " +
                String.format("%2.2f", progress) + "% words/sec/thread: " + String.format("%8.0f", wst))
        if (!stop) sb.append(String.format(" lr: %2.5f", lr))
        sb.append(String.format(" arg.loss: %2.5f", loss.toFloat()))
        if (!stop) sb.append(" ETA: " + etah + "h " + etam + "m " + etas + "s")

        logger(sb)
    }

    class ShareDouble(var value: Double) {
        fun toFloat() = value.toFloat()
        fun set(v: Double) {
            value = v
        }
    }
}

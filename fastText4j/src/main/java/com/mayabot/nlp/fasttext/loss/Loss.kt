package com.mayabot.nlp.fasttext.loss

import com.mayabot.nlp.fasttext.Model
import com.mayabot.nlp.fasttext.Predictions
import com.mayabot.nlp.fasttext.ScoreIdPair
import com.mayabot.nlp.fasttext.args.Args
import com.mayabot.nlp.fasttext.args.ModelName
import com.mayabot.nlp.fasttext.blas.DenseVector
import com.mayabot.nlp.fasttext.blas.Matrix
import com.mayabot.nlp.fasttext.dictionary.Dictionary
import com.mayabot.nlp.fasttext.dictionary.EntryType
import com.mayabot.nlp.fasttext.utils.IntArrayList
import kotlin.math.exp
import kotlin.math.ln

const val SIGMOID_TABLE_SIZE = 512
const val MAX_SIGMOID = 8
const val LOG_TABLE_SIZE = 512

fun stdLog(d: Float) = ln(d + 1e-5)

/**
 * 统一创建loss函数实例
 */
fun createLoss(args: Args, output: Matrix, modelName: ModelName, dictionary: Dictionary): Loss {

    fun getTargetCounts(): LongArray {
        return if (modelName == ModelName.sup) {
            dictionary.getCounts(EntryType.label)
        } else {
            dictionary.getCounts(EntryType.word)
        }
    }

    return when (args.loss) {
        LossName.hs -> HierarchicalSoftmaxLoss(output, getTargetCounts())
        LossName.ns -> NegativeSamplingLoss(output, args.neg, getTargetCounts())
        LossName.softmax -> SoftmaxLoss(output)
        LossName.ova -> OneVsAlLoss(output)
        else -> error("unknow loss")
    }
}

abstract class Loss(val wo: Matrix) {

    open fun predict(k: Int, threshold: Float, heap: Predictions, state: Model.State) {
        computeOutput(state)
        findKBest(k, threshold, heap, state.output)
        heap.sortByDescending { it.score }
    }

    private fun findKBest(k: Int, threshold: Float, heap: MutableList<ScoreIdPair>, output: DenseVector) {
        for (i in 0 until output.length()) {
            if (output[i] < threshold) {
                continue
            }

            if (heap.size == k && stdLog(output[i]) < heap.first().score) {
                continue
            }

            heap += ScoreIdPair(stdLog(output[i]).toFloat(), i)
            heap.sortByDescending { it.score } // 从高到低排序
            if (heap.size > k) {
                heap.sortByDescending { it.score }
                heap.removeAt(heap.size - 1)
            }
        }
    }

    abstract fun computeOutput(state: Model.State)

    abstract fun forward(targets: IntArrayList, targetIndex: Int, state: Model.State, lr: Float, backprop: Boolean): Float

    companion object {
        private val tSigmoid: FloatArray = FloatArray(SIGMOID_TABLE_SIZE + 1) { i ->
            val x = (i * 2 * MAX_SIGMOID).toFloat() / SIGMOID_TABLE_SIZE - MAX_SIGMOID
            (1.0f / (1.0f + exp((-x).toDouble()))).toFloat()
        }

        private val tLog: FloatArray = FloatArray(LOG_TABLE_SIZE + 1) { i ->
            val x = (i.toFloat() + 1e-5f) / LOG_TABLE_SIZE
            ln(x.toDouble()).toFloat()
        }

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

abstract class BinaryLogisticLoss(wo: Matrix) : Loss(wo) {

    fun binaryLogistic(
            target: Int,
            state: Model.State,
            labelIsPositive: Boolean,
            lr: Float,
            backprop: Boolean): Float {

        val score = sigmoid(wo.dotRow(state.hidden, target))
        if (backprop) {
            val alpha = lr * ((if (labelIsPositive) 1.0f else 0.0f) - score)
//            state.grad += (alpha to wo[target])
            state.grad.addRow(wo, target, alpha.toDouble())
            wo.addVectorToRow(state.hidden, target, alpha)
        }

        if (labelIsPositive) {
            return -log(score)
        } else {
            return -log(1.0f - score)
        }
    }

    override fun computeOutput(state: Model.State) {
        val output = state.output

        output.mul(wo, state.hidden)

        val osz = output.length()

        for (i in 0 until osz) {
            output[i] = sigmoid(output[i])
        }
    }
}

/**
 * 词向量默认的loss func 是 ns
 * 文本分类的loss func默认是 softmax
 */
enum class LossName constructor(var value: Int) {

    /**
     * 分层softmax,比完全softmax慢一点。
     * 分层softmax是完全softmax损失的近似值，它允许有效地训练大量类。
     * 还请注意，这种损失函数被认为是针对不平衡的label class，即某些label比其他label更多出现在样本。
     * 如果您的数据集每个label的示例数量均衡，则值得尝试使用负采样损失（-loss ns -neg 100）。
     *
     */
    hs(1),
    /**
     * NegativeSamplingLoss 负采样
     */
    ns(2),
    /**
     * softmax ，分类模型默认
     */
    softmax(3),

    /**
     * 可用于多分类模型
     */
    ova(4);

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

package com.mayabot.nlp.fasttext.loss

import com.mayabot.nlp.fasttext.Model
import com.mayabot.nlp.fasttext.blas.Matrix
import com.mayabot.nlp.fasttext.utils.IntArrayList
import kotlin.random.Random


class NegativeSamplingLoss(wo: Matrix, val neg: Int, targetCounts: LongArray) : BinaryLogisticLoss(wo) {
    companion object {
        const val NEGATIVE_TABLE_SIZE = 10000000
    }

    val negatives = IntArrayList()


    val uniform: (random: Random) -> Int

    init {
        var z = 0.0
        for (i in 0 until targetCounts.size) {
            z += Math.pow(targetCounts[i].toDouble(), 0.5)
        }

        for (i in 0 until targetCounts.size) {
            val c = Math.pow(targetCounts[i].toDouble(), 0.5)
            for (j in 0 until (c * NEGATIVE_TABLE_SIZE / z).toInt()) {
                negatives.add(i)
            }
        }
        val ns = negatives.size()
        //uniform_ = std::uniform_int_distribution<size_t>(0, negatives_.size());
        uniform = { random -> random.nextInt(ns) }
    }

    override fun forward(targets: IntArrayList, targetIndex: Int, state: Model.State, lr: Float, backprop: Boolean): Float {
        val target = targets[targetIndex]
        var loss = binaryLogistic(target, state, true, lr, backprop)
        for (n in 0 until neg) {
            var negativeTarget = getNegative(target, state.rng)
            loss += binaryLogistic(negativeTarget, state, false, lr, backprop)
        }
        return loss
    }

    private fun getNegative(target: Int, rng: Random): Int {
        var negative = -1
        do {
            negative = negatives[uniform(rng)]
        } while (target == negative)
        return negative
    }

}

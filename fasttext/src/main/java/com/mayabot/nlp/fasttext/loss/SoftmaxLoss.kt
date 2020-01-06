package com.mayabot.nlp.fasttext.loss

import com.carrotsearch.hppc.IntArrayList
import com.mayabot.nlp.fasttext.Model
import com.mayabot.nlp.fasttext.blas.Matrix
import java.lang.Math.max

class SoftmaxLoss(wo: Matrix) : Loss(wo) {
    override fun computeOutput(state: Model.State) {
        val output = state.output

//        matrixMulVector(wo, state.hidden, output)
        output.mul(wo, state.hidden)

        var max = output[0]
        var z = 0.0f

        val osz = output.length()

        for (i in 0 until osz) {
            max = max(output[i], max)
        }

        for (i in 0 until osz) {
            output[i] = kotlin.math.exp((output[i] - max).toDouble()).toFloat()
            z += output[i]
        }
        // 归一化?
        for (i in 0 until osz) {
            output[i] = output[i] / z
        }
    }

    override fun forward(targets: IntArrayList, targetIndex: Int, state: Model.State, lr: Float, backprop: Boolean): Float {

        computeOutput(state)

        val target = targets[targetIndex]
        if (backprop) {
            val osz = wo.row
            for (i in 0 until osz) {
                val label = if (i == target) 1.0f else 0.0f
                val alpha = lr * (label - state.output[i])

                state.grad.addRow(wo, i, alpha.toDouble())
                wo.addVectorToRow(state.hidden, i, alpha)
            }

        }

        val t = -log(state.output[target])
        return t
    }


}

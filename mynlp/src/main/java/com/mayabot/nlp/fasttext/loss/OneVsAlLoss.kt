package com.mayabot.nlp.fasttext.loss

import com.mayabot.nlp.blas.Matrix
import com.mayabot.nlp.common.IntArrayList
import com.mayabot.nlp.fasttext.Model

class OneVsAlLoss(wo: Matrix) : BinaryLogisticLoss(wo) {

    override fun forward(targets: IntArrayList, t_: Int, state: Model.State, lr: Float, backprop: Boolean): Float {
        var loss = 0f
        val osz = state.output.length()
        for (i in 0 until osz) {
            val isMatch = targets.contains(i)
            loss += binaryLogistic(i, state, isMatch, lr, backprop)
        }
        return loss
    }
}

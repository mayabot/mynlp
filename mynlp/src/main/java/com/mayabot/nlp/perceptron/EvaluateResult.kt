package com.mayabot.nlp.perceptron

/**
 * 评估结果
 */
data class EvaluateResult(
        /**
         * 正确率
         */
        val precision: Float,
        /**
         * 召回率
         */
        val recall: Float
) {

    constructor(goldTotal: Int, predTotal: Int, correct: Int) : this(
            (correct * 100.0 / predTotal).toFloat(),
            (correct * 100.0 / goldTotal).toFloat()
    )

    /**
     * F1综合指标
     */
    val f1: Float
        get() = (2.0 * precision * recall / (precision + recall)).toFloat()

    override fun toString(): String {
        return "正确率(P) %.2f , 召回率(R) %.2f , F1 %.2f".format(precision, recall, f1)
    }
}
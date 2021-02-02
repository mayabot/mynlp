package com.mayabot.nlp.segment.plugins.pos

import com.mayabot.nlp.perceptron.PerceptronModel
import com.mayabot.nlp.perceptron.PerceptronComputer
import java.io.File

/**
 * 通用的词性标注。
 * 格式 word/pos word/pos
 */
open class CommonPosModel(val labels: Array<String>,
                     val perceptron: PerceptronModel) {

    init {
        perceptron.decodeQuickMode(true)
    }

    protected val runner = PerceptronComputer(PosPerceptronDef(labels))

    /**
     * 解码
     */
    fun decodeWithIndex(list: List<String>): IntArray {
        return runner.decode(perceptron, list)
    }

    fun save(dir: File) {
        perceptron.save(dir)
    }

    fun learn(sample: String) {
        runner.learnModel(perceptron,sample)
    }

    /**
     * 解码
     */
    fun decode(list: List<String>): List<String> {
        val decodeResult = runner.decode(perceptron, list)
        return decodeResult.map { labels[it] }
    }

    companion object {

        fun train(labels: List<String>,
                  trainFile: File,
                  evaluateFile: File?,
                  iter: Int,
                  threadNum: Int): PerceptronModel {
            val runner = PerceptronComputer(PosPerceptronDef(labels.toTypedArray()))
            return runner.train(trainFile, evaluateFile, iter, threadNum, true)
        }
    }
}
package com.mayabot.nlp.segment.plugins.pos

import com.mayabot.nlp.perceptron.PerceptronModel
import com.mayabot.nlp.perceptron.PerceptronRunner
import com.mayabot.nlp.segment.Nature
import com.mayabot.nlp.segment.wordnet.Vertex
import java.io.File

/**
 * 词性分析感知机
 */

class POSPerceptron(
        val perceptron: PerceptronModel) {

    init {
        perceptron.decodeQuickMode(true)
    }

    val labels = InnerPos.natures

    val natureList = labels.map { Nature.parse(it) }.toTypedArray()


    val runner = PerceptronRunner(PosPerceptronDef(labels))


    init {
         learn("[m]/x 章/q")
    }

    fun decodeNature(sentence: List<String>): List<Nature> {
        val result = decodeWithIndex(sentence)
        return result.map {
            if (it == -1) {
                Nature.x
            } else {
                natureList[it]
            }
        }
    }

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
        runner.learnModel(perceptron, sample)
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
            val runner = PerceptronRunner(PosPerceptronDef(labels.toTypedArray()))
            return runner.train(trainFile, evaluateFile, iter, threadNum, true)
        }
    }
}

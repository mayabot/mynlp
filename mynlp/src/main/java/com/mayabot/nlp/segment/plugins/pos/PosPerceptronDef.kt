package com.mayabot.nlp.segment.plugins.pos

import com.mayabot.nlp.common.FastStringBuilder
import com.mayabot.nlp.common.utils.CharNormUtils
import com.mayabot.nlp.common.utils.Characters
import com.mayabot.nlp.perceptron.EvaluateFunction
import com.mayabot.nlp.perceptron.PerceptronDefinition
import com.mayabot.nlp.perceptron.PerceptronModel
import com.mayabot.nlp.segment.Nature

object InnerPos {
    val natures = Nature.values().filter {
        it != Nature.newWord
                && it != Nature.begin
                && it != Nature.end
    }.map { it.name }.sorted().toTypedArray()
}

/**
 * 语料格式。 word1/label word2/label
 */
class PosPerceptronDef
@JvmOverloads
constructor(
        val labels: Array<String> = InnerPos.natures

) : PerceptronDefinition<String, List<String>> {

    override fun labels(): Array<String> {
        return labels
    }


    override fun featureMaxSize() = 50

    /**
     *  word1/flag word2/flag
     */
    override fun parseAnnotateText(text: String): List<Pair<String, String>> {
        return text.split(' ').mapNotNull {
            val s = it.split('/')
            if (s.size == 2) {
                s[0] to s[1]
            } else {
                null
            }
        }
    }

    override fun inputList2InputSeq(list: List<String>): List<String> {
        return list
    }

    private val CHAR_BEGIN = "_B_"
    private val CHAR_END = "_E_"

    /**
     * 和前面一个词，后面一个词。
     * 词本身。
     * 词的前缀，词的后缀
     */
    override fun featureFunction(sentence: List<String>, size: Int, position: Int, buffer: FastStringBuilder, emit: () -> Unit) {

        var preWord = if (position > 0) sentence[position - 1] else null
        val curWord = sentence[position]
        var nextWord = if (position < size - 1) sentence[position + 1] else null

        if (nextWord!=null && nextWord.length == 1) {
            val c = nextWord[0]
            val isP = Characters.isPunctuation(c)
            if (isP || c == ' ') {
                // 我认为标点符号和词性无关
                nextWord = null
            }
        }

        if (preWord!=null && preWord.length == 1) {
            val c = preWord[0]
            val isP = Characters.isPunctuation(c)
            if (isP || c == ' ') {
                // 我认为标点符号和词性无关
                preWord = null
            }
        }

        if (preWord != null) {
            buffer.clear()
            buffer.append(preWord)
            buffer.append('☺')
            emit()
        }

        //让同一个特征出现两次。我认为这个特征比较重要
        buffer.clear()
        buffer.append(curWord)
        emit()
        emit()

        if (nextWord != null) {
            buffer.clear()
            buffer.append(nextWord)
            buffer.append('♂')
            emit()
        }

        val length = curWord.length

        // prefix
        if (length >= 2) {
            val last = length - 1

            val c1 = curWord[0]
            val l1 = curWord[last]

            buffer.set2(c1, '★')
            emit()

            buffer.set2(l1, '✆')
            emit()

            if (length >= 3) {
                val c2 = curWord[1]
                val l2 = curWord[last - 1]

                buffer.set3(c1, c2, '★')
                emit()

                buffer.set3(l1, l2, '✆')
                emit()

                if (length >= 4) {
                    val c3 = curWord[2]
                    val l3 = curWord[last - 2]
                    buffer.set4(c1, c2, c3, '★')
                    emit()
                    buffer.set4(l1, l2, l3, '✆')
                    emit()
                }
            }
        }
    }

    override fun evaluateFunction(perceptron: PerceptronModel): EvaluateFunction? {
        return null
    }

    override fun preProcessInputSequence(input: List<String>): List<String> {
        return input.map { CharNormUtils.convert(it) }
    }

}
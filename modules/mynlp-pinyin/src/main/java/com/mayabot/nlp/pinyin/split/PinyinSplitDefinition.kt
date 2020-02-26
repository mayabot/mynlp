package com.mayabot.nlp.pinyin.split

import com.mayabot.nlp.common.FastStringBuilder
import com.mayabot.nlp.perceptron.*

/**
 * 把一个完整联系的拼音输入。
 * wanzhengdepinyin => wan zheng de pin yin
 * ﹍
 */
class PinyinSplitDefinition : PerceptronDefinition<Char, CharArray>{

    override fun labels() = arrayOf("B", "M", "E", "S")

    override fun featureMaxSize() = 4

    override fun featureFunction(sentence: CharArray, size: Int, position: Int, buffer: FastStringBuilder, emit: () -> Unit) {

        val CHAR_NULL = '\u0000'

        val lastIndex = size - position - 1

        val pre2Char = if (position > 1) sentence[position - 2] else CHAR_NULL
        val preChar = if (position > 0) sentence[position - 1] else CHAR_NULL
        val curChar = sentence[position]
        val nextChar = if (lastIndex > 0) sentence[position + 1] else CHAR_NULL
        val next2Char = if (lastIndex > 1) sentence[position + 2] else CHAR_NULL

        buffer.clear()
        buffer.set2(curChar, '2')
        emit()

        if (position > 0) {
            buffer.clear()
            buffer.set2(preChar, '1')
            emit()

            buffer.clear()
            buffer.set4(preChar, '/', curChar, '5')
            emit()

            if (position > 1) {
                buffer.clear()
                buffer.set4(pre2Char, '/', preChar, '4')
                emit()
            }
        }

        if (lastIndex > 0) {
            buffer.clear()
            buffer.set2(nextChar, '3')
            emit()

            buffer.clear()
            buffer.set4(curChar, '/', nextChar, '6')
            emit()

            if (lastIndex > 1) {
                buffer.clear()
                buffer.set4(nextChar, '/', next2Char, '7')
                emit()
            }
        }
    }

    override fun inputList2InputSeq(list: List<Char>): CharArray {
        return list.toCharArray()
    }

    /**
     * "世界 你好" => 世/B 界/E 你/B 好/E
     * B M S E
     */
    override fun parseAnnotateText(text: String): List<Pair<Char, String>> {
        return text.splitToSequence('﹍')
                .flatMap { word ->
                    when (word.length) {
                        0 -> emptyList()
                        1 -> listOf(word[0] to "S")
                        2 -> listOf(word[0] to "B", word[1] to "E")
                        3 -> listOf(word[0] to "B", word[1] to "M", word[2] to "E")
                        4 -> listOf(word[0] to "B", word[1] to "M", word[2] to "M", word[3] to "E")
                        5 -> listOf(word[0] to "B", word[1] to "M", word[2] to "M", word[3] to "M", word[4] to "E")
                        else -> {
                            val list = ArrayList<Pair<Char, String>>(word.length)
                            list += word[0] to "B"
                            for (i in 1 until word.length - 1) {
                                list += word[i] to "M"
                            }
                            list += word[0] to "E"
                            list.toList()
                        }
                    }.asSequence()
                }.toList()
    }

    override fun evaluateFunction(model: PerceptronModel): EvaluateFunction? {
        return EvaluateFunction {sampleList->
            var count = 0
            var goldTotal = 0
            var predTotal = 0

            var correct = 0

            val segmenter = PinyinSplitApp(model)

            for (line in sampleList) {
                val wordArray = line.split("﹍")
                goldTotal += wordArray.size

                val text = wordArray.joinToString(separator = "")
                val predArray = segmenter.decodeToWordList(text)
                predTotal += predArray.size

                correct += wordCorrect(wordArray,predArray)

                count++
            }

            EvaluateResult(goldTotal, predTotal, correct)
        }
    }

    override fun preProcessInputSequence(input: CharArray): CharArray {
        return input
    }
}
//
//fun pinyinSplitEvaluateFun(id:Int, model:PerceptronModel, sampleList:List<String>) : EvaluateResult {
//
//}
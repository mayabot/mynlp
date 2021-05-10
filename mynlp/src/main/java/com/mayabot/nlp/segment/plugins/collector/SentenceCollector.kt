package com.mayabot.nlp.segment.plugins.collector

import com.mayabot.nlp.Mynlp
import com.mayabot.nlp.common.utils.StringUtils
import com.mayabot.nlp.segment.WordTerm
import com.mayabot.nlp.segment.wordnet.Wordnet
import com.mayabot.nlp.segment.wordnet.Wordpath
import java.util.function.Consumer

/**
 * WordTermCollector的默认实现，从各种数据结构中收集和生成词序列
 *
 * @author jimichan
 */
class SentenceCollector(val mynlp: Mynlp) : WordTermCollector {

    /**
     * 从wordnet中计算出子词的方法。
     */
    override var pickUpSubword: WordTermCollector.PickUpSubword? = null

    /**
     * 给一个初始化wordnet的机会，填充更多的可能性
     */
    private val fillSubword = ArrayList<WordTermCollector.FillSubword>()

    override fun addFillSubword(fs: WordTermCollector.FillSubword) {
        fillSubword += fs
    }

    override fun collect(txtChars: CharArray?, wordnet: Wordnet, wordPath: Wordpath, consumer: Consumer<WordTerm>) {

        val vertexIterator = wordPath.iteratorVertex()

        fillSubword.forEach {
            it.fill(wordnet, wordPath)
        }

        while (vertexIterator.hasNext()) {
            val vertex = vertexIterator.next()

            val word = if (txtChars == null) {
                vertex.realWord()
            } else {
                String(txtChars, vertex.offset(), vertex.length)
            }

            val term = WordTerm(word, vertex.nature, vertex.offset())

            if (StringUtils.isWhiteSpace(term.word)) {
                continue
            }

            val pick = pickUpSubword

            //给当前的term计算子词
            pick?.pickup(term, wordnet, wordPath)

            consumer.accept(term)
        }
    }
}

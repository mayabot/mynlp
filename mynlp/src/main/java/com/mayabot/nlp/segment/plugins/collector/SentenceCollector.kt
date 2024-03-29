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
class SentenceCollector(
    private val mynlp: Mynlp,
    private val subwordComputer: List<SubwordComputer> = emptyList(),
    private val setupList: List<SubwordInfoSetup> = emptyList()
) : WordTermCollector {

    override fun collect(txtChars: CharArray?, wordnet: Wordnet, wordPath: Wordpath, consumer: Consumer<WordTerm>) {

        val vertexIterator = wordPath.iteratorVertex()

        setupList.forEach {
            it.fill(wordnet, wordPath)
        }

        while (vertexIterator.hasNext()) {
            val vertex = vertexIterator.next()

            val word = if (txtChars == null) {
                vertex.realWord()
            } else {
                String(chars = txtChars, vertex.offset(), vertex.length)
            }

            val term = WordTerm(word, vertex.nature, vertex.offset())

            if (StringUtils.isWhiteSpace(term.word)) {
                continue
            }

            // 如果运行成功，后面的就不运行了
            subwordComputer.forEach {
                if (it.run(term, wordnet, wordPath)) {
                    return@forEach
                }
            }

            consumer.accept(term)
        }
    }
}
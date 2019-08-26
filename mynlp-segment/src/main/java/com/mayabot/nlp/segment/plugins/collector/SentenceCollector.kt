package com.mayabot.nlp.segment.plugins.collector

import com.mayabot.nlp.segment.WordTerm
import com.mayabot.nlp.segment.wordnet.Wordnet
import com.mayabot.nlp.segment.wordnet.Wordpath
import com.mayabot.nlp.utils.StringUtils
import java.util.function.Consumer

/**
 * Nlp收集方式，不处理子词
 * 按照WordPath里面描述的唯一切分路径，构建WordTerm序列
 *
 * @author jimichan
 */
class SentenceCollector : WordTermCollector {

    override var pickUpSubword: WordTermCollector.PickUpSubword? = null

    override var fillSubword: WordTermCollector.FillSubword? = null

    override fun collect(wordnet: Wordnet, wordPath: Wordpath, consumer: Consumer<WordTerm>) {


        val vertexIterator = wordPath.iteratorVertex()

        fillSubword?.fill(wordnet, wordPath)

        while (vertexIterator.hasNext()) {
            val vertex = vertexIterator.next()
            val term = WordTerm(vertex.realWord(), vertex.nature, vertex.rowNum)

            if (StringUtils.isWhiteSpace(term.word)) {
                continue
            }

            val pick = pickUpSubword

            if (pick != null && term.length() >= 3) {
                pick.pickup(term, wordnet, wordPath)
            }

            consumer.accept(term)
        }
    }
}

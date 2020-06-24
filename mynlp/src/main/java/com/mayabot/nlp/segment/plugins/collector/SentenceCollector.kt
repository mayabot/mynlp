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

    /**
     * 从wordnet中计算出子词的方法。
     */
    override var pickUpSubword: WordTermCollector.PickUpSubword? = null

    /**
     * 给一个初始化wordnet的机会，填充更多的可能性
     */
    override var fillSubword: WordTermCollector.FillSubword? = null

    override fun collect(txtChars: CharArray?, wordnet: Wordnet, wordPath: Wordpath, consumer: Consumer<WordTerm>) {

        val vertexIterator = wordPath.iteratorVertex()

        fillSubword?.fill(wordnet, wordPath)

        while (vertexIterator.hasNext()) {
            val vertex = vertexIterator.next()

            val word = if(txtChars==null){
                vertex.realWord()
            }else {
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

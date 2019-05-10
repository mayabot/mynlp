package com.mayabot.nlp.segment.reader

import com.mayabot.nlp.common.ParagraphReader
import com.mayabot.nlp.segment.Lexer
import com.mayabot.nlp.segment.WordTerm
import java.util.*
import java.util.function.Predicate


/**
 * WordTerm迭代器
 * @author jimichan
 */
class LexerIterator(val lexer: Lexer, val paragraphReader: ParagraphReader) : AbstractIterator<WordTerm>() {

    /**
     * 段落的偏移位置
     */
    private var baseOffset = 0

    /**
     * 内部变量
     */
    private var lastTextLength = -1

    private val buffer = LinkedList<WordTerm>()

    override fun computeNext() {
        if (buffer.isEmpty()) {
            val paragraph: String? = paragraphReader.next()

            if (paragraph == null || paragraph.isEmpty()) {
                done()
                return
            } else {
                if (lastTextLength == -1) {
                    this.baseOffset = 0
                    this.lastTextLength = 0
                } else {
                    this.baseOffset = lastTextLength
                }

                lastTextLength += paragraph.length

                val text = paragraph.toCharArray()

                lexer.scan(text) { term -> this.buffer.add(term) }

            }
        }

        if (buffer.isEmpty()) {
            done()
            return
        }

        val term = buffer.pop()

        if (term == null) {
            done()
        } else {
            //补充偏移量
            if (baseOffset != 0) {
                term.setOffset(term.getOffset() + baseOffset)
            }
            setNext(term)
        }
    }
}

/**
 * 能控制posInc的Iterator过滤器
 * @author jimichan
 */
class FilterWordItemIterator(
        private val source: Iterator<WordTerm>,
        private val predicate: Predicate<WordTerm>)
    : AbstractIterator<WordTerm>() {

    override fun computeNext() {
        var pos = 1
        while (source.hasNext()) {
            val next = source.next()
            if (predicate.test(next)) {
                next.posInc = pos
                setNext(next)
                return
            }
            pos++
        }
        done()
    }
}
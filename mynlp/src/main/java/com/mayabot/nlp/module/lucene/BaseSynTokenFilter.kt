package com.mayabot.nlp.module.lucene

import org.apache.lucene.analysis.TokenFilter
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute
import java.util.*

/**
 * 基础类；对词进行扩展
 */
abstract class BaseSynTokenFilter(input: TokenStream) : TokenFilter(input) {

    /**
     * 当前词
     */
    private val termAtt = addAttribute(CharTermAttribute::class.java)

    /**
     * Position Increment
     */
    private val positionAttr = addAttribute(
        PositionIncrementAttribute::class.java
    )

    private val buffer = LinkedList<String>()

    override fun incrementToken(): Boolean {

        if (buffer.isNotEmpty()) {
            val ele = buffer.pollFirst()
            termAtt.setEmpty().append(ele)
            positionAttr.positionIncrement = 0
            return true
        }

        val hasNext = input.incrementToken()
        if (!hasNext) {
            return false
        }

        val item = termAtt as CharSequence

        val extended = extend(item)
        buffer.addAll(extended)

        // buffer 肯定不能是空
        termAtt.setEmpty().append(buffer.pollFirst())

        return true
    }

    /**
     * 返回的list不能为空，至少要包括自己吧
     */
    abstract fun extend(item: CharSequence): List<String>

    override fun reset() {
        super.reset()
        this.buffer.clear()
    }
}
package com.mayabot.nlp.module.lucene

import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute

fun TokenStream.iterable():Iterable<String>{
    return Iterable {
        TokenStreamIterator(this)
    }
}

class TokenStreamIterator(private val tokenStream:TokenStream) : AbstractIterator<String>() {
    init {
        tokenStream.reset()
    }

    private val charTermAttr = tokenStream.getAttribute(CharTermAttribute::class.java)
    private val offsetAttr = tokenStream.getAttribute(OffsetAttribute::class.java)

    override fun computeNext() {
        val hasNext = tokenStream.incrementToken()
        if (hasNext) {
            this.setNext(charTermAttr.toString())
        }else{
            tokenStream.end()
            tokenStream.close()
            done()
        }
    }

}
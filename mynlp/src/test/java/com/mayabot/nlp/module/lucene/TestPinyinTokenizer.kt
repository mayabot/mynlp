package com.mayabot.nlp.module.lucene

import com.mayabot.nlp.Mynlp
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute
import org.junit.Test
import java.io.StringReader

class TestPinyinTokenizer {

    val pinyin = Mynlp.instance().pinyin();

    @Test
    fun test() {
        val tok = PinyinAnalyzer(pinyin,true,true,false)

        tok.pinyinTokens("飞机").forEach {
            println(it)
        }
    }

    @Test
    fun test2() {
        val tok = PinyinAnalyzer(pinyin,true,false,false)

        tok.pinyinTokens("三个 小猪").forEach {
            println(it)
        }
    }

    private fun PinyinAnalyzer.pinyinTokens(text:String):List<Item> {
        val tk = this.tokenStream("title",text)
        tk.reset()

        val charTermAttr = tk.getAttribute(CharTermAttribute::class.java)
        val offsetAttr = tk.getAttribute(OffsetAttribute::class.java)
        val posAttr = tk.getAttribute(PositionIncrementAttribute::class.java)
        val list = ArrayList<Item>()

        while (tk.incrementToken()) {
            list += Item(charTermAttr.toString(),offsetAttr.startOffset(),offsetAttr.endOffset(),posAttr.positionIncrement)
        }
        tk.end()
        tk.close()
        return list
    }

    data class Item(
        val py:String,
        val offsetStart:Int,
        val offsetEnd:Int,
        val inc:Int,
    )
}
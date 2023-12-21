package com.mayabot.nlp

import com.mayabot.nlp.module.Highlighter
import com.mayabot.nlp.module.highlight
import org.junit.Assert
import org.junit.Test

class HighlightTest {

    private val words = listOf<String>("居住证", "居住", "住宅", "hello")

    @Test
    fun test() {

        val highlighter = Highlighter(words)
        val text = "这个居住证，怎么办，居住和住宅----"

        Assert.assertEquals(highlighter.replace(text), "这个<em>居住证</em>，怎么办，<em>居住</em>和<em>住宅</em>----")
    }

    @Test
    fun test2() {
        val highlighter = Highlighter(words, "div")
        val text = "这个居住证，怎么办，居住和住宅----"

        Assert.assertEquals(highlighter.replace(text), "这个<div>居住证</div>，怎么办，<div>居住</div>和<div>住宅</div>----")
    }

    @Test
    fun test3() {
        val text = "这个居住证，怎么办，居住和住宅----"

        val result = text.highlight(words)

        Assert.assertEquals(result, "这个<em>居住证</em>，怎么办，<em>居住</em>和<em>住宅</em>----")
    }

    /**
     * 大小写
     */
    @Test
    fun test4() {
        val text = "Hello word !"

        val result = text.highlight(words)

        Assert.assertEquals("<em>Hello</em> word !", result)
    }

    /**
     * 大小写
     */
    @Test
    fun test5() {
        val text = "HEllo word !"

        val result = text.highlight(words)

        Assert.assertEquals("<em>HEllo</em> word !", result)
    }
}
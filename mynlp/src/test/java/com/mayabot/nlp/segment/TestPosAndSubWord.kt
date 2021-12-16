package com.mayabot.nlp.segment

import com.mayabot.nlp.Mynlp
import org.junit.Assert
import org.junit.Test

/**
 * 同时开启词性和subword，导致词性失效
 */
class TestPosAndSubWord {

    @Test
    fun test() {
        val mynlp = Mynlp.instance()

        val lexer = mynlp.lexerBuilder().hmm()
            .withPos()
            .customSentenceCollector {
                it.smartSubword()
                it.fillCoreDict()
            }
            .build()

        val result = lexer.scan("这次是北京大学拿到第一名").toString()
        Assert.assertEquals("这次/r 是/v [北京 大学]/nt 拿到/v 第一名/mq",result)
    }

}
package com.mayabot.nlp.segment

import com.mayabot.nlp.Mynlp
import com.mayabot.nlp.segment.plugins.collector.DefaultSubwordRuleDict
import org.junit.Test

class SubwordTest {

    @Test
    fun test() {
        val mynlp = Mynlp.instance()

        val x = DefaultSubwordRuleDict()
        x.add("副/市长")
        x.rebuild()

        val lexer = mynlp.lexerBuilder()
            .hmm()
            .withPos()
            .customSentenceCollector {
                it.smartSubword()
//                it.ruleBaseSubword(listOf(x))
            }
            .build()

        println(lexer.scan("这是副市长的快递").toList())
    }
}
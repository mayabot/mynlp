package com.mayabot.nlp.segment

import com.mayabot.nlp.segment.plugins.customwords.MemCustomDictionary
import org.junit.Test

class CustomDictTest {


    @Test
    fun test() {
        val mem = MemCustomDictionary()
//        mem.addWord("居转户");
        mem.rebuild()

        val lexer = Lexers.coreBuilder()
                .withCustomDictionary(mem)
                .build()

        println(lexer.scan("居转户"))
    }
}
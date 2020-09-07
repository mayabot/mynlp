package com.mayabot.nlp.segment

import com.mayabot.nlp.segment.plugins.customwords.MemCustomDictionary
import org.junit.Test

class CustomDictTest {


    @Test
    fun test() {
        val mem = MemCustomDictionary()
        mem.addWord("科学之门");
        mem.rebuild()

        val lexer = Lexers.coreBuilder()
                .withCustomDictionary(mem)
                .build()

        println(lexer.scan("科学之门"))
    }
}
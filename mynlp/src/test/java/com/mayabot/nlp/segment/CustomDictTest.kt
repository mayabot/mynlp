package com.mayabot.nlp.segment

import com.mayabot.nlp.segment.plugins.customwords.MemCustomDictionary
import org.junit.Test

class CustomDictTest {


    @Test
    fun test() {
        val mem = MemCustomDictionary()
        mem.addWord("长江1号");
        mem.addWord("ECS固收");
        mem.addWord("固收");
        mem.rebuild()

        mem.clear()

        mem.addWord("固收");
        mem.rebuild()

        val lexer = Lexers.coreBuilder()
            .withCustomDictionary(mem)
            .collector()
            .smartPickup()
            .fillSubwordCustomDict(mem)
            .done()
            .build()

        println(lexer.scan("ECS固收"))
        println("----")
        lexer.scan("ECS固收").forEach { w ->
            println(w.subword)
        }
    }
}
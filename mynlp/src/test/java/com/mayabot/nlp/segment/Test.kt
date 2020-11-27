package com.mayabot.nlp.segment

import com.mayabot.nlp.Mynlp
import com.mayabot.nlp.segment.plugins.correction.MemCorrectionDictionary

fun main() {
    val mynlp = Mynlp.instance()
    val mem = MemCorrectionDictionary()

    mem.addRule("安徽省/政府")
    mem.rebuild()

    val lexer = mynlp.lexerBuilder()
            .bigram()
            .withPos()
            .withPersonName()
            .collector().smartPickup().done()
//            .withCorrection(mem)
            .build()

    println(lexer.scan("安徽省政府网站居住证办理身份证办理"))
}
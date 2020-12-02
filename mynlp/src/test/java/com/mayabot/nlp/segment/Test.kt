package com.mayabot.nlp.segment

import com.mayabot.nlp.Mynlp
import com.mayabot.nlp.segment.plugins.correction.MemCorrectionDictionary

fun main() {
    val mynlp = Mynlp.instance()
    val mem = MemCorrectionDictionary()

//    mem.addRule("安徽省/政府")
//    mem.rebuild()

    val lexer = mynlp.lexerBuilder()
            .bigram()
            .withPos()
            .withPersonName()
            .collector().smartPickup {
                it.setBlackListCallback {
                    it[0] == '副' && it[it.length - 1] == '长'
                }
            }
            .done()
//            .withCorrection(mem)
            .build()

    lexer.scan("副市长 副省长").forEach {
        print(it)
        println("\t has sub " + it.hasSubword())
    }


    //default core
//    val lexer2 = Lexers.coreBuilder()
//            .withPersonName()
////            .withPos()
//            .collector().smartPickup()
//            .done()
//            .build()
//
//    println(lexer2.scan("基础设施"))
}
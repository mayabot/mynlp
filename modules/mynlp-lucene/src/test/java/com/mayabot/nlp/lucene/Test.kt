package com.mayabot.nlp.lucene

import com.mayabot.nlp.segment.AtomIterable
import com.mayabot.nlp.segment.Lexers

fun main() {

    AtomIterable(Lexers.coreBuilder().collector().indexPickup().done().build().scan("上海市委召开会议")
    ).forEach {
        println(it)
    }
}
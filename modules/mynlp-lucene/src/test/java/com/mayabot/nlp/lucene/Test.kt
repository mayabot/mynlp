package com.mayabot.nlp.lucene

import com.mayabot.nlp.segment.FlattenIterable
import com.mayabot.nlp.segment.Lexers

fun main() {

    FlattenIterable(Lexers.coreBuilder().collector().indexedSubword().ok().build().
            scan("上海市委召开会议")
    ).forEach {
        println(it)
    }
}
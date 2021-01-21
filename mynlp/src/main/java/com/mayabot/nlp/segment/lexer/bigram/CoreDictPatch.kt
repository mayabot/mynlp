package com.mayabot.nlp.segment.lexer.bigram

import com.mayabot.nlp.common.injector.Singleton
import org.jetbrains.annotations.Nullable

interface CoreDictPatch {
    fun appendDict(): List<Pair<String, Int>>
    fun deleteDict(): List<String>
    fun appendBiGram(): List<BiGram>
    fun dictVersion(): String
    fun biGramVersion(): String
}

data class BiGram(
        val wordA: String, val wordB: String, val count: Int
)

@Singleton
class CoreDictPathWrap {

    @Nullable
    val coreDictPatch: CoreDictPatch? = null
}
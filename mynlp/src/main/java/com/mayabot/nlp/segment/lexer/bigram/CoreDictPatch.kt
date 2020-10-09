package com.mayabot.nlp.segment.lexer.bigram

import com.mayabot.nlp.common.injector.Singleton
import org.jetbrains.annotations.Nullable

interface CoreDictPatch {
    fun dictVersion(): String
    fun biGramVersion(): String

    fun addDict(): List<Pair<String, Int>>
    fun deleteDict(): List<String>

    fun addBiGram(): List<BiGram>
}

data class BiGram(
        val wordA: String, val wordB: String, val count: Int
)

@Singleton
class CoreDictPathWrap {

    @Nullable
    val coreDictPatch: CoreDictPatch? = null
}
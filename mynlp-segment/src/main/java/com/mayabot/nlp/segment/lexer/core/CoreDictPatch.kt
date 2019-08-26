package com.mayabot.nlp.segment.lexer.core

import com.google.inject.Inject
import com.google.inject.Singleton

interface CoreDictPatch {
    fun dictVersion() :String
    fun biGramVersion() :String

    fun addDict() : List<Pair<String,Int>>
    fun deleteDict() : List<String>

    fun addBiGram(): List<BiGram>
}

data class BiGram(
        val wordA:String,val wordB:String,val count:Int
)

@Singleton
class CoreDictPathWrap{
    @field:Inject(optional = true)
    val coreDictPatch: CoreDictPatch? = null
}
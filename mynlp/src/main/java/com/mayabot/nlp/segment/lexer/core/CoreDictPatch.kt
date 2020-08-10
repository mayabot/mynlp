package com.mayabot.nlp.segment.lexer.core

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

//FIXME  @Nullable自己实现的，需要注意这个注解，运行注入空，默认不行
//FIXME coreDictPatch 声明为属性，是否能自动注入
@Singleton
class CoreDictPathWrap {

    @Nullable
    val coreDictPatch: CoreDictPatch? = null
}
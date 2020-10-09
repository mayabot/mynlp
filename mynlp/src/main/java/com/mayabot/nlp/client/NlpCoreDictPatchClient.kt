package com.mayabot.nlp.client

import com.mayabot.nlp.MynlpConfigs
import com.mayabot.nlp.MynlpEnv
import com.mayabot.nlp.segment.lexer.bigram.BiGram
import com.mayabot.nlp.segment.lexer.bigram.CoreDictPatch

class NlpCoreDictPatchClient(mynlpEnv: MynlpEnv) : CoreDictPatch {

    val server = mynlpEnv.get(MynlpConfigs.server).trim()

    init {
        if (server.isNotBlank()) {
            println("Mynlp set to $server")
        }
    }

    override fun addBiGram(): List<BiGram> {
        if (server.isEmpty()) {
            return listOf()
        }

        return listOf()
    }

    override fun addDict(): List<Pair<String, Int>> {
        if (server.isEmpty()) {
            return listOf()
        }
        return listOf()
    }

    override fun deleteDict(): List<String> {
        if (server.isEmpty()) {
            return listOf()
        }
        return listOf()
    }

    override fun dictVersion(): String {
        if (server.isEmpty()) {
            return ""
        }
        return ""
    }


    override fun biGramVersion(): String {
        if (server.isEmpty()) {
            return ""
        }

        return ""
    }


    data class CoreDictItem(
            val word: String,
            val operate: String
    )


}
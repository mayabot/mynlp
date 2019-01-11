package com.mayabot.nlp.segment.cws

import com.google.inject.Inject
import com.google.inject.Singleton
import com.mayabot.nlp.MynlpEnv

@Singleton
class CwsPatch
@Inject
constructor(val mynlpEnv: MynlpEnv) {

    val examples = ArrayList<String>()

    init {
        examples += loadExample("patch/cws-default.txt")
        examples += loadExample("patch/cws.txt")
    }

    fun addExample(line: String) {
        examples += line
    }

    fun removeExample(line: String) {
        examples.remove(line)
    }

    fun addResources(rsName: String) {
        examples += loadExample(rsName)
    }

    private fun loadExample(rsName: String): List<String> {
        val resource = mynlpEnv.loadResource(rsName)
        if (resource != null) {
            return resource.openLineReader().asSequence().map { it.trim() }.filter {
                it.isNotBlank() && !it.startsWith("#")
            }.toList()
        }
        return listOf()
    }
}
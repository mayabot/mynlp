package com.mayabot.nlp.segment.lexer.bigram

import com.mayabot.nlp.Mynlp
import com.mayabot.nlp.MynlpEnv
import com.mayabot.nlp.common.Guava.split

class CoreDictionaryReader(val env: MynlpEnv) {

    constructor(mynlp: Mynlp) : this(mynlp.env)

    var totalFreq = 0

    fun read(blocker: (String, Int) -> Unit) {

        val dictResource = env.loadResource(CoreDictionaryImpl.path)
            ?: throw RuntimeException("Not Found dict resource " + CoreDictionaryImpl.path)

        dictResource.inputStream().bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.forEach { line ->
                val param = split(line, " ")
                if (param.size == 2) {
                    val count = Integer.valueOf(param[1])
                    blocker(param[0], count)
                    totalFreq += count
                }
            }
        }
    }
}

fun readCoreDict(blocker: (String, Int) -> Unit) {
    CoreDictionaryReader(Mynlp.instance()).read(blocker)
}
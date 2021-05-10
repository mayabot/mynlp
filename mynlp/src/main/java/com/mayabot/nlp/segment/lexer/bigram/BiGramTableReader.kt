package com.mayabot.nlp.segment.lexer.bigram

import com.mayabot.nlp.Mynlp
import com.mayabot.nlp.MynlpEnv
import com.mayabot.nlp.common.Guava.split

class BiGramTableReader(private val env: MynlpEnv) {
    constructor(mynlp: Mynlp) : this(mynlp.env)

    fun read(blocker: (String, String, Int) -> Unit) {

        val dictResource = env.loadResource(BiGramTableDictionaryImpl.path)
            ?: throw RuntimeException("Not Found dict resource " + BiGramTableDictionaryImpl.path)

        var firstWord: String? = null

        dictResource.inputStream().bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.forEach { line ->
                if (line.startsWith("\t")) {
                    val firstWh = line.indexOf(" ")
                    val numString = line.substring(1, firstWh)
                    val num = numString.toInt()
                    val words = split(line.substring(firstWh + 1), " ")
                    val wordA = firstWord!!

                    for (wordB in words) {
                        blocker(wordA, wordB, num)
                    }
                } else {
                    firstWord = line
                }
            }
        }

    }
}

fun readCoreBigramTable(blocker: (String, String, Int) -> Unit) {
    BiGramTableReader(Mynlp.instance()).read(blocker)
}

package com.mayabot.nlp.segment.reader

import com.google.inject.Inject
import com.google.inject.Singleton
import com.mayabot.nlp.MynlpEnv

const val StopWordDictPath = "stopword-dict/stopwords.txt"

/**
 * 停用词词典
 *
 * @author jimichan
 */
@Singleton
class StopWordDict
@Inject constructor(env: MynlpEnv) {

    private val stopWords: Set<String>

    init {

        val resource = env.loadResource(StopWordDictPath)
                ?: throw RuntimeException("Not found $StopWordDictPath Resource")

        stopWords = resource.openInputStream().bufferedReader()
                .lineSequence()
                .map {
                    it.trim()
                }.filter { it.isNotBlank() }
                .toSet()
    }

    fun getStopWords(): Set<String> {
        return stopWords
    }
}

package com.mayabot.nlp.transform

import java.util.*
import javax.inject.Singleton

/**
 * 简体转繁体的词典
 *
 * @author jimichan
 */
@Singleton
class Simplified2Traditional : BaseTransformDictionary() {

    override fun loadDictionary(): TreeMap<String, String> {
        return loadFromResource(RS_NAME)
    }

    companion object {
        private val RS_NAME = "ts-dict/s2t.txt"
    }
}

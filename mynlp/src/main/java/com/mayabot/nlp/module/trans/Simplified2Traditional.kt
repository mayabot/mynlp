package com.mayabot.nlp.module.trans

import com.mayabot.nlp.common.injector.Singleton
import java.util.*

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

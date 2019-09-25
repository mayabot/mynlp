package com.mayabot.nlp.transform

import com.mayabot.nlp.injector.Singleton
import java.util.TreeMap

/**
 * 繁体转简体的词典
 *
 * @author jimichan
 */
@Singleton
class Traditional2Simplified : BaseTransformDictionary() {

    override fun loadDictionary(): TreeMap<String, String> {
        return loadFromResource(RS_NAME)
    }

    companion object {

        private val RS_NAME = "ts-dict/t2s.txt"
    }
}

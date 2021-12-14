package com.mayabot.nlp.segment.plugins.collector

import com.mayabot.nlp.segment.wordnet.Wordnet
import com.mayabot.nlp.segment.wordnet.Wordpath

/**
 * 感知机、crf等分词，wordnet中没有子词信息。那么通过这个接口在收集结果之前，通过词典新增子词信息。
 * @author jimichan
 */
interface SubwordInfoSetup {
    fun fill(wordnet: Wordnet, wordPath: Wordpath)
}
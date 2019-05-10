package com.mayabot.nlp.segment.plugins

import com.mayabot.nlp.segment.PipelineLexerBuilder

/**
 * 人名识别
 * @author jimichan
 */
interface PersonNameRecognition {
    fun setEnablePersonName(enable: Boolean): PipelineLexerBuilder
}

/**
 * 地名和组织机构名识别
 * @author jimichan
 */
interface NERRecognition {
    fun setEnableNER(enable: Boolean): PipelineLexerBuilder
}


/**
 * 用户自定义词典
 * @author jimichan
 */
interface CustomDictionaryRecognition {
    fun setEnableCustomDictionary(enable: Boolean): PipelineLexerBuilder
}

/**
 * 词性标注
 * @author jimichan
 */
interface PartOfSpeechTagging {
    fun setEnablePOS(enable: Boolean): PipelineLexerBuilder
}

/**
 * 分词纠错
 * @author jimichan
 */
interface Correction {
    fun setEnableCorrection(enable: Boolean): PipelineLexerBuilder
}
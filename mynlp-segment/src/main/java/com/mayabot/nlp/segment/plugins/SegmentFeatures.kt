package com.mayabot.nlp.segment.plugins

import com.mayabot.nlp.segment.PipelineTokenizerBuilder

/**
 * 人名识别
 * @author jimichan
 */
interface PersonNameRecognition {
    fun setEnablePersonName(enable: Boolean): PipelineTokenizerBuilder
}

/**
 * 地名和组织机构名识别
 * @author jimichan
 */
interface NERRecognition {
    fun setEnableNER(enable: Boolean): PipelineTokenizerBuilder
}


/**
 * 用户自定义词典
 * @author jimichan
 */
interface CustomDictionaryRecognition {
    fun setEnableCustomDictionary(enable: Boolean): PipelineTokenizerBuilder
}

/**
 * 词性标注
 * @author jimichan
 */
interface PartOfSpeechTagging {
    fun setEnablePOS(enable: Boolean): PipelineTokenizerBuilder
}

/**
 * 分词纠错
 * @author jimichan
 */
interface Correction {
    fun setEnableCorrection(enable: Boolean): PipelineTokenizerBuilder
}
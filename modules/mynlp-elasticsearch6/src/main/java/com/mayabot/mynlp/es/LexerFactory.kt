package com.mayabot.mynlp.es

import com.mayabot.nlp.segment.Lexer
import com.mayabot.nlp.segment.Lexers
import org.elasticsearch.common.settings.Settings

/**
 * @author jimichan
 */
class LexerFactory {

    var type = "core" //  cws

    var filterPunctuaction = true

    var filterStopword = false

    var isIndexWordModel = false

    /**
     * 分词纠错
     */
    var isCorrection = true

    constructor(settings: Settings) {
        filterPunctuaction = settings.getAsBoolean("punctuation", true)
        filterStopword = settings.getAsBoolean("stopword", false)
        isCorrection = settings.getAsBoolean("correction", false)
        isIndexWordModel = settings.getAsBoolean("index-model", false)
    }

    fun build() = buildLexer().filterReader(filterPunctuaction, filterStopword)

    private fun buildLexer(): Lexer {
        return if ("cws".equals(type, ignoreCase = true)) {
            val builder = Lexers.cwsTokenizerBuilder()
            builder.isEnableCorrection = isCorrection
            builder.isEnableIndexModel = isIndexWordModel
            builder.build()
        } else {
            val builder = Lexers.coreTokenizerBuilder()
            builder.isEnableCorrection = isCorrection
            builder.isEnableIndexModel = isIndexWordModel
            builder.build()
        }
    }

}
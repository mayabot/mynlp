package com.mayabot.mynlp.es

import com.mayabot.nlp.Mynlps
import com.mayabot.nlp.segment.FluentLexerBuilder
import com.mayabot.nlp.segment.Lexer
import com.mayabot.nlp.segment.core.CoreDictionary
import com.mayabot.nlp.segment.plugins.collector.TermCollectorMode
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
        isIndexWordModel = settings.getAsBoolean("index-model", false)
    }

    fun build() = buildLexer().filterReader(filterPunctuaction, filterStopword)

    private fun buildLexer(): Lexer {
        return if ("cws".equals(type, ignoreCase = true)) {
            val builder2 = FluentLexerBuilder.builder()
                    .basic().cws()

            if (isIndexWordModel) {
                builder2.collector().collectorIndex(
                        TermCollectorMode.MIXED,
                        Mynlps.instanceOf(CoreDictionary::class.java))
            }

            builder2.build()
        } else {
            val builder2 = FluentLexerBuilder.builder()
                    .basic().core()

            if (isIndexWordModel) {
                builder2.collector().collectorIndex(TermCollectorMode.MIXED)
            }

            builder2.build()
        }
    }

}
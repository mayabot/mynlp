package com.mayabot.mynlp.es

import com.mayabot.nlp.segment.FluentLexerBuilder
import com.mayabot.nlp.segment.Lexer
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


    constructor(settings: Settings) {
        filterPunctuaction = settings.getAsBoolean("punctuation", true)
        filterStopword = settings.getAsBoolean("stopword", false)
        isIndexWordModel = settings.getAsBoolean("index-model", false)
    }

    fun build() = buildLexer().filterReader(filterPunctuaction, filterStopword)

    private fun buildLexer(): Lexer {
        return if ("cws".equals(type, ignoreCase = true)) {
            val builder = FluentLexerBuilder.builder()
                    .basic().cws()

            if (isIndexWordModel) {
                builder.collector {
                    model = TermCollectorMode.MIXED
                    dictMoreSubword()
                    indexedSubword(2)
                }
            }

            builder.build()
        } else {
            val builder = FluentLexerBuilder.builder()
                    .basic().core()

            if (isIndexWordModel) {
                builder.collector {
                    model = TermCollectorMode.MIXED
                    indexedSubword(2)
                }
            }

            builder.build()
        }
    }

}
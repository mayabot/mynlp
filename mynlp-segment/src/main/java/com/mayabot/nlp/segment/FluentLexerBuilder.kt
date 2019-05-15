package com.mayabot.nlp.segment

import com.mayabot.nlp.segment.core.CoreLexerPlugin
import com.mayabot.nlp.segment.core.DictionaryMatcher
import com.mayabot.nlp.segment.cws.CwsLexerPlugin
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin
import com.mayabot.nlp.segment.plugins.collector.IndexCollectorPlugin
import com.mayabot.nlp.segment.plugins.collector.SentenceCollectorPlugin
import com.mayabot.nlp.segment.plugins.collector.TermCollectorMode
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryPlugin
import com.mayabot.nlp.segment.plugins.ner.NerPlugin
import com.mayabot.nlp.segment.plugins.personname.PersonNamePlugin
import com.mayabot.nlp.segment.plugins.pos.PosPlugin

/**
 * Fluent style
 * @author jimichan
 */
class FluentLexerBuilder : LexerBuilder {

    companion object {
        @JvmStatic
        fun builder() = FluentLexerBuilder()
    }

    override fun build(): Lexer {
        return builder.build()
    }

    val builder = PipelineLexerBuilder.builder()

    fun basic() = Basic()

    fun withPos(): FluentLexerBuilder {
        builder.install(PosPlugin())
        return this
    }

    fun withPersonName(): FluentLexerBuilder {
        builder.install(PersonNamePlugin())
        return this
    }

    fun withNer(): FluentLexerBuilder {
        builder.install(NerPlugin())
        return this
    }

    fun withCustomDictionary(): FluentLexerBuilder {
        builder.install(CustomDictionaryPlugin())
        return this
    }

    fun with(plugin: PipelineLexerPlugin) {
        builder.install(plugin)
    }

    fun collector(): Collector = Collector()

    inner class Basic {
        fun core(): FluentLexerBuilder {
            builder.install(CoreLexerPlugin())
            return this@FluentLexerBuilder
        }

        fun coreByDict(dict: DictionaryMatcher): FluentLexerBuilder {
            builder.install(CoreLexerPlugin(dict))
            return this@FluentLexerBuilder
        }

        fun cws(): FluentLexerBuilder {
            builder.install(CwsLexerPlugin())
            return this@FluentLexerBuilder
        }
    }

    inner class Collector {
        @JvmOverloads
        fun collectorSentence(mode: TermCollectorMode = TermCollectorMode.TOP): FluentLexerBuilder {
            builder.install(SentenceCollectorPlugin(mode))
            return this@FluentLexerBuilder
        }

        @JvmOverloads
        fun collectorIndex(mode: TermCollectorMode = TermCollectorMode.TOP,
                           subDict: DictionaryMatcher? = null): FluentLexerBuilder {
            builder.install(IndexCollectorPlugin(mode).apply { subwordDictionary = subDict })
            return this@FluentLexerBuilder
        }
    }

}



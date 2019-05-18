package com.mayabot.nlp.segment

import com.mayabot.nlp.segment.lexer.core.CoreLexerPlugin
import com.mayabot.nlp.segment.lexer.core.DictionaryMatcher
import com.mayabot.nlp.segment.lexer.perceptron.CwsLexerPlugin
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin
import com.mayabot.nlp.segment.plugins.collector.SentenceCollectorPlugin
import com.mayabot.nlp.segment.plugins.collector.SentenceCollectorPluginBuilder
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryPlugin
import com.mayabot.nlp.segment.plugins.ner.NerPlugin
import com.mayabot.nlp.segment.plugins.personname.PersonNamePlugin
import com.mayabot.nlp.segment.plugins.pos.PosPlugin

/**
 * Fluent style
 * @author jimichan
 */
open class FluentLexerBuilder : LexerBuilder {

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

    fun collector(xbuilder: SentenceCollectorPluginBuilder): FluentLexerBuilder {
        val scPlugin = xbuilder.build()
        builder.install(scPlugin)
        return this
    }

    fun collector(block: SentenceCollectorPluginBuilder.()->Unit): FluentLexerBuilder {
        val scPlugin = SentenceCollectorPlugin.builder()
        scPlugin.block()
        builder.install(scPlugin.build())
        return this
    }

    fun collector(collector: WordTermCollector): FluentLexerBuilder {
        builder.termCollector = collector
        return this
    }

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
}



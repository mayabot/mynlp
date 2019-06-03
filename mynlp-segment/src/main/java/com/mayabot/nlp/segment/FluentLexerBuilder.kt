package com.mayabot.nlp.segment

import com.google.common.base.Preconditions
import com.mayabot.nlp.Mynlps
import com.mayabot.nlp.segment.lexer.core.CoreDictionary
import com.mayabot.nlp.segment.lexer.core.CoreLexerPlugin
import com.mayabot.nlp.segment.lexer.core.DictionaryMatcher
import com.mayabot.nlp.segment.lexer.perceptron.CwsLexerPlugin
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin
import com.mayabot.nlp.segment.plugins.collector.*
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

    fun basic() = BasicBlock()

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

    fun collector(): CollectorBlock {
        return CollectorBlock()
    }

    inner class CollectorBlock {

        var plugin = SentenceCollectorPlugin()
        var pluginSelf = false
        var collector: WordTermCollector? = null

        fun subwordCollector(subwordCollector: SubwordCollector) {
            Preconditions.checkState(!pluginSelf)
            plugin.subwordCollector = subwordCollector
        }

        @JvmOverloads
        fun indexedSubword(minWordLen: Int = 2): CollectorBlock {
            Preconditions.checkState(!pluginSelf)
            val subwordCollector = IndexSubwordCollector()
            subwordCollector.minWordLength = minWordLen
            plugin.subwordCollector = subwordCollector
            return this
        }

        @JvmOverloads
        fun dictMoreSubword(dbcms: DictionaryMatcher = Mynlps.instanceOf(CoreDictionary::class.java)): CollectorBlock {
            Preconditions.checkState(!pluginSelf)
            plugin.computeMoreSubword = DictBasedComputeMoreSubword(dbcms)
            return this
        }


        fun with(plugin: SentenceCollectorPlugin) : CollectorBlock{
            this.plugin = plugin
            this.pluginSelf = true
            return this
        }

        fun with(collector: WordTermCollector) : CollectorBlock{
            builder.termCollector = collector
            return this
        }

        fun ok() : FluentLexerBuilder{

            if (collector != null) {
                builder.termCollector = collector
            }else{
                builder.install(plugin)
            }

            return this@FluentLexerBuilder
        }
    }

    inner class BasicBlock {
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



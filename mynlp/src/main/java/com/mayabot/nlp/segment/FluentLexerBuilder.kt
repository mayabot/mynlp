package com.mayabot.nlp.segment

import com.mayabot.nlp.Mynlp
import com.mayabot.nlp.segment.lexer.bigram.BigramLexerPlugin
import com.mayabot.nlp.segment.lexer.bigram.CoreDictionary
import com.mayabot.nlp.segment.lexer.perceptron.PerceptronSegmentPlugin
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin
import com.mayabot.nlp.segment.plugins.collector.*
import com.mayabot.nlp.segment.plugins.correction.CorrectionDictionary
import com.mayabot.nlp.segment.plugins.correction.CorrectionPlugin
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionary
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryPlugin
import com.mayabot.nlp.segment.plugins.ner.NerPlugin
import com.mayabot.nlp.segment.plugins.personname.PersonNamePlugin
import com.mayabot.nlp.segment.plugins.pos.PosPlugin

/**
 * Fluent style
 * @author jimichan
 */
open class FluentLexerBuilder(val mynlp: Mynlp = Mynlp.instance()) : LexerBuilder {

    private val builder = PipelineLexerBuilder(mynlp)

    override fun build(): Lexer {
        return builder.build()
    }

    fun install(plugin: PipelineLexerPlugin) {
        builder.install(plugin)
    }

    @Deprecated(message = "使用bigram方法", replaceWith = ReplaceWith("bigram"), level = DeprecationLevel.WARNING)
    fun core(): FluentLexerBuilder {
        builder.install(BigramLexerPlugin(mynlp))
        return this@FluentLexerBuilder
    }

    @Deprecated(message = "使用bigram方法", replaceWith = ReplaceWith("bigram(dict)"), level = DeprecationLevel.WARNING)
    fun coreByDict(dict: CoreDictionary): FluentLexerBuilder {
        builder.install(BigramLexerPlugin(dict))
        return this@FluentLexerBuilder
    }

    fun bigram(): FluentLexerBuilder {
        builder.install(BigramLexerPlugin(mynlp))
        return this@FluentLexerBuilder
    }

    fun bigram(dict: CoreDictionary): FluentLexerBuilder {
        builder.install(BigramLexerPlugin(dict))
        return this@FluentLexerBuilder
    }

    fun perceptron(): FluentLexerBuilder {
        builder.install(PerceptronSegmentPlugin())
        return this@FluentLexerBuilder
    }

    fun withCorrection(): FluentLexerBuilder {
        builder.install(CorrectionPlugin())
        return this;
    }

    fun withCorrection(dict: CorrectionDictionary): FluentLexerBuilder {
        builder.install(CorrectionPlugin(dict))
        return this;
    }

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

    fun withCustomDictionary(dict: CustomDictionary): FluentLexerBuilder {
        builder.install(CustomDictionaryPlugin(dict))
        return this
    }

    fun withCustomDictionary(): FluentLexerBuilder {
        builder.install(CustomDictionaryPlugin())
        return this
    }

    fun with(plugin: PipelineLexerPlugin) : FluentLexerBuilder {
        builder.install(plugin)
        return this
    }

    /**
     * 保持字符原样输出
     */
    fun keepOriCharOutput() : FluentLexerBuilder {
        builder.isKeepOriCharOutput = true
        return this
    }

    fun collector(): CollectorBlock {
        return CollectorBlock()
    }

    inner class CollectorBlock {

        val collector: WordTermCollector = SentenceCollector(mynlp)

        fun pickUpSubword(pickUpSubword: WordTermCollector.PickUpSubword): CollectorBlock {
            collector.pickUpSubword = pickUpSubword
            return this
        }

        fun fillSubword(fillSubword: WordTermCollector.FillSubword): CollectorBlock {
            collector.fillSubword = fillSubword
            return this
        }

        @JvmOverloads
        fun indexPickup(minWordLen: Int = 2): CollectorBlock {
            val indexd = IndexPickUpSubword()
            indexd.minWordLength = minWordLen
            collector.pickUpSubword = indexd
            return this
        }

        @JvmOverloads
        fun smartPickup(block: (x: WordTermCollector.PickUpSubword) -> Unit
                        = { _ -> Unit }
        ): CollectorBlock {
            val p = SmartPickUpSubword(mynlp)
            block(p)
            collector.pickUpSubword = p
            return this
        }

        @JvmOverloads
        fun fillSubwordDict(dbcms: CoreDictionary = mynlp.getInstance(CoreDictionary::class.java)): CollectorBlock {
            collector.fillSubword = DictBasedFillSubword(dbcms)
            return this
        }

        fun with(collector: WordTermCollector): CollectorBlock {
            builder.termCollector = collector
            return this
        }

        fun done(): FluentLexerBuilder {
            builder.termCollector = collector
            return this@FluentLexerBuilder
        }
    }
}



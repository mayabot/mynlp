package com.mayabot.nlp.segment

import com.mayabot.nlp.Mynlp
import com.mayabot.nlp.segment.lexer.bigram.CoreDictionary
import com.mayabot.nlp.segment.lexer.bigram.HmmLexerPlugin
import com.mayabot.nlp.segment.lexer.perceptron.PerceptronSegmentPlugin
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin
import com.mayabot.nlp.segment.plugins.collector.SentenceCollectorBuilder
import com.mayabot.nlp.segment.plugins.collector.WordTermCollector
import com.mayabot.nlp.segment.plugins.correction.CorrectionDictionary
import com.mayabot.nlp.segment.plugins.correction.CorrectionPlugin
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionary
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryPlugin
import com.mayabot.nlp.segment.plugins.ner.NerPlugin
import com.mayabot.nlp.segment.plugins.personname.PersonNamePlugin
import com.mayabot.nlp.segment.plugins.pos.PosPlugin
import java.util.function.Consumer

/**
 * Fluent style
 * @author jimichan
 */
open class FluentLexerBuilder(private val mynlp: Mynlp) : LexerBuilder {

    private val pipeline = PipelineLexerBuilder(mynlp)

    override fun build(): Lexer {
        return pipeline.build()
    }

    fun install(plugin: PipelineLexerPlugin) {
        pipeline.install(plugin)
    }

    fun hmm(): FluentLexerBuilder {
        pipeline.install(HmmLexerPlugin(mynlp))
        return this@FluentLexerBuilder
    }

    fun hmm(dict: CoreDictionary): FluentLexerBuilder {
        pipeline.install(HmmLexerPlugin(dict))
        return this@FluentLexerBuilder
    }

    @Deprecated(message = "使用hmm方法", replaceWith = ReplaceWith("hmm"), level = DeprecationLevel.WARNING)
    fun core(): FluentLexerBuilder {
        pipeline.install(HmmLexerPlugin(mynlp))
        return this@FluentLexerBuilder
    }

    @Deprecated(message = "使用hmm方法", replaceWith = ReplaceWith("hmm(dict)"), level = DeprecationLevel.WARNING)
    fun coreByDict(dict: CoreDictionary): FluentLexerBuilder {
        pipeline.install(HmmLexerPlugin(dict))
        return this@FluentLexerBuilder
    }

    @Deprecated(message = "使用hmm方法", replaceWith = ReplaceWith("hmm"), level = DeprecationLevel.WARNING)
    fun bigram(): FluentLexerBuilder {
        pipeline.install(HmmLexerPlugin(mynlp))
        return this@FluentLexerBuilder
    }

    @Deprecated(message = "使用hmm方法", replaceWith = ReplaceWith("hmm(dict)"), level = DeprecationLevel.WARNING)
    fun bigram(dict: CoreDictionary): FluentLexerBuilder {
        pipeline.install(HmmLexerPlugin(dict))
        return this@FluentLexerBuilder
    }

    /**
     * 感知机分词器
     */
    fun perceptron(): FluentLexerBuilder {
        pipeline.install(PerceptronSegmentPlugin())
        return this@FluentLexerBuilder
    }

    fun withCorrection(): FluentLexerBuilder {
        pipeline.install(CorrectionPlugin())
        return this;
    }

    fun withCorrection(dict: CorrectionDictionary): FluentLexerBuilder {
        pipeline.install(CorrectionPlugin(dict))
        return this;
    }

    fun withPos(): FluentLexerBuilder {
        pipeline.install(PosPlugin())
        return this
    }

    fun withPersonName(): FluentLexerBuilder {
        pipeline.install(PersonNamePlugin())
        return this
    }

    fun withNer(): FluentLexerBuilder {
        pipeline.install(NerPlugin())
        return this
    }

    fun withCustomDictionary(dict: CustomDictionary): FluentLexerBuilder {
        pipeline.install(CustomDictionaryPlugin(dict))
        return this
    }

    fun withCustomDictionary(): FluentLexerBuilder {
        pipeline.install(CustomDictionaryPlugin())
        return this
    }

    fun with(plugin: PipelineLexerPlugin): FluentLexerBuilder {
        pipeline.install(plugin)
        return this
    }

    /**
     * 保持字符原样输出
     */
    fun keepOriCharOutput(): FluentLexerBuilder {
        pipeline.isKeepOriCharOutput = true
        return this
    }

    /**
     * 配置默认SentenceCollector
     */
    fun customSentenceCollector(consumer: Consumer<SentenceCollectorBuilder>): FluentLexerBuilder {
        val builder = SentenceCollectorBuilder(mynlp)

        consumer.accept(builder)

        pipeline.termCollector = builder.build()
        return this
    }

    /**
     * 安装自定义的WordTermCollector
     */
    fun withCollector(collector: WordTermCollector): FluentLexerBuilder {
        pipeline.termCollector = collector
        return this
    }
}


package com.mayabot.nlp.segment.plugins.collector

import com.mayabot.nlp.Mynlps
import com.mayabot.nlp.segment.lexer.core.CoreDictionary
import com.mayabot.nlp.segment.lexer.core.DictionaryMatcher
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin

class SentenceCollectorPluginBuilder {

    var model: TermCollectorMode? = null

    var subwordCollector: SubwordCollector? = null

    var computeMoreSubword: ComputeMoreSubword? = null

    fun subwordCollector(subwordCollector: SubwordCollector) {
        this.subwordCollector = subwordCollector
    }

    fun atom(): SentenceCollectorPluginBuilder {
        this.model = TermCollectorMode.ATOM
        return this
    }

    fun mixed(): SentenceCollectorPluginBuilder {
        this.model = TermCollectorMode.MIXED
        return this
    }

    fun top(): SentenceCollectorPluginBuilder {
        this.model = TermCollectorMode.TOP
        return this
    }

    @JvmOverloads
    fun indexedSubword(minWordLen: Int = 2): SentenceCollectorPluginBuilder {
        val subwordCollector = IndexSubwordCollector()
        subwordCollector.minWordLength = minWordLen
        this.subwordCollector = subwordCollector
        if (model == null) {
            model = TermCollectorMode.MIXED
        }
        return this
    }

    @JvmOverloads
    fun dictMoreSubword(dbcms: DictionaryMatcher = Mynlps.instanceOf(CoreDictionary::class.java)): SentenceCollectorPluginBuilder {
        this.computeMoreSubword = DictBasedComputeMoreSubword(dbcms)
        return this
    }

    fun build(): PipelineLexerPlugin {
        if (model == null) {
            model = TermCollectorMode.TOP
        }
        return SentenceCollectorPlugin().apply {
            model = this@SentenceCollectorPluginBuilder.model!!
            subwordCollector = this@SentenceCollectorPluginBuilder.subwordCollector
            computeMoreSubword = this@SentenceCollectorPluginBuilder.computeMoreSubword
        }
    }
}

/**
 * @author jimichan
 */
class SentenceCollectorPlugin : PipelineLexerPlugin {

    var model = TermCollectorMode.TOP

    var subwordCollector: SubwordCollector? = null

    var computeMoreSubword: ComputeMoreSubword? = null

    override fun install(builder: PipelineLexerBuilder) {

        val ic = SentenceCollector()
        ic.model = model
        ic.computeMoreSubword = computeMoreSubword
        ic.subwordCollector = subwordCollector

        builder.termCollector = ic
    }


    companion object {
        @JvmStatic
        fun builder(): SentenceCollectorPluginBuilder {
            return SentenceCollectorPluginBuilder()
        }
    }

}

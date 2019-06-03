package com.mayabot.nlp.segment.plugins.collector

import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin

/**
 * @author jimichan
 */
class SentenceCollectorPlugin : PipelineLexerPlugin {

    var subwordCollector: SubwordCollector? = null

    var computeMoreSubword: ComputeMoreSubword? = null

    override fun install(builder: PipelineLexerBuilder) {

        val ic = SentenceCollector()
        ic.computeMoreSubword = computeMoreSubword
        ic.subwordCollector = subwordCollector

        builder.termCollector = ic
    }


}

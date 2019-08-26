package com.mayabot.mynlp.es

import com.mayabot.nlp.segment.Lexer
import com.mayabot.nlp.segment.Lexers
import com.mayabot.nlp.segment.WordTermIterableMode
import org.elasticsearch.common.settings.Settings

/**
 * 计算字词，应该是方式选择，而不是boolean。
 *
 * @author jimichan
 */
class LexerFactory {

    var type = "core" //  cws

    var filterPunctuaction = true

    var filterStopword = false
    var personName = false

    var subWord = "none" // smart index

    val mode: WordTermIterableMode // graph flatten

    /**
     * 分词纠错
     */
    var isCorrection = true

    constructor(settings: Settings) {
        filterPunctuaction = settings.getAsBoolean("filter-punctuation", true)
        filterStopword = settings.getAsBoolean("filter-stopword", false)
        subWord = settings.get("sub-word", "none") // smart index
        mode = when (settings.get("mode", "top").toLowerCase()) {
            "top" -> WordTermIterableMode.TOP
            "atom" -> WordTermIterableMode.ATOM
            "overlap" -> WordTermIterableMode.Overlap
            else -> WordTermIterableMode.TOP
        }
        personName = settings.getAsBoolean("personName", settings.getAsBoolean("personname", true))
    }

    fun build() = buildLexer().filterReader(filterPunctuaction, filterStopword)

    private fun buildLexer(): Lexer {

        if ("cws" == type) {
            val builder = Lexers.perceptronBuilder()

            if (subWord != "none") {
                val cb = builder.collector()
                cb.fillSubwordDict()
                if ("smart" == subWord) {
                    cb.smartPickup()
                } else if ("index" == subWord) {
                    cb.indexPickup()
                }
                cb.done()
            }

            return builder.build()
        } else {
            //default core

            val builder = Lexers.coreBuilder()

            if (personName) {
                builder.withPersonName()
            }

            if (subWord != "none") {
                val cb = builder.collector()
                if ("smart" == subWord) {
                    cb.smartPickup()
                } else if ("index" == subWord) {
                    cb.indexPickup()
                }
                cb.done()
            }

            return builder.build()
        }

    }

}
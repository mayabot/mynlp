package com.mayabot.nlp.segment.plugins.collector

import com.mayabot.nlp.Mynlp
import com.mayabot.nlp.segment.lexer.bigram.CoreDictionary
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionary

/**
 * SentenceCollector 构建器
 */
class SentenceCollectorBuilder(
    val mynlp: Mynlp
) {

    private val subwordComputer = ArrayList<SubwordComputer>()


    private val setupList = ArrayList<SubwordInfoSetup>()

    /**
     * 增加一个SubwordInfoSetup
     */
    fun addSubwordInfoSetup(setup: SubwordInfoSetup) {
        setupList += setup
    }

    /**
     * 安装默认的索引子词切分算法
     */
    @JvmOverloads
    fun indexSubword(minWordLen: Int = 2): SentenceCollectorBuilder {
        subwordComputer += IndexSubwordComputer().apply {
            minWordLength = minWordLen
        }
        return this
    }

    /**
     * 安装智能子词切分算法
     */
    fun smartSubword(): SentenceCollectorBuilder {
        val p = SmartSubwordComputer(mynlp)
        subwordComputer += p
        return this
    }

    /**
     * 安装基于词典的切分算法
     */
    fun ruleBaseSubword(dict: List<SubwordRuleDict>): SentenceCollectorBuilder {
        val p = RuleDictSubwordComputer(dict)
        subwordComputer += p
        return this
    }

    @JvmOverloads
    fun fillCoreDict(dbcms: CoreDictionary = mynlp.getInstance(CoreDictionary::class.java)): SentenceCollectorBuilder {
        addSubwordInfoSetup(
            CoreDictSubwordInfoSetup(
                dbcms
            )
        )
        return this
    }

    fun fillCustomDict(dict: CustomDictionary): SentenceCollectorBuilder {
        addSubwordInfoSetup(
            CustomDictSubwordInfoSetup(
                dict
            )
        )
        return this
    }

    fun build(): SentenceCollector {
        return SentenceCollector(mynlp, subwordComputer, setupList)
    }

}
package com.mayabot.nlp.segment.plugins.collector

import com.mayabot.nlp.segment.WordTerm
import com.mayabot.nlp.segment.wordnet.Wordnet
import com.mayabot.nlp.segment.wordnet.Wordpath
import org.jetbrains.annotations.NotNull

/**
 * 子词切分计算器接口
 *
 * 从wordnet中计算出子词的所需要的基本信息，计算结果保存在WordTerm的subword字段里面
 * @author jimichan
 */
interface SubwordComputer {

    /**
     * [term] 一个待切分的子词
     * [wordnet] 当前
     */
    fun pickup(@NotNull term: WordTerm, @NotNull wordnet: Wordnet, @NotNull wordPath: Wordpath)

}
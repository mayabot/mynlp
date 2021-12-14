package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.lexer.bigram.BiGramTableDictionary;
import com.mayabot.nlp.segment.lexer.bigram.CoreDictionary;
import com.mayabot.nlp.segment.plugins.bestpath.AtomWordViterbiBestPathAlgorithm;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 智能的子词二次切分算法。
 * <p>
 * 通过mini的Viterbi算法，选择一个最佳的切分方式。
 *
 * @author jimichan
 */

public class SmartSubwordComputer implements SubwordComputer {

    //    private Mynlp  mynlp;
    private AtomWordViterbiBestPathAlgorithm algorithm;

    private CoreDictionary coreDictionary;
    private BiGramTableDictionary biGramTableDictionary;

    /**
     * 外部程序控制是否进一步拆分.返回true表示不再拆分
     */
    private Function<String, Boolean> blackListCallback;

    public SmartSubwordComputer(@NotNull Mynlp mynlp) {
//        this.mynlp = mynlp;
        algorithm = mynlp.getInstance(AtomWordViterbiBestPathAlgorithm.class);
        coreDictionary = mynlp.getInstance(CoreDictionary.class);
        biGramTableDictionary = mynlp.getInstance(BiGramTableDictionary.class);
    }


    /**
     * 拆分结果保存到term中去
     *
     * @param term
     * @param wordnet
     * @param wordPath
     */
    @Override
    public void pickup(WordTerm term, Wordnet wordnet, Wordpath wordPath) {

        //2个字的不拆
        //3.3.0版本开始变成2个字不拆。但是三字是否切分，需要看是否存在bigram搭配（要求严格点）
        if (term.length() <= 2) {
            return;
        }


        // 人名不拆
        if (term.getNature() == Nature.nr) {
            return;
        }

        //时间怎么拆

        //

        List<Vertex> list = algorithm.selectSub(wordnet, term.offset, term.length());
        if (list != null) {
            List<WordTerm> subList = new ArrayList<>(list.size());
            int len = 0;
            for (Vertex v : list) {
                WordTerm x = new WordTerm(v.realWord(), v.nature, v.getRowNum());
                len += v.length;
                subList.add(x);
            }

            // [省 政府]/n
            // 如果是3字词，切分为两片。那么要求在biGramTableDictionary中包含一个pair

            if (len == term.length()) {

                if (blackListCallback != null) {
                    if (blackListCallback.apply(term.word)) {
                        return;
                    }
                }

                if (len == 3 && subList.size() == 2) {
                    if (this.biGramTableDictionary.getBiFrequency(list.get(0).wordID, list.get(1).wordID) > 0) {
                        term.setSubword(subList);
                    }
                } else {
                    term.setSubword(subList);
                }

            }

        }
    }

    /**
     * 外部程序控制是否进一步拆分.返回true表示不再拆分
     *
     * @param blackListCallback
     * @return SmartPickUpSubword
     */
    public SmartSubwordComputer setBlackListCallback(Function<String, Boolean> blackListCallback) {
        this.blackListCallback = blackListCallback;
        return this;
    }
}

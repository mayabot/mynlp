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

/**
 * Nlp收集方式，不处理子词
 * 按照WordPath里面描述的唯一切分路径，构建WordTerm序列
 *
 * @author jimichan
 */

public class SmartPickUpSubword implements WordTermCollector.PickUpSubword {

    //    private Mynlp  mynlp;
    private AtomWordViterbiBestPathAlgorithm algorithm;

    private CoreDictionary coreDictionary;
    private BiGramTableDictionary biGramTableDictionary;

    public SmartPickUpSubword(@NotNull Mynlp mynlp) {
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

        //三个字的不拆
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
                System.out.println(v.wordID);
            }

            // [省 政府]/n
            // 如果是3字词，切分为两片。那么要求在biGramTableDictionary中包含一个pair

            if (len == term.length()) {
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


}

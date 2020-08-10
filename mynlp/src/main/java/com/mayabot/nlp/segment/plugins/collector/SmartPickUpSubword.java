package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.plugins.bestpath.AtomWordViterbiBestPathAlgorithm;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.ArrayList;
import java.util.List;

/**
 * Nlp收集方式，不处理子词
 * 按照WordPath里面描述的唯一切分路径，构建WordTerm序列
 *
 * @author jimichan
 */

public class SmartPickUpSubword implements WordTermCollector.PickUpSubword {

    private AtomWordViterbiBestPathAlgorithm algorithm = Mynlps.get().getInstance(AtomWordViterbiBestPathAlgorithm.class);

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
        if (term.length() <= 3) {
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
            for (Vertex v : list) {
                WordTerm x = new WordTerm(v.realWord(), v.nature, v.getRowNum());
                subList.add(x);
            }
            term.setSubword(subList);
        }
    }


}

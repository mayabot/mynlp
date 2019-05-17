package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.VertexRow;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.ArrayList;
import java.util.List;

/**
 * 收集子词所有的可能性组合
 * @author jimichan
 */
public class IndexSubwordCollector implements SubwordCollector {
    /**
     * 字词的最小长度
     */
    private int minWordLength = 2;

    @Override
    public void subWord(WordTerm term, Wordnet wordnet, Wordpath wordPath) {
        if (term.length() >= 3) {
            int from = term.offset;
            int to = from + term.length();
            final int lastIndex = term.length() + term.offset;

            List<WordTerm> list = new ArrayList<>();

            for (int i = term.offset; i < to; i++) {
                VertexRow row = wordnet.getRow(i);

                for (Vertex small = row.first(); small != null; small = small.next()) {
                    if (small.length >= minWordLength && i + small.length() <= lastIndex && small.length != term.length()) {

                        WordTerm smallterm = new WordTerm(small.realWord(), small.nature, small.getRowNum());

                        list.add(smallterm);
                    }
                }
            }
            if (!list.isEmpty()) {
                term.setSubword(list);
            }
        }
    }

    public int getMinWordLength() {
        return minWordLength;
    }

    public IndexSubwordCollector setMinWordLength(int minWordLength) {
        this.minWordLength = minWordLength;
        return this;
    }
}

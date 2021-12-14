package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.VertexRow;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 所谓的Index类型的子词切分。
 * <p>
 * 收集子词所有的可能性组合，比如 北京大学 => 北京 大学 京大
 * <p>
 * 这里的实现也不是通过ngram把所有的可能性组合一遍，应该和Wordnet中的信息有关系
 *
 * @author jimichan
 */
public class IndexSubwordComputer implements SubwordComputer {
    /**
     * 字词的最小长度
     */
    private int minWordLength = 2;

    @Override
    public boolean run(WordTerm term, @NotNull Wordnet wordnet, @NotNull Wordpath wordPath) {
        boolean result = false;
        if (term.length() >= 3) {
            int from = term.offset;
            int to = from + term.length();
            final int lastIndex = term.length() + term.offset;

            List<WordTerm> list = new ArrayList<>();

            int lastMaxPoint = term.offset - 1;

            for (int i = term.offset; i < to; i++) {
                VertexRow row = wordnet.getRow(i);

                for (Vertex small = row.first(); small != null; small = small.next()) {
                    if(small.length == term.length()){
                        continue;
                    }

                    if (i + small.length() <= lastIndex) {
                        WordTerm smallterm = new WordTerm(small.realWord(), small.nature, small.getRowNum());

                        if (small.length >= minWordLength ||
                                (i > lastMaxPoint && (small.next() == null||small.next().length == term.length()))
                        ) {
                            list.add(smallterm);
                            int lp = i + small.length - 1;
                            if(lp>lastMaxPoint) {lastMaxPoint = lp;}
                        }

                    }

                }
            }
            if (!list.isEmpty()) {
                term.setSubword(list);
                result = true;
            }
        }
        return result;
    }

    public int getMinWordLength() {
        return minWordLength;
    }

    public IndexSubwordComputer setMinWordLength(int minWordLength) {
        this.minWordLength = minWordLength;
        return this;
    }
}

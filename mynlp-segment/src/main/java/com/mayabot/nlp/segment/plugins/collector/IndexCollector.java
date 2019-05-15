package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.VertexRow;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.ArrayList;
import java.util.List;

/**
 * 对长词进行切分字词操作,结果保存在WordTerm的subword里面
 * <p>
 * 从词图中寻找字词切分方法。
 * 如果设定多个DAT词典，那么在这里进行wordnet填充（适用于CRF、感知机分词等非词典分词方式）
 * <p>
 * folded来控制是否平铺或者折叠.
 *
 * @author jimichan
 */
public class IndexCollector extends AbstractWordTermCollector {

    /**
     * 字词的最小长度
     */
    private int minWordLength = 2;

    /**
     * 长词长度最小值。默认三字词以上算长词
     */
    private int longWordLength = 3;

    /**
     * 折叠true，表示把长词的字词放到WordTerm的subword属性里面
     */
    private boolean folded = false;


    public IndexCollector(TermCollectorMode model) {
        super(model);
        setComputeSubword(true);
    }

    @Override
    void fillSubWord(WordTerm term, Wordnet wordnet, Wordpath wordPath) {
        if (term.length() >= longWordLength) {
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

    public IndexCollector setMinWordLength(int minWordLength) {
        this.minWordLength = minWordLength;
        return this;
    }

    public IndexCollector setFolded(boolean folded) {
        this.folded = folded;
        return this;
    }

    public boolean isFolded() {
        return folded;
    }

    public int getLongWordLength() {
        return longWordLength;
    }

    public IndexCollector setLongWordLength(int longWordLength) {
        this.longWordLength = longWordLength;
        return this;
    }
}

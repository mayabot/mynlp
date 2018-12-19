package com.mayabot.nlp.segment.plugins.collector;

import com.google.common.collect.Lists;
import com.mayabot.nlp.collection.dat.DATMapMatcher;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieMap;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.WordTermCollector;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.VertexRow;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;
import com.mayabot.nlp.utils.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
public class SentenceIndexWordCollector implements WordTermCollector {

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

    /**
     * 排除指定的字词。如 副市长 的子词 市长，不希望被提取出来
     */
    private DoubleArrayTrieMap<String[]> excludeDict;

    /**
     * 可以控制长词是否被分解
     * 判断条件返回true，表示不做分解
     */
    private Predicate<Vertex> excludeFilter;


    /**
     * 字词的词典
     */
    private DoubleArrayTrieMap[] dictList;


    @Override
    public void collect(Wordnet wordnet, Wordpath wordPath, Consumer<WordTerm> consumer) {

        char[] text = wordnet.getCharArray();

        //如果词图里面没有细分词的话。
        if (dictList != null) {
            for (DoubleArrayTrieMap dict : dictList) {
                // 核心词典查询
                DATMapMatcher searcher = dict.match(text, 0);

                while (searcher.next()) {

                    int offset = searcher.getBegin();
                    int length = searcher.getLength();

                    VertexRow row = wordnet.getRow(offset);

                    if (!row.contains(length)) {
                        Vertex v = new Vertex(length);
                        wordnet.put(offset, v);
                    }

                }
            }
        }

        Iterator<Vertex> vertexIterator = wordPath.iteratorVertex();
        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();

            WordTerm term = new WordTerm(vertex.realWord(), vertex.nature, vertex.getRowNum());

            if (StringUtils.isWhiteSpace(term.word)) {
                continue;
            }

            consumer.accept(term);

            if (vertex.length >= longWordLength) {

                if (excludeFilter != null && excludeFilter.test(vertex)) {
                    continue;
                }

                List<WordTerm> subwords = null;
                if (folded) {
                    subwords = Lists.newArrayList();
                    term.setSubword(subwords);
                }

                final int lastIndex = vertex.length + vertex.getRowNum();

                int from = vertex.getRowNum();
                int to = from + vertex.length;

                String[] exclude = null;
                if (excludeDict != null) {
                    exclude = excludeDict.get(text, from, vertex.length);
                }

                for (int i = from; i < to; i++) {
                    VertexRow row = wordnet.getRow(i);

                    loop:
                    for (Vertex small = row.first(); small != null; small = small.next()) {
                        if (small.length >= minWordLength && i + small.length() <= lastIndex && small != vertex) {
                            String word = small.realWord();

                            if (exclude != null) {
                                if (exclude.length == 1) {
                                    if (exclude[0].equals(word)) {
                                        continue;
                                    }
                                } else {
                                    for (String s : exclude) {
                                        if (s.equals(word)) {
                                            continue loop;
                                        }
                                    }
                                }
                            }

                            WordTerm smallterm = new WordTerm(small.realWord(), small.nature, small.getRowNum());

                            if (folded) {
                                subwords.add(smallterm);
                            } else {
                                consumer.accept(smallterm);
                            }

                        }
                    }
                }
            }
        }
    }

    public int getMinWordLength() {
        return minWordLength;
    }

    public SentenceIndexWordCollector setMinWordLength(int minWordLength) {
        this.minWordLength = minWordLength;
        return this;
    }

    public SentenceIndexWordCollector setFolded(boolean folded) {
        this.folded = folded;
        return this;
    }

    public boolean isFolded() {
        return folded;
    }

    public SentenceIndexWordCollector setExcludeDict(DoubleArrayTrieMap<String[]> excludeDict) {
        this.excludeDict = excludeDict;
        return this;
    }

    public SentenceIndexWordCollector setExcludeDict(TreeMap<String, String[]> excludeDict) {
        this.excludeDict = new DoubleArrayTrieMap<>(excludeDict);
        return this;
    }

    public Predicate<Vertex> getExcludeFilter() {
        return excludeFilter;
    }

    public SentenceIndexWordCollector setExcludeFilter(Predicate<Vertex> excludeFilter) {
        this.excludeFilter = excludeFilter;
        return this;
    }

    public int getLongWordLength() {
        return longWordLength;
    }

    public SentenceIndexWordCollector setLongWordLength(int longWordLength) {
        this.longWordLength = longWordLength;
        return this;
    }

    public DoubleArrayTrieMap[] getDictList() {
        return dictList;
    }

    public SentenceIndexWordCollector setDictList(DoubleArrayTrieMap[] dictList) {
        this.dictList = dictList;
        return this;
    }

    public SentenceIndexWordCollector setDictList(List<DoubleArrayTrieMap> dictList) {
        this.dictList = dictList.toArray(new DoubleArrayTrieMap[0]);
        return this;
    }
}

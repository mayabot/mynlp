package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.collection.dat.DoubleArrayTrieStringIntMap;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.WordTermCollector;
import com.mayabot.nlp.segment.core.DictionaryMatcher;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;
import com.mayabot.nlp.utils.StringUtils;

import java.util.Iterator;
import java.util.function.Consumer;

public abstract class AbstractWordTermCollector implements WordTermCollector {

    private TermCollectorModel model;
    private boolean computeSubword = false;

    private DictionaryMatcher subwordDictionary = null;

    public AbstractWordTermCollector(TermCollectorModel model) {
        this.model = model;
    }

    abstract void fillSubWord(WordTerm term, Wordnet wordnet, Wordpath wordPath);

    /**
     * 在抽取结果之前有机会
     *
     * @param wordnet
     * @param wordPath
     */
    void fixWordnet(Wordnet wordnet, Wordpath wordPath) {
        DictionaryMatcher dictionary = subwordDictionary;
        if (dictionary != null) {
            char[] text = wordnet.getCharArray();

            // 核心词典查询
            DoubleArrayTrieStringIntMap.DATMapMatcherInt searcher = dictionary.match(text, 0);

            while (searcher.next()) {
                int offset = searcher.getBegin();
                int length = searcher.getLength();
                int wordId = searcher.getIndex();

                Vertex v = new Vertex(length, wordId, searcher.getValue());

                wordnet.put(offset, v);
            }
        }
    }


    @Override
    public void collect(Wordnet wordnet, Wordpath wordPath, Consumer<WordTerm> consumer) {

        Iterator<Vertex> vertexIterator = wordPath.iteratorVertex();

        fixWordnet(wordnet, wordPath);

        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();
            WordTerm term = new WordTerm(vertex.realWord(), vertex.nature, vertex.getRowNum());

            if (StringUtils.isWhiteSpace(term.word)) {
                continue;
            }

            if (computeSubword && term.length() >= 3) {
                fillSubWord(term, wordnet, wordPath);
            }

            switch (model) {
                case TOP:
                    consumer.accept(term);
                    break;
                case ATOM:
                    if (term.hasSubword()) {
                        for (WordTerm s : term.getSubword()) {
                            consumer.accept(s);
                        }
                    } else {
                        consumer.accept(term);
                    }
                    break;
                case MIXED:
                    consumer.accept(term);
                    if (term.hasSubword()) {
                        for (WordTerm s : term.getSubword()) {
                            consumer.accept(s);
                        }
                    }
                    break;
            }
        }
    }


    public DictionaryMatcher getSubwordDictionary() {
        return subwordDictionary;
    }

    public AbstractWordTermCollector setSubwordDictionary(DictionaryMatcher subwordDictionary) {
        this.subwordDictionary = subwordDictionary;
        return this;
    }

    public boolean isComputeSubword() {
        return computeSubword;
    }

    public AbstractWordTermCollector setComputeSubword(boolean computeSubword) {
        this.computeSubword = computeSubword;
        return this;
    }

}
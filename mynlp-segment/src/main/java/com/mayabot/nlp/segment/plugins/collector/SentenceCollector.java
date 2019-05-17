package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.WordTermCollector;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;
import com.mayabot.nlp.utils.StringUtils;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Nlp收集方式，不处理子词
 * 按照WordPath里面描述的唯一切分路径，构建WordTerm序列
 *
 * @author jimichan
 */
public class SentenceCollector implements WordTermCollector {

    private TermCollectorMode model = TermCollectorMode.TOP;

    private SubwordCollector subwordCollector;

    private ComputeMoreSubword computeMoreSubword;

    public SentenceCollector() {

    }

    @Override
    public void collect(Wordnet wordnet, Wordpath wordPath, Consumer<WordTerm> consumer) {

        Iterator<Vertex> vertexIterator = wordPath.iteratorVertex();

        if (computeMoreSubword != null) {
            computeMoreSubword.fill(wordnet,wordPath);
        }

        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();
            WordTerm term = new WordTerm(vertex.realWord(), vertex.nature, vertex.getRowNum());

            if (StringUtils.isWhiteSpace(term.word)) {
                continue;
            }

            if (subwordCollector!=null && term.length() >= 3) {
                subwordCollector.subWord(term, wordnet, wordPath);
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

    public TermCollectorMode getModel() {
        return model;
    }

    public SentenceCollector setModel(TermCollectorMode model) {
        this.model = model;
        return this;
    }

    public SubwordCollector getSubwordCollector() {
        return subwordCollector;
    }

    public SentenceCollector setSubwordCollector(SubwordCollector subwordCollector) {
        this.subwordCollector = subwordCollector;
        return this;
    }

    public ComputeMoreSubword getComputeMoreSubword() {
        return computeMoreSubword;
    }

    public SentenceCollector setComputeMoreSubword(ComputeMoreSubword computeMoreSubword) {
        this.computeMoreSubword = computeMoreSubword;
        return this;
    }
}

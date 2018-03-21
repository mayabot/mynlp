package com.mayabot.nlp.segment;

import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.Iterator;
import java.util.List;

/**
 * Mynlp收集器
 *
 * @author jimichan
 */
public interface MynlpTermCollector {

    void collect(Wordnet wordnet, Wordpath wordPath, List<MynlpTerm> target);

    /**
     * 最优路径选择器。
     * 如果有subword，双层结构表示。类似于语料库中处理复合词的做法
     * 北京人民大学  =》 北京 [人民 大学]
     */
    MynlpTermCollector bestPath = (wordnet, wordPath, target) -> {
        Iterator<Vertex> vertexIterator = wordPath.iteratorBestPath();
        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();

            MynlpTerm term = new MynlpTerm(vertex.realWord(), vertex.guessNature());
            term.setOffset(vertex.getRowNum());

            if (vertex.subWords != null) {
                term.setSubword(Lists.newArrayListWithCapacity(vertex.subWords.size()));
                for (Vertex subWord : vertex.subWords) {
                    MynlpTerm sub = new MynlpTerm(subWord.realWord(), null);
                    sub.setOffset(subWord.getRowNum());
                    term.getSubword().add(sub);
                }
            }

            target.add(term);
        }
    };


    /**
     * 最优路径选择器，子词平铺
     * 如果有子词，那么平铺，抛弃原有的组合词
     * 北京人民大学  =》 北京 人民 大学
     */
    MynlpTermCollector bestpath_subword_flat = (wordnet, wordPath, target) -> {
        Iterator<Vertex> vertexIterator = wordPath.iteratorBestPath();
        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();

            if (vertex.subWords != null) {
                for (Vertex subWord : vertex.subWords) {
                    MynlpTerm sub = new MynlpTerm(subWord.realWord(), null);
                    sub.setOffset(subWord.getRowNum());
                    target.add(sub);
                }
            } else {
                MynlpTerm term = new MynlpTerm(vertex.realWord(), vertex.guessNature());
                term.setOffset(vertex.getRowNum());

                target.add(term);
            }

        }
    };


    /**
     * 索引分词。只有求得所有组合的可能性
     */
    MynlpTermCollector indexs_ = (wordnet, wordPath, target) -> {
        Iterator<Vertex> vertexIterator = wordPath.iteratorBestPath();
        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();

            if (vertex.subWords != null) {
                for (Vertex subWord : vertex.subWords) {
                    MynlpTerm sub = new MynlpTerm(subWord.realWord(), null);
                    sub.setOffset(subWord.getRowNum());
                    target.add(sub);
                }
            } else {
                MynlpTerm term = new MynlpTerm(vertex.realWord(), vertex.guessNature());
                term.setOffset(vertex.getRowNum());

                target.add(term);
            }

        }
    };
}

package com.mayabot.nlp.segment.core;

import com.google.inject.Inject;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieStringIntMap;
import com.mayabot.nlp.segment.SegmentComponentOrder;
import com.mayabot.nlp.segment.WordSplitAlgorithm;
import com.mayabot.nlp.segment.common.BaseSegmentComponent;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;

/**
 * 基于核心词典的基础切词器
 *
 * @author jimichan
 */
public class CoreDictionarySplitAlgorithm extends BaseSegmentComponent implements WordSplitAlgorithm {

    private CoreDictionary coreDictionary;

    @Inject
    public CoreDictionarySplitAlgorithm(CoreDictionary coreDictionary) {
        this.coreDictionary = coreDictionary;
        setOrder(SegmentComponentOrder.FIRST);
    }

    @Override
    public void fill(Wordnet wordnet) {
        char[] text = wordnet.getCharArray();

        // 核心词典查询
        DoubleArrayTrieStringIntMap.DATMapMatcherInt searcher = coreDictionary.match(text, 0);

        while (searcher.next()) {
            int offset = searcher.getBegin();
            int length = searcher.getLength();
            int wordId = searcher.getIndex();

            Vertex v = new Vertex(length, wordId, searcher.getValue());

            wordnet.put(offset, v);
        }
    }

}
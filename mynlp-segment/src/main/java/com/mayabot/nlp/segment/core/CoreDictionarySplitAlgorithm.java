package com.mayabot.nlp.segment.core;

import com.mayabot.nlp.collection.dat.DoubleArrayTrieStringIntMap.DATMapMatcherInt;
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

    private DictionaryMatcher coreDictionary;


    /**
     * 切分字词模式。传入的是一个词。那么排除词典中已有的这个大词
     */
//    private final boolean subWordModel;

    public CoreDictionarySplitAlgorithm(DictionaryMatcher coreDictionary) {
        this(coreDictionary, false);
    }

    public CoreDictionarySplitAlgorithm(DictionaryMatcher coreDictionary,
                                        boolean subWordModel) {
        this.coreDictionary = coreDictionary;
//        this.subWordModel = subWordModel;
        setOrder(SegmentComponentOrder.FIRST);
    }


    @Override
    public void fill(Wordnet wordnet) {
        char[] text = wordnet.getCharArray();

        // 核心词典查询
        DATMapMatcherInt searcher = coreDictionary.match(text, 0);

        while (searcher.next()) {
            int offset = searcher.getBegin();
            int length = searcher.getLength();
            int wordId = searcher.getIndex();

//            if (subWordModel && length == text.length) {
//                continue;
//            }

            Vertex v = new Vertex(length, wordId, searcher.getValue());

            wordnet.put(offset, v);
        }
    }

}
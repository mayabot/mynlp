package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.collection.dat.DoubleArrayTrieStringIntMap;
import com.mayabot.nlp.segment.lexer.core.DictionaryMatcher;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

/**
 * 基于词典的子词补全
 */
public class DictBasedComputeMoreSubword  implements ComputeMoreSubword{

    private DictionaryMatcher dictionary;

    public DictBasedComputeMoreSubword(DictionaryMatcher dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public void fill(Wordnet wordnet, Wordpath wordPath) {
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

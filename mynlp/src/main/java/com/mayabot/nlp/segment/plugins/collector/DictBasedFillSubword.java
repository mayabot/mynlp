package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.algorithm.collection.dat.DoubleArrayTrieStringIntMap;
import com.mayabot.nlp.segment.lexer.bigram.CoreDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;
import org.jetbrains.annotations.NotNull;

/**
 * 基于词典的子词补全
 */
public class DictBasedFillSubword implements WordTermCollector.FillSubword {

    private CoreDictionary dictionary;

    public DictBasedFillSubword(CoreDictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public void fill(@NotNull Wordnet wordnet, @NotNull Wordpath wordPath) {
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

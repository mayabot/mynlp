package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.algorithm.collection.dat.DoubleArrayTrieStringIntMap;
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;
import org.jetbrains.annotations.NotNull;

/**
 * 基于词典的子词补全.
 * 一般在感知机分词器，需要补全
 */
public class CustomDictSubwordInfoSetup implements SubwordInfoSetup {

    private CustomDictionary dictionary;

    public CustomDictSubwordInfoSetup(CustomDictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public void fill(@NotNull Wordnet wordnet, @NotNull Wordpath wordPath) {
        DoubleArrayTrieStringIntMap trie = dictionary.getTrie();
        if (trie == null) {
            return;
        }
        char[] text = wordnet.getCharArray();
        DoubleArrayTrieStringIntMap.DATMapMatcherInt searcher = trie.match(text, 0);

        while (searcher.next()) {
            int offset = searcher.getBegin();
            int length = searcher.getLength();

            Vertex v = new Vertex(length, -1, searcher.getValue());
            if (!wordnet.row(offset).contains(length)) {
                wordnet.put(offset, v);
            }
        }
    }


}

package com.mayabot.nlp.segment.dictionary.correction;

import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import com.mayabot.nlp.segment.dictionary.CorrectionDictionary;

import java.util.TreeMap;

/**
 * 内存版本CustomDictionary
 *
 * @author jimichan
 */
public class MemCorrectionDictionary implements CorrectionDictionary {

    private TreeMap<String, AdjustWord> dict;

    private DoubleArrayTrie<AdjustWord> trie;

    public MemCorrectionDictionary(TreeMap<String, AdjustWord> dict) {
        this.dict = dict;
        rebuild();
    }

    public MemCorrectionDictionary() {
        this.dict = new TreeMap<>();
        rebuild();
    }

    public void rebuild() {
        if (dict.isEmpty()) {
            trie = null;
            return;
        }
        trie = new DoubleArrayTrieBuilder().build(dict);
    }

    public void addWord(String rule) {
        AdjustWord adjustWord = AdjustWord.parse(rule
        );
        dict.put(adjustWord.path, adjustWord);
    }

    public void removeWord(String word) {
        dict.remove(word);
    }

    @Override
    public DoubleArrayTrie<AdjustWord> getTrie() {
        return trie;
    }

}

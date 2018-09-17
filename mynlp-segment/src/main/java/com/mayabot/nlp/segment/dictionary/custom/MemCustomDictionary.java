package com.mayabot.nlp.segment.dictionary.custom;

import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import com.mayabot.nlp.segment.dictionary.CustomDictionary;
import com.mayabot.nlp.segment.dictionary.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;

import java.util.TreeMap;

/**
 * 内存版本CustomDictionary
 *
 * @author jimichan
 */
public class MemCustomDictionary implements CustomDictionary {

    private TreeMap<String, NatureAttribute> dict;

    private DoubleArrayTrie<NatureAttribute> trie;

    public MemCustomDictionary(TreeMap<String, NatureAttribute> dict) {
        this.dict = dict;
        rebuild();
    }

    public MemCustomDictionary() {
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

    public void addWord(String word, Nature nature) {
        dict.put(word, NatureAttribute.create(nature, 1000));
    }

    public void removeWord(String word) {
        dict.remove(word);
    }

    @Override
    public DoubleArrayTrie<NatureAttribute> getTrie() {
        return trie;
    }

}

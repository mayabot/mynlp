/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

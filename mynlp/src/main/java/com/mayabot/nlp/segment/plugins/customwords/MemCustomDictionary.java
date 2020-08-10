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
package com.mayabot.nlp.segment.plugins.customwords;

import com.mayabot.nlp.algorithm.collection.dat.DoubleArrayTrieStringIntMap;

import java.util.TreeMap;

/**
 * 内存版本CustomDictionary
 *
 * @author jimichan
 */
public class MemCustomDictionary implements CustomDictionary {

    private TreeMap<String, Integer> dict;

    private DoubleArrayTrieStringIntMap trie;

    public MemCustomDictionary() {
        this.dict = new TreeMap<>();
        rebuild();
    }

    public MemCustomDictionary(TreeMap<String, Integer> dict) {
        this.dict = dict;
        rebuild();
    }

    public void rebuild() {
        if (dict.isEmpty()) {
            trie = null;
            return;
        }
        trie = new DoubleArrayTrieStringIntMap(dict);
    }

    public void addWord(String word) {
        dict.put(word, 1000);
    }

    public void removeWord(String word) {
        dict.remove(word);
    }

    @Override
    public DoubleArrayTrieStringIntMap getTrie() {
        return trie;
    }

}

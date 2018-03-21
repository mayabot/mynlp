/*
 * Copyright 2012 Takao Nakaguchi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trie4j;

import java.util.Map;

/**
 * Represents the Trie that can keep values binded to the key.
 *
 * @param <T> type of the value
 * @author Takao Nakaguchi
 */
public interface MapTrie<T> extends Trie {
    /**
     * retuns root node.
     *
     * @return root node.
     */
    MapNode<T> getRoot();

    /**
     * gets the value associated with word.
     *
     * @param word word
     * @return value. null if word not inserted or inserted with null value.
     */
    T get(String word);

    /**
     * TODO 暂时这么做，后面改掉。全部加上char[]的方法。
     *
     * @param word
     * @return
     */
    default T get(char[] word){
        return get(new String(word));
    }

    /**
     * insert a word with value.
     *
     * @param word  word to insert.
     * @param value the value associated with word.
     * @return old value for word.
     */
    T insert(String word, T value);

    /**
     * Put a value associated with existing word.
     *
     * @param word  word to be associated with value. word must be exist in trie.
     * @param value value
     * @return old value for word.
     */
    T put(String word, T value);

    /**
     * search trie for words contained in query.
     * If query is "helloworld" and trie contains "he", "hello" and "world",
     * the words "he" and "hello" will be found.
     *
     * @param query query
     * @return Iterable object which iterates found words.
     */
    Iterable<Map.Entry<String, T>> commonPrefixSearchEntries(String query);

    /**
     * search trie for words starting prefix.
     * If prefix is "he" and trie contains "he", "hello" and "world",
     * the words "he" and "hello" will be found.
     *
     * @param prefix prefix
     * @return Iterable object which iterates found words.
     */
    Iterable<Map.Entry<String, T>> predictiveSearchEntries(String prefix);
}

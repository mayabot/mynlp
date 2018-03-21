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

import java.io.IOException;
import java.io.Writer;

public interface Trie {
    /**
     * returns node size;
     *
     * @return node size;
     */
    int nodeSize();

    /**
     * retuns root node.
     *
     * @return root node.
     */
    Node getRoot();

    /**
     * returns inserted word count(equals to terminal node count)
     *
     * @return inserted word count
     */
    int size();

    /**
     * returns true if trie contains word.
     *
     * @param word word to check it contained.
     * @return true if trie contains word.
     */
    boolean contains(String word);

    /**
     * search trie for word contained in chars. If the word is found, this method
     * returns the position in chars and add found word to word parameter.
     *
     * @param chars chars
     * @param start start position
     * @param end   end position
     * @param word  buffer to append found word. this can be null
     * @return found position. -1 if no word found.
     * @deprecated replaced by findShortestWord {@link #findShortestWord(CharSequence, int, int, StringBuilder)}
     */
    @Deprecated
    int findWord(CharSequence chars, int start, int end, StringBuilder word);

    /**
     * search trie for shortest word contained in chars. If the word is found, this method
     * returns the position in chars and add found word to word parameter.
     *
     * @param chars chars
     * @param start start position
     * @param end   end position
     * @param word  buffer to append found word. this can be null
     * @return found position. -1 if no word found.
     */
    int findShortestWord(CharSequence chars, int start, int end, StringBuilder word);

    /**
     * search trie for longest word contained in chars. If the word is found, this method
     * returns the position in chars and add found word to word parameter.
     *
     * @param chars chars
     * @param start start position
     * @param end   end position
     * @param word  buffer to append found word. this can be null
     * @return found position. -1 if no word found.
     */
    int findLongestWord(CharSequence chars, int start, int end, StringBuilder word);

    /**
     * search trie for words contained in query.
     * If query is "helloworld" and trie contains "he", "hello" and "world",
     * the words "he" and "hello" will be found.
     *
     * @param query query
     * @return Iterable object which iterates found words.
     */
    Iterable<String> commonPrefixSearch(String query);

    /**
     * search trie for words starting prefix.
     * If prefix is "he" and trie contains "he", "hello" and "world",
     * the words "he" and "hello" will be found.
     *
     * @param prefix prefix
     * @return Iterable object which iterates found words.
     */
    Iterable<String> predictiveSearch(String prefix);

    /**
     * insert word.
     *
     * @param word word to insert.
     */
    void insert(String word);

    /**
     * dump trie to Writer.
     *
     * @param writer writer
     */
    void dump(Writer writer) throws IOException;

    /**
     * shrink buffer size to fit actual node count.
     */
    void trimToSize();

    /**
     * freeze trie and drop objects allocated for insert operation.
     * trie goes immutable.
     */
    void freeze();
}

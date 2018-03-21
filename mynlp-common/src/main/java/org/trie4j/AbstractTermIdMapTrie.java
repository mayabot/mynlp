/*
 * Copyright 2014 Takao Nakaguchi
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

import org.trie4j.util.Pair;

import java.io.*;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

public abstract class AbstractTermIdMapTrie<T>
        implements Externalizable, MapTrie<T> {
    protected AbstractTermIdMapTrie() {
    }

    protected AbstractTermIdMapTrie(TermIdTrie trie) {
        this.trie = trie;
    }

    @Override
    public int nodeSize() {
        return trie.nodeSize();
    }

    @Override
    public boolean contains(String word) {
        return trie.contains(word);
    }

    @Override
    public Iterable<String> commonPrefixSearch(String query) {
        return trie.commonPrefixSearch(query);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int findWord(CharSequence chars, int start, int end,
                        StringBuilder word) {
        return trie.findWord(chars, start, end, word);
    }

    @Override
    public int findShortestWord(CharSequence chars, int start, int end,
                                StringBuilder word) {
        return trie.findShortestWord(chars, start, end, word);
    }

    @Override
    public int findLongestWord(CharSequence chars, int start, int end,
                               StringBuilder word) {
        return trie.findLongestWord(chars, start, end, word);
    }

    @Override
    public Iterable<String> predictiveSearch(String prefix) {
        return trie.predictiveSearch(prefix);
    }

    @Override
    public void insert(String word) {
        trie.insert(word);
    }

    @Override
    public int size() {
        return trie.size();
    }

    @Override
    public void trimToSize() {
        trie.trimToSize();
    }

    @Override
    public void dump(Writer writer) throws IOException {
        trie.dump(writer);
    }

    @Override
    public void freeze() {
        trie.freeze();
    }

    public class MapNodeAdapter implements MapNode<T> {
        public MapNodeAdapter(TermIdNode orig) {
            this.orig = orig;
        }

        @Override
        public char[] getLetters() {
            return orig.getLetters();
        }

        @Override
        public boolean isTerminate() {
            return orig.isTerminate();
        }

        @Override
        public MapNode<T> getChild(char c) {
            return new MapNodeAdapter(orig.getChild(c));
        }

        @Override
        @SuppressWarnings("unchecked")
        public MapNode<T>[] getChildren() {
            TermIdNode[] origArray = orig.getChildren();
            MapNode<T>[] ret = new MapNode[origArray.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = new MapNodeAdapter(origArray[i]);
            }
            return ret;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T getValue() {
            return (T) values[orig.getTermId()];
        }

        @Override
        public void setValue(T value) {
            values[orig.getTermId()] = value;
        }

        private TermIdNode orig;
    }

    @Override
    public MapNode<T> getRoot() {
        return new MapNodeAdapter(trie.getRoot());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(String text) {
        int id = trie.getTermId(text);
        if (id < 0) return null;
        return (T) values[id];
    }

    @Override
    @SuppressWarnings("unchecked")
    public T put(String text, T value) {
        int id = trie.getTermId(text);
        if (id < 0) {
            throw new NoSuchElementException();
        }
        T ret = (T) values[id];
        values[id] = value;
        return ret;
    }

    @Override
    public T insert(String word, T value) {
        throw new UnsupportedOperationException();
    }

    private class IterableAdapter extends org.trie4j.util.IterableAdapter<Pair<String, Integer>, Entry<String, T>> {
        public IterableAdapter(Iterable<Pair<String, Integer>> iterable) {
            super(iterable);
        }

        @Override
        protected Entry<String, T> convert(final Pair<String, Integer> value) {
            return new Entry<String, T>() {
                @Override
                public String getKey() {
                    return value.getFirst();
                }

                @Override
                @SuppressWarnings("unchecked")
                public T getValue() {
                    return (T) values[value.getSecond()];
                }

                @Override
                public T setValue(T v) {
                    T ret = getValue();
                    values[value.getSecond()] = v;
                    return ret;
                }
            };
        }
    }

    @Override
    public Iterable<Entry<String, T>> commonPrefixSearchEntries(final String query) {
        return new IterableAdapter(trie.commonPrefixSearchWithTermId(query));
    }

    @Override
    public Iterable<Entry<String, T>> predictiveSearchEntries(String prefix) {
        return new IterableAdapter(trie.predictiveSearchWithTermId(prefix));
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        trie = (TermIdTrie) in.readObject();
        values = (Object[]) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(trie);
        out.writeObject(values);
    }

    public TermIdTrie getTrie() {
        return trie;
    }

    protected void setTrie(TermIdTrie trie) {
        this.trie = trie;
    }

    public Object[] getValues() {
        return values;
    }

    protected void setValues(Object[] values) {
        this.values = values;
    }

    private TermIdTrie trie;
    private Object[] values = {};
}

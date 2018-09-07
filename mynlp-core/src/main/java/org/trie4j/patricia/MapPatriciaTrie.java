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
package org.trie4j.patricia;

import org.trie4j.MapTrie;
import org.trie4j.util.Pair;

import java.io.Serializable;
import java.util.Map;

public class MapPatriciaTrie<T>
        extends PatriciaTrie
        implements Serializable, MapTrie<T> {
    @Override
    @SuppressWarnings("unchecked")
    public MapPatriciaTrieNode<T> getRoot() {
        return (MapPatriciaTrieNode<T>) super.getRoot();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T insert(String text, T value) {
        MapPatriciaTrieNode<T> node = (MapPatriciaTrieNode<T>) insert(getRoot(), text, 0);
        T ret = node.getValue();
        node.setValue(value);
        return ret;
    }

    @Override
    public T get(String word) {
        MapPatriciaTrieNode<T> node = getNode(word);
        if (node == null) return null;
        return node.getValue();
    }

    @Override
    public T put(String word, T value) {
        MapPatriciaTrieNode<T> node = getNode(word);
        if (node == null) return null;
        T ret = node.getValue();
        node.setValue(value);
        return ret;
    }

    @SuppressWarnings("unchecked")
    public MapPatriciaTrieNode<T> getNode(String text) {
        return (MapPatriciaTrieNode<T>) super.getNode(text);
    }

    @Override
    public Iterable<Map.Entry<String, T>> commonPrefixSearchEntries(String query) {
        return new IterableAdapter(commonPrefixSearchWithNode(query));
    }

    @Override
    public Iterable<Map.Entry<String, T>> predictiveSearchEntries(String prefix) {
        return new IterableAdapter(predictiveSearchWithNode(prefix));
    }

    @Override
    protected MapPatriciaTrieNode<T> newNode() {
        return new MapPatriciaTrieNode<T>();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected PatriciaTrieNode newNode(char[] letters, PatriciaTrieNode source) {
        return new MapPatriciaTrieNode<T>(letters, source.isTerminate(),
                (MapPatriciaTrieNode<T>[]) source.getChildren(), ((MapPatriciaTrieNode<T>) source).getValue());
    }

    @Override
    protected MapPatriciaTrieNode<T> newNode(char[] letters, boolean terminated) {
        return new MapPatriciaTrieNode<T>(letters, terminated);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected MapPatriciaTrieNode<T>[] newNodeArray(PatriciaTrieNode... nodes) {
        MapPatriciaTrieNode<T>[] ret = new MapPatriciaTrieNode[nodes.length];
        System.arraycopy(nodes, 0, ret, 0, nodes.length);
        return ret;
    }

    private class Entry implements Map.Entry<String, T> {
        public Entry(String key, MapPatriciaTrieNode<T> node) {
            this.key = key;
            this.node = node;
        }

        @Override
        public String getKey() {
            return key;
        }

        public T getValue() {
            return node.getValue();
        }

        @Override
        public T setValue(T value) {
            T ret = node.getValue();
            node.setValue(value);
            return ret;
        }

        private String key;
        private MapPatriciaTrieNode<T> node;
    }

    private class IterableAdapter extends org.trie4j.util.IterableAdapter<Pair<String, PatriciaTrieNode>, Map.Entry<String, T>> {
        public IterableAdapter(Iterable<Pair<String, PatriciaTrieNode>> orig) {
            super(orig);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Map.Entry<String, T> convert(Pair<String, PatriciaTrieNode> value) {
            return new Entry(value.getFirst(), (MapPatriciaTrieNode<T>) value.getSecond());
        }
    }

    private static final long serialVersionUID = 2165079531157534766L;
}

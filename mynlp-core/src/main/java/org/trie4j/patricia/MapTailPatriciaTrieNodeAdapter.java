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
package org.trie4j.patricia;

import org.trie4j.MapNode;

public class MapTailPatriciaTrieNodeAdapter<T> implements MapNode<T> {
    public MapTailPatriciaTrieNodeAdapter(MapTailPatriciaTrieNode<T> node, CharSequence tails) {
        this.node = node;
        this.tails = tails;
    }

    public MapTailPatriciaTrieNode<T> getNode() {
        return node;
    }

    @Override
    public MapNode<T> getChild(char c) {
        MapTailPatriciaTrieNode<T> n = node.getChild(c);
        if (n == null) return null;
        else return new MapTailPatriciaTrieNodeAdapter<T>(n, tails);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapNode<T>[] getChildren() {
        TailPatriciaTrieNode[] children = node.getChildren();
        if (children == null) return null;
        MapNode<T>[] ret = new MapNode[children.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new MapTailPatriciaTrieNodeAdapter<T>(node.getChildren()[i], tails);
        }
        return ret;
    }

    @Override
    public char[] getLetters() {
        return node.getLetters(tails);
    }

    @Override
    public boolean isTerminate() {
        return node.isTerminate();
    }

    @Override
    public T getValue() {
        return node.getValue();
    }

    public void setValue(T value) {
        node.setValue(value);
    }

    private MapTailPatriciaTrieNode<T> node;
    private CharSequence tails;
}

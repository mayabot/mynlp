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

public class TailPatriciaTrieNodeAdapter implements org.trie4j.Node {
    public TailPatriciaTrieNodeAdapter(TailPatriciaTrieNode node, CharSequence tails) {
        this.node = node;
        this.tails = tails;
    }

    public TailPatriciaTrieNode getNode() {
        return node;
    }

    @Override
    public org.trie4j.Node getChild(char c) {
        TailPatriciaTrieNode n = node.getChild(c);
        if (n == null) return null;
        else return new TailPatriciaTrieNodeAdapter(n, tails);
    }

    @Override
    public org.trie4j.Node[] getChildren() {
        TailPatriciaTrieNode[] children = node.getChildren();
        if (children == null) return null;
        org.trie4j.Node[] ret = new org.trie4j.Node[children.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new TailPatriciaTrieNodeAdapter(node.getChildren()[i], tails);
        }
        return ret;
    }

    public char getFirstLetter() {
        return node.getFirstLetter();
    }

    @Override
    public char[] getLetters() {
        return node.getLetters(tails);
    }

    @Override
    public boolean isTerminate() {
        return node.isTerminate();
    }

    private TailPatriciaTrieNode node;
    private CharSequence tails;
}

/*
 * Copyright (C) 2012 Takao Nakaguchi
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

import org.trie4j.NodeVisitor;

import java.io.Serializable;

public class PatriciaTrieNode
        implements Serializable, org.trie4j.Node {
    public PatriciaTrieNode() {
        this(new char[]{}, false);
    }

    public PatriciaTrieNode(char[] letters, boolean terminated) {
        this(letters, terminated, emptyChildren);
    }

    public PatriciaTrieNode(char[] letters, boolean terminated, PatriciaTrieNode[] children) {
        this.letters = letters;
        this.terminate = terminated;
        this.children = children;
    }

    public PatriciaTrieNode[] getChildren() {
        return children;
    }

    public void setChildren(PatriciaTrieNode[] children) {
        this.children = children;
    }

    public char[] getLetters() {
        return letters;
    }

    public void setLetters(char[] letters) {
        this.letters = letters;
    }

    public boolean isTerminate() {
        return terminate;
    }

    public void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

    public PatriciaTrieNode getChild(char c) {
        int end = children.length;
        if (end > 16) {
            int start = 0;
            while (start < end) {
                int i = (start + end) / 2;
                PatriciaTrieNode n = children[i];
                int d = c - n.letters[0];
                if (d == 0) return n;
                if (d < 0) {
                    end = i;
                } else if (start == i) {
                    break;
                } else {
                    start = i;
                }
            }
        } else {
            for (int i = 0; i < end; i++) {
                PatriciaTrieNode n = children[i];
                if (n.letters[0] == c) return n;
            }
        }
        return null;
    }

    public void visit(NodeVisitor visitor, int nest) {
        if (!visitor.visit(this, nest)) return;
        nest++;
        for (PatriciaTrieNode n : children) {
            n.visit(visitor, nest);
        }
    }

    public PatriciaTrieNode addChild(int index, PatriciaTrieNode n) {
        PatriciaTrieNode[] newc = new PatriciaTrieNode[children.length + 1];
        System.arraycopy(children, 0, newc, 0, index);
        newc[index] = n;
        System.arraycopy(children, index, newc, index + 1, children.length - index);
        this.children = newc;
        return this;
    }

    private char[] letters;
    private boolean terminate;
    private PatriciaTrieNode[] children;
    private static PatriciaTrieNode[] emptyChildren = {};
    private static final long serialVersionUID = -1625115329685564809L;
}

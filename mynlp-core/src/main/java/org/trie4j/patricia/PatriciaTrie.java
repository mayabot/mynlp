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

import org.trie4j.AbstractTrie;
import org.trie4j.NodeVisitor;
import org.trie4j.Trie;
import org.trie4j.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 压缩前缀树，是一种更节省空间的Trie（前缀树)
 */
public class PatriciaTrie
        extends AbstractTrie
        implements Serializable, Trie {
    public PatriciaTrie() {
    }

    public PatriciaTrie(String... words) {
        for (String s : words) insert(s);
    }

    @Override
    public int nodeSize() {
        return nodeSize;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(String text) {
        PatriciaTrieNode node = root;
        int n = text.length();
        for (int i = 0; i < n; i++) {
            node = node.getChild(text.charAt(i));
            if (node == null) return false;
            char[] letters = node.getLetters();
            int lettersLen = letters.length;
            for (int j = 1; j < lettersLen; j++) {
                i++;
                if (i == n) return false;
                if (text.charAt(i) != letters[j]) return false;
            }
        }
        return node.isTerminate();
    }

    public PatriciaTrieNode getNode(String text) {
        PatriciaTrieNode node = root;
        int n = text.length();
        for (int i = 0; i < n; i++) {
            node = node.getChild(text.charAt(i));
            if (node == null) return null;
            char[] letters = node.getLetters();
            int lettersLen = letters.length;
            for (int j = 1; j < lettersLen; j++) {
                i++;
                if (i == n) return null;
                if (text.charAt(i) != letters[j]) return null;
            }
        }
        if (node.isTerminate()) {
            return node;
        } else {
            return null;
        }
    }

    @Override
    public Iterable<String> commonPrefixSearch(String query) {
        List<String> ret = new ArrayList<String>();
        char[] queryChars = query.toCharArray();
        int cur = 0;
        PatriciaTrieNode node = root;
        while (node != null) {
            char[] letters = node.getLetters();
            if (letters.length > (queryChars.length - cur)) return ret;
            for (int i = 0; i < letters.length; i++) {
                if (letters[i] != queryChars[cur + i]) return ret;
            }
            if (node.isTerminate()) {
                ret.add(new String(queryChars, 0, cur + letters.length));
            }
            cur += letters.length;
            if (queryChars.length == cur) return ret;
            node = node.getChild(queryChars[cur]);
        }
        return ret;
    }

    public Iterable<Pair<String, PatriciaTrieNode>> commonPrefixSearchWithNode(String query) {
        List<Pair<String, PatriciaTrieNode>> ret = new ArrayList<Pair<String, PatriciaTrieNode>>();
        char[] queryChars = query.toCharArray();
        int cur = 0;
        PatriciaTrieNode node = root;
        while (node != null) {
            char[] letters = node.getLetters();
            if (letters.length > (queryChars.length - cur)) return ret;
            for (int i = 0; i < letters.length; i++) {
                if (letters[i] != queryChars[cur + i]) return ret;
            }
            if (node.isTerminate()) {
                ret.add(Pair.create(
                        new String(queryChars, 0, cur + letters.length),
                        node));
            }
            cur += letters.length;
            if (queryChars.length == cur) return ret;
            node = node.getChild(queryChars[cur]);
        }
        return ret;
    }

    @Override
    public Iterable<String> predictiveSearch(String prefix) {
        char[] queryChars = prefix.toCharArray();
        int cur = 0;
        PatriciaTrieNode node = root;
        while (node != null) {
            char[] letters = node.getLetters();
            int n = Math.min(letters.length, queryChars.length - cur);
            for (int i = 0; i < n; i++) {
                if (letters[i] != queryChars[cur + i]) {
                    return Collections.emptyList();
                }
            }
            cur += n;
            if (queryChars.length == cur) {
                List<String> ret = new ArrayList<String>();
                int rest = letters.length - n;
                if (rest > 0) {
                    prefix += new String(letters, n, rest);
                }
                if (node.isTerminate()) ret.add(prefix);
                enumLetters(node, prefix, ret);
                return ret;
            }
            node = node.getChild(queryChars[cur]);
        }
        return Collections.emptyList();
    }

    public Iterable<Pair<String, PatriciaTrieNode>> predictiveSearchWithNode(String prefix) {
        char[] queryChars = prefix.toCharArray();
        int cur = 0;
        PatriciaTrieNode node = root;
        while (node != null) {
            char[] letters = node.getLetters();
            int n = Math.min(letters.length, queryChars.length - cur);
            for (int i = 0; i < n; i++) {
                if (letters[i] != queryChars[cur + i]) {
                    return Collections.emptyList();
                }
            }
            cur += n;
            if (queryChars.length == cur) {
                List<Pair<String, PatriciaTrieNode>> ret = new ArrayList<Pair<String, PatriciaTrieNode>>();
                int rest = letters.length - n;
                if (rest > 0) {
                    prefix += new String(letters, n, rest);
                }
                if (node.isTerminate()) ret.add(Pair.create(prefix, node));
                enumLettersWithNode(node, prefix, ret);
                return ret;
            }
            node = node.getChild(queryChars[cur]);
        }
        return Collections.emptyList();
    }

    public void insert(String text) {
        insert(root, text, 0);
    }

    protected PatriciaTrieNode insert(PatriciaTrieNode node, String letters, int offset) {
        int lettersRest = letters.length() - offset;
        while (true) {
            int thisLettersLength = node.getLetters().length;
            int n = Math.min(lettersRest, thisLettersLength);
            int i = 0;
            while (i < n && (letters.charAt(i + offset) - node.getLetters()[i]) == 0) i++;
            if (i != n) {
                PatriciaTrieNode child1 = newNode(
                        Arrays.copyOfRange(node.getLetters(), i, node.getLetters().length)
                        , node);
                PatriciaTrieNode child2 = newNode(
                        letters.substring(i + offset).toCharArray()
                        , true);
                node.setLetters(Arrays.copyOfRange(node.getLetters(), 0, i));
                node.setTerminate(false);
                node.setChildren(
                        (child1.getLetters()[0] < child2.getLetters()[0]) ?
                                newNodeArray(child1, child2) : newNodeArray(child2, child1));
                size++;
                nodeSize += 2;
                return child2;
            } else if (lettersRest == thisLettersLength) {
                if (!node.isTerminate()) {
                    node.setTerminate(true);
                    size++;
                }
                return node;
            } else if (lettersRest < thisLettersLength) {
                PatriciaTrieNode newChild = newNode(
                        Arrays.copyOfRange(node.getLetters(), lettersRest, thisLettersLength)
                        , node);
                node.setLetters(Arrays.copyOfRange(node.getLetters(), 0, i));
                node.setTerminate(true);
                node.setChildren(newNodeArray(newChild));
                size++;
                nodeSize++;
                return node;
            } else {
                int index = 0;
                int end = node.getChildren().length;
                boolean cont = false;
                if (end > 16) {
                    int start = 0;
                    while (start < end) {
                        index = (start + end) / 2;
                        PatriciaTrieNode child = node.getChildren()[index];
                        int c = letters.charAt(i + offset) - child.getLetters()[0];
                        if (c == 0) {
                            node = child;
                            offset += i;
                            lettersRest -= i;
                            cont = true;
                            break;
                        }
                        if (c < 0) {
                            end = index;
                        } else if (start == index) {
                            index = end;
                            break;
                        } else {
                            start = index;
                        }
                    }
                } else {
                    for (; index < end; index++) {
                        PatriciaTrieNode child = node.getChildren()[index];
                        int c = letters.charAt(i + offset) - child.getLetters()[0];
                        if (c < 0) break;
                        if (c == 0) {
                            node = child;
                            offset += i;
                            lettersRest -= i;
                            cont = true;
                            break;
                        }
                    }
                }
                if (cont) continue;
                PatriciaTrieNode child = newNode(letters.substring(i + offset).toCharArray(), true);
                node.addChild(index, child);
                size++;
                nodeSize++;
                return child;
            }
        }
    }

    public void visit(NodeVisitor visitor) {
        root.visit(visitor, 0);
    }

    public PatriciaTrieNode getRoot() {
        return root;
    }

    protected PatriciaTrieNode newNode() {
        return new PatriciaTrieNode();
    }

    protected PatriciaTrieNode newNode(char[] letters, PatriciaTrieNode source) {
        return new PatriciaTrieNode(letters, source.isTerminate(), source.getChildren());
    }

    protected PatriciaTrieNode newNode(char[] letters, boolean terminated) {
        return new PatriciaTrieNode(letters, terminated);
    }

    protected PatriciaTrieNode[] newNodeArray(PatriciaTrieNode... nodes) {
        return nodes;
    }

    private static void enumLetters(PatriciaTrieNode node, String prefix, List<String> letters) {
        for (PatriciaTrieNode child : node.getChildren()) {
            String text = prefix + new String(child.getLetters());
            if (child.isTerminate()) letters.add(text);
            enumLetters(child, text, letters);
        }
    }

    private static void enumLettersWithNode(PatriciaTrieNode node, String prefix, List<Pair<String, PatriciaTrieNode>> letters) {
        for (PatriciaTrieNode child : node.getChildren()) {
            String text = prefix + new String(child.getLetters());
            if (child.isTerminate()) letters.add(Pair.create(text, child));
            enumLettersWithNode(child, text, letters);
        }
    }

    private int size;
    private int nodeSize;
    private PatriciaTrieNode root = newNode();
    private static final long serialVersionUID = -7611399538600722195L;
}

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
import org.trie4j.Node;
import org.trie4j.Trie;
import org.trie4j.tail.FastTailCharIterator;
import org.trie4j.tail.TailCharIterator;
import org.trie4j.tail.builder.SuffixTrieTailBuilder;
import org.trie4j.tail.builder.TailBuilder;
import org.trie4j.util.Pair;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class TailPatriciaTrie
        extends AbstractTrie
        implements Serializable, Trie {
    public TailPatriciaTrie() {
        this(new SuffixTrieTailBuilder());
    }

    public TailPatriciaTrie(TailBuilder builder) {
        this.tailBuilder = builder;
        this.tails = builder.getTails();
    }

    public TailPatriciaTrie(Trie orig, TailBuilder builder) {
        this.tailBuilder = builder;
        this.tails = builder.getTails();
        this.root = cloneNode(orig.getRoot());
        this.size = orig.size();
        this.nodeSize = orig.nodeSize();
        trimToSize();
    }

    private TailPatriciaTrieNode cloneNode(Node node) {
        char[] letters = node.getLetters();
        char fc = letters.length == 0 ? (char) 0xffff : letters[0];
        int ti = letters.length < 2 ? -1 : tailBuilder.insert(letters, 1, letters.length - 1);
        Node[] orgChildren = node.getChildren();
        TailPatriciaTrieNode[] children = newNodeArray(orgChildren.length);
        for (int i = 0; i < children.length; i++) {
            children[i] = cloneNode(orgChildren[i]);
        }
        return new TailPatriciaTrieNode(fc, ti, node.isTerminate(), children);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int nodeSize() {
        return nodeSize;
    }

    @Override
    public Node getRoot() {
        return new TailPatriciaTrieNodeAdapter(root, tails);
    }

    @Override
    public boolean contains(String text) {
        TailPatriciaTrieNode node = root;
        FastTailCharIterator it = new FastTailCharIterator(tails, -1);
        int n = text.length();
        for (int i = 0; i < n; i++) {
            node = node.getChild(text.charAt(i));
            if (node == null) return false;
            int ti = node.getTailIndex();
            if (ti == -1) continue;
            it.setIndex(node.getTailIndex());
            char c;
            while ((c = it.getNext()) != '\0') {
                i++;
                if (i == n) return false;
                if (text.charAt(i) != c) return false;
            }
        }
        return node.isTerminate();
    }

    public TailPatriciaTrieNode getNode(String text) {
        TailPatriciaTrieNode node = root;
        FastTailCharIterator it = new FastTailCharIterator(tails, -1);
        int n = text.length();
        for (int i = 0; i < n; i++) {
            node = node.getChild(text.charAt(i));
            if (node == null) return null;
            int ti = node.getTailIndex();
            if (ti == -1) continue;
            it.setIndex(node.getTailIndex());
            char c;
            while ((c = it.getNext()) != '\0') {
                i++;
                if (i == n) return null;
                if (text.charAt(i) != c) return null;
            }
        }
        return node;
    }

    public CharSequence getTails() {
        return tails;
    }

    @Override
    public int findShortestWord(CharSequence chars, int start, int end, StringBuilder word) {
        TailCharIterator it = new TailCharIterator(tails, -1);
        for (int i = start; i < end; i++) {
            TailPatriciaTrieNode node = root;
            for (int j = i; j < end; j++) {
                node = node.getChild(chars.charAt(j));
                if (node == null) break;
                boolean matched = true;
                it.setIndex(node.getTailIndex());
                while (it.hasNext()) {
                    j++;
                    if (j == end || chars.charAt(j) != it.next()) {
                        matched = false;
                        break;
                    }
                }
                if (matched) {
                    if (node.isTerminate()) {
                        if (word != null) word.append(chars, i, j + 1);
                        return i;
                    }
                } else {
                    break;
                }
            }
        }
        return -1;
    }

    @Override
    public int findLongestWord(CharSequence chars, int start, int end, StringBuilder word) {
        TailCharIterator it = new TailCharIterator(tails, -1);
        for (int i = start; i < end; i++) {
            TailPatriciaTrieNode node = root;
            int lastJ = -1;
            for (int j = i; j < end; j++) {
                node = node.getChild(chars.charAt(j));
                if (node == null) break;
                boolean matched = true;
                it.setIndex(node.getTailIndex());
                while (it.hasNext()) {
                    j++;
                    if (j == end || chars.charAt(j) != it.next()) {
                        matched = false;
                        break;
                    }
                }
                if (matched) {
                    if (node.isTerminate()) {
                        lastJ = j;
                    }
                } else {
                    break;
                }
            }
            if (lastJ != -1) {
                if (word != null) word.append(chars, i, lastJ + 1);
                return i;
            }
        }
        return -1;
    }

    @Override
    public Iterable<String> commonPrefixSearch(final String query) {
        if (query.length() == 0) return new ArrayList<String>(0);
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    private int cur;
                    private StringBuilder currentChars = new StringBuilder();
                    private TailPatriciaTrieNode current = root;
                    private String next;

                    {
                        cur = 0;
                        findNext();
                    }

                    private void findNext() {
                        next = null;
                        while (next == null) {
                            if (query.length() <= cur) return;
                            TailPatriciaTrieNode child = current.getChild(query.charAt(cur));
                            if (child == null) return;
                            int rest = query.length() - cur;
                            char[] letters = child.getLetters(tails);
                            int len = letters.length;
                            if (rest < len) return;
                            for (int i = 1; i < len; i++) {
                                int c = letters[i] - query.charAt(cur + i);
                                if (c != 0) return;
                            }

                            String b = query.substring(cur, cur + len);
                            if (child.isTerminate()) {
                                next = currentChars + b;
                            }
                            cur += len;
                            currentChars.append(b);
                            current = child;
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        return next != null;
                    }

                    @Override
                    public String next() {
                        String ret = next;
                        if (ret == null) {
                            throw new NoSuchElementException();
                        }
                        findNext();
                        return ret;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public Iterable<Pair<String, TailPatriciaTrieNode>> commonPrefixSearchWithNode(final String query) {
        if (query.length() == 0) return new ArrayList<Pair<String, TailPatriciaTrieNode>>(0);
        return new Iterable<Pair<String, TailPatriciaTrieNode>>() {
            @Override
            public Iterator<Pair<String, TailPatriciaTrieNode>> iterator() {
                return new Iterator<Pair<String, TailPatriciaTrieNode>>() {
                    private int cur;
                    private StringBuilder currentChars = new StringBuilder();
                    private TailPatriciaTrieNode current = root;
                    private Pair<String, TailPatriciaTrieNode> next;

                    {
                        cur = 0;
                        findNext();
                    }

                    private void findNext() {
                        next = null;
                        while (next == null) {
                            if (query.length() <= cur) return;
                            TailPatriciaTrieNode child = current.getChild(query.charAt(cur));
                            if (child == null) return;
                            int rest = query.length() - cur;
                            char[] letters = child.getLetters(tails);
                            int len = letters.length;
                            if (rest < len) return;
                            for (int i = 1; i < len; i++) {
                                int c = letters[i] - query.charAt(cur + i);
                                if (c != 0) return;
                            }

                            String b = query.substring(cur, cur + len);
                            cur += len;
                            currentChars.append(b);
                            if (child.isTerminate()) {
                                next = Pair.create(currentChars.toString(), child);
                            }
                            current = child;
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        return next != null;
                    }

                    @Override
                    public Pair<String, TailPatriciaTrieNode> next() {
                        Pair<String, TailPatriciaTrieNode> ret = next;
                        if (ret == null) {
                            throw new NoSuchElementException();
                        }
                        findNext();
                        return ret;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    @Override
    public Iterable<String> predictiveSearch(String prefix) {
        char[] queryChars = prefix.toCharArray();
        int cur = 0;
        TailPatriciaTrieNode node = root;
        while (node != null) {
            char[] letters = node.getLetters(tails);
            int n = Math.min(letters.length, queryChars.length - cur);
            for (int i = 0; i < n; i++) {
                if (letters[i] != queryChars[cur + i]) {
                    return Collections.emptyList();
                }
            }
            cur += n;
            if (queryChars.length == cur) {
                List<String> ret = new ArrayList<String>();
                prefix += new String(letters, n, letters.length - n);
                if (node.isTerminate()) ret.add(prefix);
                enumLetters(node, prefix, ret);
                return ret;
            }
            node = node.getChild(queryChars[cur]);
        }
        return Collections.emptyList();
    }

    public Iterable<Pair<String, TailPatriciaTrieNode>> predictiveSearchWithNode(String prefix) {
        char[] queryChars = prefix.toCharArray();
        int cur = 0;
        TailPatriciaTrieNode node = root;
        while (node != null) {
            char[] letters = node.getLetters(tails);
            int n = Math.min(letters.length, queryChars.length - cur);
            for (int i = 0; i < n; i++) {
                if (letters[i] != queryChars[cur + i]) {
                    return Collections.emptyList();
                }
            }
            cur += n;
            if (queryChars.length == cur) {
                List<Pair<String, TailPatriciaTrieNode>> ret = new ArrayList<Pair<String, TailPatriciaTrieNode>>();
                prefix += new String(letters, n, letters.length - n);
                if (node.isTerminate()) ret.add(Pair.create(prefix, node));
                enumLettersWithNode(node, prefix, ret);
                return ret;
            }
            node = node.getChild(queryChars[cur]);
        }
        return Collections.emptyList();
    }

    private void enumLetters(TailPatriciaTrieNode node, String prefix, List<String> letters) {
        TailPatriciaTrieNode[] children = node.getChildren();
        if (children == null) return;
        for (TailPatriciaTrieNode child : children) {
            String text = prefix + new String(child.getLetters(tails));
            if (child.isTerminate()) letters.add(text);
            enumLetters(child, text, letters);
        }
    }

    private void enumLettersWithNode(TailPatriciaTrieNode node, String prefix, List<Pair<String, TailPatriciaTrieNode>> letters) {
        TailPatriciaTrieNode[] children = node.getChildren();
        if (children == null) return;
        for (TailPatriciaTrieNode child : children) {
            String text = prefix + new String(child.getLetters(tails));
            if (child.isTerminate()) letters.add(Pair.create(text, child));
            enumLettersWithNode(child, text, letters);
        }
    }

    @Override
    public void insert(String text) {
        if (tailBuilder == null) {
            throw new UnsupportedOperationException("insert isn't permitted for freezed trie");
        }
        insert(root, text, 0);
    }

    protected TailPatriciaTrieNode insert(TailPatriciaTrieNode node, String letters, int offset) {
        TailCharIterator it = new TailCharIterator(tails, node.getTailIndex());
        int count = 0;
        boolean matchComplete = true;
        int lettersLength = letters.length();
        while (it.hasNext() && offset < lettersLength) {
            if (letters.charAt(offset) != it.next()) {
                matchComplete = false;
                break;
            }
            offset++;
            count++;
        }
        if (offset == lettersLength) {
            if (it.hasNext()) {
                // n: abcde
                // l: abc
                char c = it.next();
                int idx = it.getNextIndex();
                if (!it.hasNext()) {
                    idx = -1;
                }
                TailPatriciaTrieNode newChild = newNode(c, idx, node);
                node.setTailIndex(
                        (count > 0) ? tailBuilder.insert(letters, offset - count, count)
                                : -1
                );
                node.setChildren(newNodeArray(newChild));
                node.setTerminate(true);
                size++;
                nodeSize++;
                return node;
            } else {
                // n: abc
                // l: abc
                if (!node.isTerminate()) {
                    node.setTerminate(true);
                    size++;
                }
                return node;
            }
        } else {
            if (!matchComplete) {
                // n: abcwz
                // l: abcde
                int firstOffset = offset - count;
                char n1Fc = it.current();
                int n1Idx = it.getNextIndex();
                if (!it.hasNext()) {
                    n1Idx = -1;
                }
                TailPatriciaTrieNode n1 = newNode(n1Fc, n1Idx, node);
                char n2Fc = letters.charAt(offset++);
                int n2Idx = (offset < lettersLength) ?
                        tailBuilder.insert(letters, offset, lettersLength - offset) :
                        -1;
                TailPatriciaTrieNode n2 = newNode(n2Fc, n2Idx, true);
                if (count > 0) {
                    node.setTailIndex(tailBuilder.insert(letters, firstOffset, count));
                } else {
                    node.setTailIndex(-1);
                }
                node.setTerminate(false);
                node.setChildren(
                        (n1.getFirstLetter() < n2.getFirstLetter()) ?
                                newNodeArray(n1, n2) : newNodeArray(n2, n1));
                size++;
                nodeSize += 2;
                return n2;
            } else {
                // n: abc
                // l: abcde
                char fc = letters.charAt(offset++);
                // find node
                Pair<TailPatriciaTrieNode, Integer> ret = node.findNode(fc);
                TailPatriciaTrieNode child = ret.getFirst();
                if (child != null) {
                    return insert(child, letters, offset);
                } else {
                    int idx = (offset < lettersLength) ?
                            tailBuilder.insert(letters, offset, lettersLength - offset) :
                            -1;
                    TailPatriciaTrieNode newNode = newNode(fc, idx, true);
                    node.addChild(ret.getSecond(), newNode);
                    size++;
                    nodeSize++;
                    return newNode;
                }
            }
        }
    }

    @Override
    public void trimToSize() {
        if (tails instanceof StringBuilder) {
            ((StringBuilder) tails).trimToSize();
        }
    }

    @Override
    public void freeze() {
        trimToSize();
        tailBuilder = null;
    }

    public TailBuilder getTailBuilder() {
        return tailBuilder;
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException {
        trimToSize();
        out.defaultWriteObject();
    }

    protected TailPatriciaTrieNode newNode() {
        return new TailPatriciaTrieNode((char) 0xffff, -1, false, newNodeArray());
    }

    protected TailPatriciaTrieNode newNode(char firstChar, int tailIndex, TailPatriciaTrieNode source) {
        return new TailPatriciaTrieNode(firstChar, tailIndex, source.isTerminate(), source.getChildren());
    }

    protected TailPatriciaTrieNode newNode(char firstChar, int tailIndex, boolean terminated) {
        return new TailPatriciaTrieNode(firstChar, tailIndex, terminated, newNodeArray());
    }

    protected TailPatriciaTrieNode[] newNodeArray(TailPatriciaTrieNode... nodes) {
        return nodes;
    }

    protected TailPatriciaTrieNode[] newNodeArray(int size) {
        return new TailPatriciaTrieNode[size];
    }

    private int size;
    private int nodeSize;
    private TailPatriciaTrieNode root = newNode();
    private TailBuilder tailBuilder;
    private CharSequence tails;
    private static final long serialVersionUID = -2084269385978925271L;
}

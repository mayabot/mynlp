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
package org.trie4j.louds;

import org.trie4j.AbstractTrie;
import org.trie4j.Node;
import org.trie4j.Trie;
import org.trie4j.bv.BitVector01Divider;
import org.trie4j.bv.BytesRank0OnlySuccinctBitVector;
import org.trie4j.bv.BytesSuccinctBitVector;
import org.trie4j.tail.ConcatTailArrayBuilder;
import org.trie4j.tail.TailArray;
import org.trie4j.tail.TailArrayBuilder;
import org.trie4j.tail.TailCharIterator;
import org.trie4j.util.Pair;

import java.io.*;
import java.util.*;

public class InlinedTailLOUDSPPTrie
        extends AbstractTrie
        implements Externalizable, Trie {
    public InlinedTailLOUDSPPTrie() {
        tailArray = newTailArrayBuilder(0).build();
        r0 = new BytesRank0OnlySuccinctBitVector();
        r1 = new BytesSuccinctBitVector();
    }

    public InlinedTailLOUDSPPTrie(Trie orig) {
        this(orig, new BytesRank0OnlySuccinctBitVector(orig.size() + 1), new BytesSuccinctBitVector(orig.size() + 1));
    }

    public InlinedTailLOUDSPPTrie(Trie orig, BytesRank0OnlySuccinctBitVector r0, BytesSuccinctBitVector r1) {
        this.r0 = r0;
        this.r1 = r1;
        size = orig.size();
        TailArrayBuilder tailArrayBuilder = newTailArrayBuilder(size);
        labels = new char[size];
        term = new BitSet(size);
        LinkedList<Node> queue = new LinkedList<Node>();
        int count = 0;
        BitVector01Divider d = new BitVector01Divider(r0, r1);
        if (orig.getRoot() != null) queue.add(orig.getRoot());
        while (!queue.isEmpty()) {
            Node node = queue.pollFirst();
            int index = count++;
            if (index >= labels.length) {
                extend();
            }
            if (node.isTerminate()) term.set(index);
            for (Node c : node.getChildren()) {
                d.append1();
                queue.offerLast(c);
            }
            d.append0();
            char[] letters = node.getLetters();
            if (letters.length == 0) {
                labels[index] = 0xffff;
                tailArrayBuilder.appendEmpty(index);
            } else {
                labels[index] = letters[0];
                if (letters.length >= 2) {
                    tailArrayBuilder.append(index, letters, 1, letters.length - 1);
                } else {
                    tailArrayBuilder.appendEmpty(index);
                }
            }
        }
        nodeSize = count;
        this.tailArray = tailArrayBuilder.build();
    }

    public BytesRank0OnlySuccinctBitVector getR0() {
        return r0;
    }

    public BytesSuccinctBitVector getR1() {
        return r1;
    }

    @Override
    public int nodeSize() {
        return nodeSize;
    }

    @Override
    public Node getRoot() {
        return new LOUDSNode(0);
    }

    @Override
    public void dump(Writer writer) throws IOException {
        super.dump(writer);
        String r0s = r0.toString();
        writer.write("r0: " + ((r0s.length() > 100) ? r0s.substring(0, 100) : r0s));
        String r1s = r1.toString();
        writer.write("\nr1: " + ((r1s.length() > 100) ? r1s.substring(0, 100) : r1s));
        writer.write("\nlabels: ");
        int count = 0;
        for (char c : labels) {
            writer.write(c);
            if (count++ == 99) break;
        }
        writer.write("\n");
    }

    @Override
    public boolean contains(String text) {
        int nodeId = 0; // root
        TailCharIterator it = tailArray.newIterator();
        int n = text.length();
        for (int i = 0; i < n; i++) {
            nodeId = getChildNode(nodeId, text.charAt(i));
            if (nodeId == -1) return false;
            it.setOffset(tailArray.getIteratorOffset(nodeId));
            while (it.hasNext()) {
                i++;
                if (i == n) return false;
                if (text.charAt(i) != it.next()) return false;
            }
        }
        return term.get(nodeId);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterable<String> commonPrefixSearch(String query) {
        List<String> ret = new ArrayList<String>();
        char[] chars = query.toCharArray();
        int charsLen = chars.length;
        int nodeId = 0; // root
        TailCharIterator tci = tailArray.newIterator();
        for (int charsIndex = 0; charsIndex < charsLen; charsIndex++) {
            int child = getChildNode(nodeId, chars[charsIndex]);
            if (child == -1) return ret;
            tci.setOffset(tailArray.getIteratorOffset(child));
            while (tci.hasNext()) {
                charsIndex++;
                if (charsLen <= charsIndex) return ret;
                if (chars[charsIndex] != tci.next()) return ret;
            }
            if (term.get(child)) {
                ret.add(new String(chars, 0, charsIndex + 1));
            }
            nodeId = child;
        }
        return ret;
    }

    @Override
    public Iterable<String> predictiveSearch(String query) {
        List<String> ret = new ArrayList<String>();
        char[] chars = query.toCharArray();
        int charsLen = chars.length;
        int nodeId = 0; // root
        TailCharIterator tci = tailArray.newIterator();
        String pfx = null;
        int charsIndexBack = 0;
        for (int charsIndex = 0; charsIndex < charsLen; charsIndex++) {
            charsIndexBack = charsIndex;
            int child = getChildNode(nodeId, chars[charsIndex]);
            if (child == -1) return ret;
            tci.setOffset(tailArray.getIteratorOffset(child));
            while (tci.hasNext()) {
                charsIndex++;
                if (charsIndex >= charsLen) break;
                if (chars[charsIndex] != tci.next()) return ret;
            }
            nodeId = child;
        }
        pfx = new String(chars, 0, charsIndexBack);

        Deque<Pair<Integer, String>> queue = new LinkedList<Pair<Integer, String>>();
        queue.offerLast(Pair.create(nodeId, pfx));
        while (queue.size() > 0) {
            Pair<Integer, String> element = queue.pollFirst();
            int nid = element.getFirst();

            StringBuilder b = new StringBuilder(element.getSecond());
            b.append(labels[nid]);
            tci.setOffset(tailArray.getIteratorOffset(nid));
            while (tci.hasNext()) b.append(tci.next());
            String letter = b.toString();
            if (term.get(nid)) ret.add(letter);
            if (r0.isZero(nid)) {
                int s = r1.select0(r0.rank0(nid)) + 1;
                int e = r1.next0(s) + 1;
                for (int i = (e - 1); i >= s; i--) {
                    queue.offerFirst(Pair.create(i, letter));
                }
            }
        }
        return ret;
    }

    @Override
    public void insert(String word) {
        throw new UnsupportedOperationException();
    }

    public class LOUDSNode implements Node {
        public LOUDSNode(int nodeId) {
            this.nodeId = nodeId;
        }

        public int getId() {
            return nodeId;
        }

        @Override
        public char[] getLetters() {
            StringBuilder b = new StringBuilder();
            char h = labels[nodeId];
            if (h != 0xffff) {
                b.append(h);
            }
            int ti = tailArray.getIteratorOffset(nodeId);
            if (ti != -1) {
                TailCharIterator it = tailArray.newIterator();
                it.setOffset(ti);
                while (it.hasNext()) b.append(it.next());
            }
            return b.toString().toCharArray();
        }

        @Override
        public boolean isTerminate() {
            return term.get(nodeId);
        }

        @Override
        public Node getChild(char c) {
            int nid = getChildNode(nodeId, c);
            if (nid == -1) return null;
            else return new LOUDSNode(nid);
        }

        @Override
        public Node[] getChildren() {
            if (!r0.isZero(nodeId)) return new Node[]{};
            int start = r1.select0(r0.rank0(nodeId)) + 1;
            int end = r1.next0(start) + 1;
            Node[] children = new Node[end - start];
            for (int i = start; i < end; i++) {
                children[i - start] = new LOUDSNode(i);
            }
            return children;
        }

        private int nodeId;
    }

    public void trimToSize() {
        if (labels.length > nodeSize) {
            labels = Arrays.copyOf(labels, nodeSize);
        }
        r0.trimToSize();
        r1.trimToSize();
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        size = in.readInt();
        nodeSize = in.readInt();
        labels = new char[nodeSize];
        for (int i = 0; i < nodeSize; i++) {
            labels[i] = in.readChar();
        }
        tailArray = (TailArray) in.readObject();
        term = (BitSet) in.readObject();
        r0.readExternal(in);
        r1.readExternal(in);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(size);
        out.writeInt(nodeSize);
        trimToSize();
        for (char c : labels) {
            out.writeChar(c);
        }
        out.writeObject(tailArray);
        out.writeObject(term);
        r0.writeExternal(out);
        r1.writeExternal(out);
    }

    private int getChildNode(int nodeId, char c) {
        if (!r0.isZero(nodeId)) return -1;
        int start = r1.select0(r0.rank0(nodeId)) + 1;
        int end = r1.next0(start) + 1;
        if (end == -1) return -1;
        if ((end - start) <= 16) {
            for (int i = start; i < end; i++) {
                int d = c - labels[i];
                if (d == 0) {
                    return i;
                }
            }
            return -1;
        } else {
            do {
                int i = (start + end) / 2;
                int d = c - labels[i];
                if (d < 0) {
                    end = i;
                } else if (d > 0) {
                    if (start == i) return -1;
                    else start = i;
                } else {
                    return i;
                }
            } while (start != end);
            return -1;
        }
    }

    private void extend() {
        int nsz = (int) (labels.length * 1.2);
        if (nsz <= labels.length) nsz = labels.length * 2 + 1;
        labels = Arrays.copyOf(labels, nsz);
    }

    protected TailArrayBuilder newTailArrayBuilder(int initialCapacity) {
        return new ConcatTailArrayBuilder(initialCapacity);
    }

    private BytesRank0OnlySuccinctBitVector r0;
    private BytesSuccinctBitVector r1;
    private int size;
    private char[] labels;
    private TailArray tailArray;
    private BitSet term;
    private int nodeSize;
}

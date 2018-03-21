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

import org.trie4j.*;
import org.trie4j.bv.BytesRank1OnlySuccinctBitVector;
import org.trie4j.bv.SuccinctBitVector;
import org.trie4j.louds.bvtree.BvTree;
import org.trie4j.louds.bvtree.LOUDSBvTree;
import org.trie4j.patricia.PatriciaTrie;
import org.trie4j.tail.ConcatTailArrayBuilder;
import org.trie4j.tail.TailArray;
import org.trie4j.tail.TailArrayBuilder;
import org.trie4j.tail.TailCharIterator;
import org.trie4j.util.FastBitSet;
import org.trie4j.util.Pair;
import org.trie4j.util.Range;

import java.io.*;
import java.util.*;

public class TailLOUDSTrie
        extends AbstractTermIdTrie
        implements Externalizable, TermIdTrie {
    protected static interface NodeListener {
        void listen(Node node, int id);
    }

    public TailLOUDSTrie() {
        this(new PatriciaTrie());
    }

    public TailLOUDSTrie(Trie orig) {
        this(orig, new LOUDSBvTree(orig.nodeSize()));
    }

    public TailLOUDSTrie(Trie orig, BvTree bvTree) {
        this(orig, bvTree, new ConcatTailArrayBuilder(orig.size() * 4), new NodeListener() {
            @Override
            public void listen(Node node, int id) {
            }
        });
    }

    public TailLOUDSTrie(Trie orig, BvTree bvTree, TailArrayBuilder tailArrayBuilder) {
        this(orig, bvTree, tailArrayBuilder, new NodeListener() {
            @Override
            public void listen(Node node, int id) {
            }
        });
    }

    public TailLOUDSTrie(Trie orig, TailArrayBuilder tailArrayBuilder) {
        this(orig, tailArrayBuilder, new NodeListener() {
            @Override
            public void listen(Node node, int id) {
            }
        });
    }

    public TailLOUDSTrie(Trie orig, TailArrayBuilder tailArrayBuilder, NodeListener listener) {
        this(orig, new LOUDSBvTree(orig.size()), tailArrayBuilder, listener);
    }

    public TailLOUDSTrie(Trie orig, BvTree bvTree, TailArrayBuilder tailArrayBuilder,
                         NodeListener listener) {
        FastBitSet bs = new FastBitSet(orig.size());
        build(orig, bvTree, tailArrayBuilder, bs, listener);
        this.term = new BytesRank1OnlySuccinctBitVector(bs.getBytes(), bs.size());
        this.tailArray = tailArrayBuilder.build();
        this.bvtree.trimToSize();
    }

    public TailLOUDSTrie(int size, int nodeSize, BvTree bvTree,
                         char[] labels, TailArray tailArray,
                         SuccinctBitVector term) {
        this.size = size;
        this.nodeSize = nodeSize;
        this.bvtree = bvTree;
        this.labels = labels;
        this.tailArray = tailArray;
        this.term = term;
    }

    @Override
    public int size() {
        return size;
    }

    public int nodeSize() {
        return nodeSize;
    }

    public void setNodeSize(int nodeSize) {
        this.nodeSize = nodeSize;
    }

    @Override
    public boolean contains(String text) {
        int nodeId = 0; // root
        Range r = new Range();
        TailCharIterator it = tailArray.newIterator();
        int n = text.length();
        for (int i = 0; i < n; i++) {
            nodeId = getChildNode(nodeId, text.charAt(i), r);
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

    public int getNodeId(String text) {
        int nodeId = 0; // root
        Range r = new Range();
        TailCharIterator it = tailArray.newIterator();
        int n = text.length();
        for (int i = 0; i < n; i++) {
            nodeId = getChildNode(nodeId, text.charAt(i), r);
            if (nodeId == -1) return -1;
            it.setOffset(tailArray.getIteratorOffset(nodeId));
            while (it.hasNext()) {
                i++;
                if (i == n) return -1;
                if (text.charAt(i) != it.next()) return -1;
            }
        }
        return nodeId;
    }

    @Override
    public int getTermId(String text) {
        int nodeId = getNodeId(text);
        if (nodeId == -1) return -1;
        return term.get(nodeId) ? term.rank1(nodeId) - 1 : -1;
    }

    private void build(Trie orig, BvTree bvtree, TailArrayBuilder tailArrayBuilder,
                       FastBitSet termBs, NodeListener listener) {
        this.bvtree = bvtree;
        this.size = orig.size();
        this.labels = new char[size];
        LinkedList<Node> queue = new LinkedList<Node>();
        int count = 0;
        if (orig.getRoot() != null) queue.add(orig.getRoot());
        while (!queue.isEmpty()) {
            Node node = queue.pollFirst();
            int index = count++;
            if (index >= labels.length) {
                extend();
            }
            listener.listen(node, index);
            if (node.isTerminate()) {
                termBs.set(index);
            } else if (termBs.size() <= index) {
                termBs.ensureCapacity(index);
            }
            for (Node c : node.getChildren()) {
                bvtree.appendChild();
                queue.offerLast(c);
            }
            bvtree.appendSelf();
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
        this.nodeSize = count;
    }

    public BvTree getBvTree() {
        return bvtree;
    }

    public void setBvtree(BvTree bvtree) {
        this.bvtree = bvtree;
    }

    public char[] getLabels() {
        return labels;
    }

    public TailArray getTailArray() {
        return tailArray;
    }

    public SuccinctBitVector getTerm() {
        return term;
    }

    @Override
    public TermIdNode getRoot() {
        return new LOUDSNode(0);
    }

    @Override
    public void dump(Writer writer) throws IOException {
        super.dump(writer);
        writer.write(bvtree.toString());
        writer.write("\nlabels: ");
        int count = 0;
        for (char c : labels) {
            writer.write(c);
            if (count++ == 99) break;
        }
        writer.write("\n");
    }

    @Override
    public int findShortestWord(CharSequence chars, int start, int end, StringBuilder word) {
        Range r = new Range();
        TailCharIterator tci = tailArray.newIterator();
        for (int i = start; i < end; i++) {
            int nodeId = 0; // root
            for (int j = i; j < end; j++) {
                int child = getChildNode(nodeId, chars.charAt(j), r);
                if (child == -1) break;
                tci.setIndex(tailArray.getIteratorOffset(child));
                boolean found = true;
                while (tci.hasNext()) {
                    j++;
                    found = false;
                    if (j >= end) break;
                    if (chars.charAt(j) != tci.next()) break;
                    found = true;
                }
                if (!found) break;
                if (term.get(child)) {
                    word.append(chars, i, j + 1);
                    return i;
                }
                nodeId = child;
            }
        }
        return -1;
    }

    @Override
    public int findLongestWord(CharSequence chars, int start, int end, StringBuilder word) {
        Range r = new Range();
        TailCharIterator tci = tailArray.newIterator();
        for (int i = start; i < end; i++) {
            int nodeId = 0; // root
            int lastJ = -1;
            for (int j = i; j < end; j++) {
                int child = getChildNode(nodeId, chars.charAt(j), r);
                if (child == -1) break;
                tci.setIndex(tailArray.getIteratorOffset(child));
                boolean found = true;
                while (tci.hasNext()) {
                    j++;
                    found = false;
                    if (j >= end) break;
                    if (chars.charAt(j) != tci.next()) break;
                    found = true;
                }
                if (!found) break;
                if (term.get(child)) {
                    lastJ = j;
                }
                nodeId = child;
            }
            if (lastJ != -1) {
                word.append(chars, i, lastJ + 1);
                return i;
            }
        }
        return -1;
    }

    @Override
    public Iterable<Pair<String, Integer>> commonPrefixSearchWithTermId(String query) {
        List<Pair<String, Integer>> ret = new ArrayList<Pair<String, Integer>>();
        char[] chars = query.toCharArray();
        int charsLen = chars.length;
        int nodeId = 0; // root
        TailCharIterator tci = tailArray.newIterator();
        Range r = new Range();
        for (int charsIndex = 0; charsIndex < charsLen; charsIndex++) {
            int child = getChildNode(nodeId, chars[charsIndex], r);
            if (child == -1) return ret;
            tci.setOffset(tailArray.getIteratorOffset(child));
            while (tci.hasNext()) {
                charsIndex++;
                if (charsLen <= charsIndex) return ret;
                if (chars[charsIndex] != tci.next()) return ret;
            }
            if (term.get(child)) {
                ret.add(Pair.create(
                        new String(chars, 0, charsIndex + 1),
                        term.rank1(child) - 1));
            }
            nodeId = child;
        }
        return ret;
    }

    @Override
    public Iterable<Pair<String, Integer>> predictiveSearchWithTermId(String query) {
        List<Pair<String, Integer>> ret = new ArrayList<Pair<String, Integer>>();
        char[] chars = query.toCharArray();
        int charsLen = chars.length;
        int nodeId = 0; // root
        Range r = new Range();
        TailCharIterator tci = tailArray.newIterator();
        String pfx = null;
        int charsIndexBack = 0;
        for (int charsIndex = 0; charsIndex < charsLen; charsIndex++) {
            charsIndexBack = charsIndex;
            int child = getChildNode(nodeId, chars[charsIndex], r);
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
            if (nid > 0) {
                b.append(labels[nid]);
            }
            tci.setOffset(tailArray.getIteratorOffset(nid));
            while (tci.hasNext()) b.append(tci.next());
            String letter = b.toString();
            if (term.get(nid)) {
                ret.add(Pair.create(letter, term.rank1(nid) - 1));
            }
            bvtree.getChildNodeIds(nid, r);
            for (int i = (r.getEnd() - 1); i >= r.getStart(); i--) {
                queue.offerFirst(Pair.create(i, letter));
            }
        }
        return ret;
    }

    public class LOUDSNode implements TermIdNode {
        public LOUDSNode(int nodeId) {
            this.nodeId = nodeId;
        }

        public int getNodeId() {
            return nodeId;
        }

        @Override
        public int getTermId() {
            if (!term.get(nodeId)) {
                return -1;
            } else {
                return term.rank1(nodeId) - 1;
            }
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
                TailCharIterator it = tailArray.newIterator(ti);
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
        public LOUDSNode getChild(char c) {
            int nid = getChildNode(nodeId, c, new Range());
            if (nid == -1) return null;
            else return new LOUDSNode(nid);
        }

        @Override
        public LOUDSNode[] getChildren() {
            Range r = new Range();
            bvtree.getChildNodeIds(nodeId, r);
            LOUDSNode[] children = new LOUDSNode[r.getLength()];
            for (int i = r.getStart(); i < r.getEnd(); i++) {
                children[i - r.getStart()] = new LOUDSNode(i);
            }
            return children;
        }

        private int nodeId;
    }

    public void trimToSize() {
        if (labels.length > nodeSize) {
            labels = Arrays.copyOf(labels, nodeSize);
        }
        bvtree.trimToSize();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(size);
        out.writeInt(nodeSize);
        trimToSize();
        out.writeObject(bvtree);
        out.writeObject(labels);
        out.writeObject(tailArray);
        out.writeObject(term);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        size = in.readInt();
        nodeSize = in.readInt();
        bvtree = (BvTree) in.readObject();
        labels = (char[]) in.readObject();
        tailArray = (TailArray) in.readObject();
        term = (SuccinctBitVector) in.readObject();
    }

    private int getChildNode(int nodeId, char c, Range r) {
        bvtree.getChildNodeIds(nodeId, r);
        int start = r.getStart();
        int end = r.getEnd();
        if (end == -1) return -1;
        if ((end - start) <= 16) {
            for (int i = start; i < end; i++) {
                if (c == labels[i]) return i;
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

    private BvTree bvtree;
    private int size;
    private char[] labels;
    private TailArray tailArray;
    private SuccinctBitVector term;
    private int nodeSize;
}

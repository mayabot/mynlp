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
package org.trie4j.doublearray;

import org.trie4j.*;
import org.trie4j.bv.BytesRank1OnlySuccinctBitVector;
import org.trie4j.bv.SuccinctBitVector;
import org.trie4j.util.BitSet;
import org.trie4j.util.FastBitSet;
import org.trie4j.util.Pair;

import java.io.*;
import java.util.*;

public class DoubleArray
        extends AbstractTermIdTrie
        implements Externalizable, TermIdTrie {
    public static interface TermNodeListener {
        void listen(Node node, int nodeIndex);
    }

    public DoubleArray() {
    }

    public DoubleArray(Trie trie) {
        this(trie, trie.size() * 2);
    }

    public DoubleArray(Trie trie, int arraySize) {
        this(trie, arraySize, new TermNodeListener() {
            @Override
            public void listen(Node node, int nodeIndex) {
            }
        });
    }

    public DoubleArray(Trie trie, int arraySize, TermNodeListener listener) {
        if (arraySize <= 1) arraySize = 2;
        size = trie.size();
        base = new int[arraySize];
        Arrays.fill(base, BASE_EMPTY);
        check = new int[arraySize];
        Arrays.fill(check, -1);
        FastBitSet bs = new FastBitSet(arraySize);
        nodeSize = 1; // for root node because it has no letter;
        build(trie.getRoot(), 0, bs, listener);

        term = new BytesRank1OnlySuccinctBitVector(bs.getBytes(), bs.size());
        base = Arrays.copyOf(base, last + chars.size());
        check = Arrays.copyOf(check, last + chars.size());
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
    public TermIdNode getRoot() {
        return newDoubleArrayNode(0);
    }

    public int[] getBase() {
        return base;
    }

    public int[] getCheck() {
        return check;
    }

    public BitSet getTerm() {
        return term;
    }

    protected class DoubleArrayNode implements TermIdNode {
        public DoubleArrayNode(int nodeId) {
            this.nodeId = nodeId;
        }

        public DoubleArrayNode(int nodeId, char firstChar) {
            this.nodeId = nodeId;
            this.firstChar = firstChar;
        }

        @Override
        public boolean isTerminate() {
            return term.get(nodeId);
        }

        @Override
        public char[] getLetters() {
            StringBuilder ret = new StringBuilder();
            if (firstChar != 0) ret.append(firstChar);
            return ret.toString().toCharArray();
        }

        @Override
        public DoubleArrayNode[] getChildren() {
            CharSequence children = listupChildChars(nodeId);
            if (children.length() == 0) return emptyNodes;
            return listupChildNodes(base[nodeId], children);
        }

        @Override
        public DoubleArrayNode getChild(char c) {
            int code = charToCode[c];
            if (code == -1) return null;
            int nid = base[nodeId] + code;
            if (nid >= 0 && nid < check.length && check[nid] == nodeId) return new DoubleArrayNode(nid, c);
            return null;
        }

        public int getNodeId() {
            return nodeId;
        }

        @Override
        public int getTermId() {
            if (!term.get(nodeId)) {
                return -1;
            }
            return term.rank1(nodeId) - 1;
        }

        private CharSequence listupChildChars(int nodeId) {
            StringBuilder b = new StringBuilder();
            int bs = base[nodeId];
            for (char c : chars) {
                int nid = bs + charToCode[c];
                if (nid >= 0 && nid < check.length && check[nid] == nodeId) {
                    b.append(c);
                }
            }
            return b;
        }

        private DoubleArrayNode[] listupChildNodes(int base, CharSequence chars) {
            int n = chars.length();
            DoubleArrayNode[] ret = new DoubleArrayNode[n];
            for (int i = 0; i < n; i++) {
                char c = chars.charAt(i);
                char code = charToCode[c];
                ret[i] = newDoubleArrayNode(base + code, c);
            }
            return ret;
        }

        private char firstChar = 0;
        private int nodeId;
    }

    @Override
    public boolean contains(String text) {
        int nodeIndex = 0; // root
        int n = text.length();
        for (int i = 0; i < n; i++) {
            char cid = charToCode[text.charAt(i)];
            if (cid == 0) return false;
            int next = base[nodeIndex] + cid;
            if (next < 0 || check[next] != nodeIndex) return false;
            nodeIndex = next;
        }
        return term.get(nodeIndex);
    }

    public int getNodeId(String text) {
        int nodeIndex = 0; // root
        int n = text.length();
        for (int i = 0; i < n; i++) {
            char cid = charToCode[text.charAt(i)];
            if (cid == 0) return -1;
            int next = base[nodeIndex] + cid;
            if (next < 0 || check[next] != nodeIndex) return -1;
            nodeIndex = next;
        }
        return nodeIndex;
    }

    @Override
    public int getTermId(String text) {
        int nid = getNodeId(text);
        if (nid == -1) return -1;
        return term.get(nid) ? term.rank1(nid) - 1 : -1;
    }

    @Override
    public int findShortestWord(CharSequence chars, int start, int end, StringBuilder word) {
        int checkLen = check.length;
        for (int i = start; i < end; i++) {
            int nodeIndex = 0;
            try {
                for (int j = i; j < end; j++) {
                    int cid = findCharId(chars.charAt(j));
                    if (cid == -1) break;
                    int b = base[nodeIndex];
                    if (b == BASE_EMPTY) break;
                    int next = b + cid;
                    if (next < 0 || checkLen <= next || check[next] != nodeIndex) break;
                    nodeIndex = next;
                    if (term.get(nodeIndex)) {
                        if (word != null) word.append(chars, i, j + 1);
                        return i;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                break;
            }
        }
        return -1;
    }

    @Override
    public int findLongestWord(CharSequence chars, int start, int end, StringBuilder word) {
        int checkLen = check.length;
        for (int i = start; i < end; i++) {
            int nodeIndex = 0;
            try {
                int lastJ = -1;
                for (int j = i; j < end; j++) {
                    int cid = findCharId(chars.charAt(j));
                    if (cid == -1) break;
                    int b = base[nodeIndex];
                    if (b == BASE_EMPTY) break;
                    int next = b + cid;
                    if (next < 0 || checkLen <= next || check[next] != nodeIndex) break;
                    nodeIndex = next;
                    if (term.get(nodeIndex)) {
                        lastJ = j;
                    }
                }
                if (lastJ != -1) {
                    if (word != null) word.append(chars, i, lastJ + 1);
                    return i;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                break;
            }
        }
        return -1;
    }

    @Override
    public Iterable<String> commonPrefixSearch(String query) {
        List<String> ret = new ArrayList<String>();
        char[] chars = query.toCharArray();
        int charsLen = chars.length;
        int checkLen = check.length;
        int nodeIndex = 0;
        for (int i = 0; i < charsLen; i++) {
            int cid = findCharId(chars[i]);
            if (cid == -1) return ret;
            int b = base[nodeIndex];
            if (b == BASE_EMPTY) return ret;
            int next = b + cid;
            if (next < 0 || checkLen <= next || check[next] != nodeIndex) return ret;
            nodeIndex = next;
            if (term.get(nodeIndex)) ret.add(new String(chars, 0, i + 1));
        }
        return ret;
    }

    @Override
    public Iterable<Pair<String, Integer>> commonPrefixSearchWithTermId(
            String query) {
        List<Pair<String, Integer>> ret = new ArrayList<Pair<String, Integer>>();
        char[] chars = query.toCharArray();
        int charsLen = chars.length;
        int checkLen = check.length;
        int nodeIndex = 0;
        for (int i = 0; i < charsLen; i++) {
            int cid = findCharId(chars[i]);
            if (cid == -1) return ret;
            int b = base[nodeIndex];
            if (b == BASE_EMPTY) return ret;
            int next = b + cid;
            if (next < 0 || checkLen <= next || check[next] != nodeIndex) return ret;
            nodeIndex = next;
            if (term.get(nodeIndex)) {
                ret.add(Pair.create(
                        new String(chars, 0, i + 1),
                        term.rank1(nodeIndex) - 1
                ));
            }
        }
        return ret;
    }

    @Override
    public Iterable<String> predictiveSearch(String prefix) {
        List<String> ret = new ArrayList<String>();
        char[] chars = prefix.toCharArray();
        int charsLen = chars.length;
        int checkLen = check.length;
        int nodeIndex = 0;
        for (int i = 0; i < charsLen; i++) {
            int cid = findCharId(chars[i]);
            if (cid == -1) return ret;
            int next = base[nodeIndex] + cid;
            if (next < 0 || checkLen <= next || check[next] != nodeIndex) return ret;
            nodeIndex = next;
        }
        if (term.get(nodeIndex)) {
            ret.add(prefix);
        }
        Deque<Pair<Integer, String>> q = new LinkedList<Pair<Integer, String>>();
        q.add(Pair.create(nodeIndex, prefix));
        while (!q.isEmpty()) {
            Pair<Integer, String> p = q.pop();
            int ni = p.getFirst();
            int b = base[ni];
            if (b == BASE_EMPTY) continue;
            String c = p.getSecond();
            for (char v : this.chars) {
                int next = b + charToCode[v];
                if (next < 0 || checkLen <= next || check[next] != ni) continue;
                String n = new StringBuilder(c).append(v).toString();
                if (term.get(next)) {
                    ret.add(n);
                }
                q.push(Pair.create(next, n));
            }
        }
        return ret;
    }

    @Override
    public Iterable<Pair<String, Integer>> predictiveSearchWithTermId(
            String prefix) {
        List<Pair<String, Integer>> ret = new ArrayList<Pair<String, Integer>>();
        char[] chars = prefix.toCharArray();
        int charsLen = chars.length;
        if (charsLen == 0) return ret;
        if (this.nodeSize == 0) return ret;
        int checkLen = check.length;
        int nodeIndex = 0;
        for (int i = 0; i < charsLen; i++) {
            int cid = findCharId(chars[i]);
            if (cid == -1) return ret;
            int next = base[nodeIndex] + cid;
            if (next < 0 || checkLen <= next || check[next] != nodeIndex) return ret;
            nodeIndex = next;
        }
        if (term.get(nodeIndex)) {
            ret.add(Pair.create(prefix, term.rank1(nodeIndex) - 1));
        }
        Deque<Pair<Integer, String>> q = new LinkedList<Pair<Integer, String>>();
        q.add(Pair.create(nodeIndex, prefix));
        while (!q.isEmpty()) {
            Pair<Integer, String> p = q.pop();
            int ni = p.getFirst();
            int b = base[ni];
            if (b == BASE_EMPTY) continue;
            String c = p.getSecond();
            for (char v : this.chars) {
                int next = b + charToCode[v];
                if (next < 0 || checkLen <= next || check[next] != ni) continue;
                String n = new StringBuilder(c).append(v).toString();
                if (term.get(next)) {
                    ret.add(Pair.create(
                            n,
                            term.rank1(next) - 1
                    ));
                }
                q.push(Pair.create(next, n));
            }
        }
        return ret;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(size);
        out.writeInt(nodeSize);
        out.writeInt(base.length);
        for (int v : base) {
            out.writeInt(v);
        }
        for (int v : check) {
            out.writeInt(v);
        }
        out.writeObject(term);
        out.writeInt(firstEmptyCheck);
        out.writeInt(chars.size());
        for (char c : chars) {
            out.writeChar(c);
            out.writeChar(charToCode[c]);
        }
    }

    public void save(OutputStream os) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(os);
        try {
            writeExternal(out);
        } finally {
            out.flush();
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        size = in.readInt();
        nodeSize = in.readInt();
        int len = in.readInt();
        base = new int[len];
        for (int i = 0; i < len; i++) {
            base[i] = in.readInt();
        }
        check = new int[len];
        for (int i = 0; i < len; i++) {
            check[i] = in.readInt();
        }
        try {
            term = (SuccinctBitVector) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        firstEmptyCheck = in.readInt();
        int n = in.readInt();
        for (int i = 0; i < n; i++) {
            char c = in.readChar();
            char v = in.readChar();
            chars.add(c);
            charToCode[c] = v;
        }
    }

    public void load(InputStream is) throws IOException {
        try {
            readExternal(new ObjectInputStream(is));
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void trimToSize() {
        int sz = last + 1 + 0xFFFF;
        if (base.length > sz) {
            base = Arrays.copyOf(base, sz);
            check = Arrays.copyOf(check, sz);
        }
    }

    @Override
    public void dump(Writer w) {
        PrintWriter writer = new PrintWriter(w);
        try {
            int n = Math.min(16, base.length);
            writer.println("array size: " + base.length);
            writer.print("      |");
            for (int i = 0; i < n; i++) {
                writer.print(String.format("%3d|", i));
            }
            writer.println();
            writer.print("|base |");
            for (int i = 0; i < n; i++) {
                if (base[i] == BASE_EMPTY) {
                    writer.print("N/A|");
                } else {
                    writer.print(String.format("%3d|", base[i]));
                }
            }
            writer.println();
            writer.print("|check|");
            for (int i = 0; i < n; i++) {
                if (check[i] < 0) {
                    writer.print("N/A|");
                } else {
                    writer.print(String.format("%3d|", check[i]));
                }
            }
            writer.println();
            writer.print("|term |");
            for (int i = 0; i < n; i++) {
                writer.print(String.format("%3d|", term.get(i) ? 1 : 0));
            }
            writer.println();
            writer.print("chars: ");
            int c = 0;
            for (char e : chars) {
                writer.print(String.format("%c:%d,", e, (int) charToCode[e]));
                c++;
                if (c > 16) break;
            }
            writer.println();
            writer.println("chars count: " + chars.size());
            writer.println();
        } finally {
            writer.flush();
        }
    }

    private void build(Node node, int nodeIndex,
                       FastBitSet bs, TermNodeListener listener) {

        // letters
        char[] letters = node.getLetters();
        int lettersLen = letters.length;
        if (lettersLen > 0) nodeSize++; // for first letter
        for (int i = 1; i < lettersLen; i++) {
            bs.unsetIfLE(nodeIndex);
            int cid = getCharId(letters[i]);
            int empty = findFirstEmptyCheck();
            setCheck(empty, nodeIndex);
            base[nodeIndex] = empty - cid;
            nodeSize++;
            nodeIndex = empty;
        }
        if (node.isTerminate()) {
            bs.set(nodeIndex);
            if (listener != null) listener.listen(node, nodeIndex);
        } else {
            bs.unsetIfLE(nodeIndex);
        }


        // children
        Node[] children = node.getChildren();
        int childrenLen = children.length;
        if (childrenLen == 0) return;
        int[] heads = new int[childrenLen];
        int maxHead = 0;
        int minHead = Integer.MAX_VALUE;
        for (int i = 0; i < childrenLen; i++) {
            heads[i] = getCharId(children[i].getLetters()[0]);
            maxHead = Math.max(maxHead, heads[i]);
            minHead = Math.min(minHead, heads[i]);
        }

        int offset = findInsertOffset(heads, minHead, maxHead);

        base[nodeIndex] = offset;
        for (int cid : heads) {
            setCheck(offset + cid, nodeIndex);
        }
/*
		for(int i = 0; i < children.length; i++){
			build(children[i], offset + heads[i]);
		}
/*/
        // sort children by children's children count.
        Map<Integer, List<Pair<Node, Integer>>> nodes = new TreeMap<Integer, List<Pair<Node, Integer>>>(new Comparator<Integer>() {
            @Override
            public int compare(Integer arg0, Integer arg1) {
                return arg1 - arg0;
            }
        });


        for (int i = 0; i < children.length; i++) {
            Node[] c = children[i].getChildren();
            int n = 0;
            if (c != null) {
                n = c.length;
            }
            List<Pair<Node, Integer>> p = nodes.get(n);
            if (p == null) {
                p = new ArrayList<Pair<Node, Integer>>();
                nodes.put(n, p);
            }
            p.add(Pair.create(children[i], heads[i]));
        }


        for (Map.Entry<Integer, List<Pair<Node, Integer>>> e : nodes.entrySet()) {
            for (Pair<Node, Integer> e2 : e.getValue()) {
                build(e2.getFirst(), e2.getSecond() + offset, bs, listener);
            }
        }

//*/
    }

    private DoubleArrayNode newDoubleArrayNode(int id) {
        return new DoubleArrayNode(id);
    }

    private DoubleArrayNode newDoubleArrayNode(int id, char s) {
        return new DoubleArrayNode(id, s);
    }

    private int findCharId(char c) {
        char v = charToCode[c];
        if (v != 0) return v;
        return -1;
    }

    private int findInsertOffset(int[] heads, int minHead, int maxHead) {

        for (int empty = findFirstEmptyCheck(); ; empty = findNextEmptyCheck(empty)) {
            int offset = empty - minHead;
            if ((offset + maxHead) >= check.length) {
                extend(offset + maxHead);
            }
            // find space
            boolean found = true;
            for (int cid : heads) {
                if (check[offset + cid] >= 0) {
                    found = false;
                    break;
                }
            }

            if (found) return offset;
        }

    }


    private int getCharId(char c) {
        char v = charToCode[c];
        if (v != 0) return v;
        v = (char) (chars.size() + 1);
        chars.add(c);
        charToCode[c] = v;
        return v;
    }

    private void extend(int i) {
        int sz = base.length;
        int nsz = Math.max(i + 0xFFFF, (int) (sz * 1.5));
//		System.out.println("extend to " + nsz);
        base = Arrays.copyOf(base, nsz);
        Arrays.fill(base, sz, nsz, BASE_EMPTY);
        check = Arrays.copyOf(check, nsz);
        Arrays.fill(check, sz, nsz, -1);

    }

    private int findFirstEmptyCheck() {

        int i = firstEmptyCheck;
        while (check[i] >= 0 || base[i] != BASE_EMPTY) {
            i++;
        }
        firstEmptyCheck = i;

        return i;

    }

    private int findNextEmptyCheck(int i) {
/*
		for(i++; i < check.length; i++){
			if(check[i] < 0) return i;
		}
		extend(i);
		return i;
/*/

        int d = check[i] * -1;
        if (d <= 0) {
            throw new RuntimeException();
        }
        int prev = i;
        i += d;
        if (check.length <= i) {
            extend(i);
            return i;
        }
        if (check[i] < 0) {
            return i;
        }
        final int checkLength = check.length;
        for (i++; i < checkLength; i++) {
            if (check[i] < 0) {
                check[prev] = prev - i;
                return i;
            }
        }
        extend(i);
        check[prev] = prev - i;
        return i;


//*/
    }

    private void setCheck(int index, int id) {
        if (firstEmptyCheck == index) {
            firstEmptyCheck = findNextEmptyCheck(firstEmptyCheck);
        }
        check[index] = id;
        last = Math.max(last, index);
    }

    private int size;
    private int nodeSize;
    private int[] base;
    private int[] check;
    private int firstEmptyCheck = 1;
    private int last;
    private SuccinctBitVector term;
    private Set<Character> chars = new TreeSet<Character>();
    private char[] charToCode = new char[Character.MAX_VALUE + 1];
    private static final int BASE_EMPTY = Integer.MAX_VALUE;
    private static final DoubleArrayNode[] emptyNodes = {};
}

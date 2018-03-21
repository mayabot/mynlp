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
package org.trie4j.io;

import org.trie4j.Trie;
import org.trie4j.bv.*;
import org.trie4j.louds.TailLOUDSTrie;
import org.trie4j.louds.bvtree.BvTree;
import org.trie4j.louds.bvtree.LOUDSBvTree;
import org.trie4j.louds.bvtree.LOUDSPPBvTree;
import org.trie4j.tail.DefaultTailArray;
import org.trie4j.tail.TailArray;
import org.trie4j.tail.index.ArrayTailIndex;
import org.trie4j.tail.index.DenseArrayTailIndex;
import org.trie4j.tail.index.SBVTailIndex;
import org.trie4j.tail.index.TailIndex;
import org.trie4j.util.IntArray;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class TrieReader implements Constants {
    public TrieReader(InputStream is) {
        dis = new DataInputStream(is);
    }

    public Trie read() throws IOException {
        short type = dis.readShort();
        switch (type) {
            case TYPE_TRIE_LOUDS_TAIL:
                return readTailLOUDSTrie();
            default:
                throw new IOException(String.format(
                        "unknown trie type: 0x%04x", type
                ));
        }
    }

    public TailLOUDSTrie readTailLOUDSTrie() throws IOException {
        return new TailLOUDSTrie(
                dis.readInt(), dis.readInt(),
                readBvTree(), readChars(),
                readTailArray(), readSuccinctBitVector()) {
        };
    }

    public BvTree readBvTree() throws IOException {
        short type = dis.readShort();
        switch (type) {
            case TYPE_BVTREE_LOUDS:
                return readLOUDSBvTree();
            case TYPE_BVTREE_LOUDSPP:
                return readLOUDSPPBvTree();
            default:
                throw new IOException(String.format(
                        "unknown bvTree type: 0x%04x", type
                ));
        }
    }

    public LOUDSBvTree readLOUDSBvTree() throws IOException {
        return new LOUDSBvTree(readBytesSuccinctBitVector());
    }

    public LOUDSPPBvTree readLOUDSPPBvTree() throws IOException {
        return new LOUDSPPBvTree(
                readBitVector01Divider(),
                readSuccinctBitVector(),
                readSuccinctBitVector());
    }

    public TailArray readTailArray() throws IOException {
        short type = dis.readShort();
        switch (type) {
            case TYPE_TAILARRAY_DEFAULT:
                return readDefaultTailArray();
            default:
                throw new IOException(String.format(
                        "unknown tailArray type: 0x%04x", type
                ));
        }
    }

    public DefaultTailArray readDefaultTailArray() throws IOException {
        return new DefaultTailArray(
                new String(readChars()), readTailIndex()
        );
    }

    public TailIndex readTailIndex() throws IOException {
        short type = dis.readShort();
        switch (type) {
            case TYPE_TAILINDEX_ARRAY:
                return readArrayTailIndex();
            case TYPE_TAILINDEX_DENSEARRAY:
                return readDenseArrayTailIndex();
            case TYPE_TAILINDEX_SBV:
                return readSBVTailIndex();
            default:
                throw new IOException(String.format(
                        "unknown tailIndex type: 0x%04x", type
                ));
        }
    }

    public ArrayTailIndex readArrayTailIndex() throws IOException {
        return new ArrayTailIndex(readInts());
    }

    public DenseArrayTailIndex readDenseArrayTailIndex() throws IOException {
        return new DenseArrayTailIndex(
                readSuccinctBitVector(), readInts());
    }

    public SBVTailIndex readSBVTailIndex() throws IOException {
        return new SBVTailIndex(
                readSuccinctBitVector(), dis.readInt());
    }

    public BitVector01Divider readBitVector01Divider() throws IOException {
        return new BitVector01Divider(
                dis.readBoolean(), dis.readBoolean());
    }

    public SuccinctBitVector readSuccinctBitVector() throws IOException {
        short type = dis.readShort();
        switch (type) {
            case TYPE_SBV_BYTES:
                return readBytesSuccinctBitVector();
            case TYPE_SBV_RANK0ONLY:
                return readRank0OnlySuccinctBitVector();
            case TYPE_SBV_RANK1ONLY:
                return readRank1OnlySuccinctBitVector();
            case TYPE_SBV_LONGS:
                return readLongsSuccinctBitVector();
        }
        return null;
    }

    public BytesSuccinctBitVector readBytesSuccinctBitVector() throws IOException {
        return new BytesSuccinctBitVector(
                readBytes(), dis.readInt(), dis.readInt(),
                dis.readInt(), dis.readInt(), dis.readInt(),
                readInts(), readIntArray()
        );
    }

    public LongsSuccinctBitVector readLongsSuccinctBitVector() throws IOException {
        return new LongsSuccinctBitVector(
                readLongs(), dis.readInt(), dis.readInt(),
                dis.readInt(), dis.readInt(), dis.readInt(),
                readInts(), readIntArray()
        );
    }

    public BytesRank0OnlySuccinctBitVector readRank0OnlySuccinctBitVector() throws IOException {
        return new BytesRank0OnlySuccinctBitVector(
                readBytes(), dis.readInt(), readInts()
        );
    }

    public BytesRank1OnlySuccinctBitVector readRank1OnlySuccinctBitVector() throws IOException {
        return new BytesRank1OnlySuccinctBitVector(
                readBytes(), dis.readInt(), readInts()
        );
    }

    public byte[] readBytes()
            throws IOException {
        int n = dis.readInt();
        byte[] ret = new byte[n];
        int offset = 0;
        while (n > 0) {
            int s = dis.read(ret, offset, n);
            if (s == -1) {
                throw new EOFException("failed to read expected bytes. exp:" +
                        (offset + n) + " act:" + offset);
            }
            offset += s;
            n -= s;
        }
        return ret;
    }

    public char[] readChars()
            throws IOException {
        int n = dis.readInt();
        char[] ret = new char[n];
        for (int i = 0; i < n; i++) {
            ret[i] = dis.readChar();
        }
        return ret;
    }

    public int[] readInts()
            throws IOException {
        int n = dis.readInt();
        int[] ret = new int[n];
        for (int i = 0; i < n; i++) {
            ret[i] = dis.readInt();
        }
        return ret;
    }

    public long[] readLongs()
            throws IOException {
        int n = dis.readInt();
        long[] ret = new long[n];
        for (int i = 0; i < n; i++) {
            ret[i] = dis.readLong();
        }
        return ret;
    }

    public IntArray readIntArray()
            throws IOException {
        int[] i = readInts();
        return new IntArray(i, i.length);
    }

    private DataInputStream dis;
}

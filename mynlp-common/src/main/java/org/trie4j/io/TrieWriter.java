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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TrieWriter implements Constants {
    public TrieWriter(OutputStream os) {
        dos = new DataOutputStream(os);
    }

    public void flush() throws IOException {
        dos.flush();
    }

    public void write(Trie trie) throws IOException {
        if (trie instanceof TailLOUDSTrie) {
            dos.writeShort(TYPE_TRIE_LOUDS_TAIL);
            writeTailLOUDSTrie((TailLOUDSTrie) trie);
        } else {
            throw new IOException("unknown trie: " + trie.getClass().getName());
        }
    }

    public void writeTailLOUDSTrie(TailLOUDSTrie trie) throws IOException {
        trie.trimToSize();
        dos.writeInt(trie.size());
        dos.writeInt(trie.nodeSize());
        writeBvTree(trie.getBvTree());
        writeChars(trie.getLabels());
        writeTailArray(trie.getTailArray());
        writeSuccinctBitVector(trie.getTerm());
    }

    public void writeBvTree(BvTree bvTree) throws IOException {
        if (bvTree instanceof LOUDSBvTree) {
            dos.writeShort(TYPE_BVTREE_LOUDS);
            writeLOUDSBvTree((LOUDSBvTree) bvTree);
        } else if (bvTree instanceof LOUDSPPBvTree) {
            dos.writeShort(TYPE_BVTREE_LOUDSPP);
            writeLOUDSPPBvTree((LOUDSPPBvTree) bvTree);
        } else {
            throw new IOException("unknown bv tree: " + bvTree.getClass().getName());
        }
    }

    public void writeLOUDSBvTree(LOUDSBvTree bvTree) throws IOException {
        bvTree.trimToSize();
        writeSuccinctBitVector(bvTree.getSbv());
    }

    public void writeLOUDSPPBvTree(LOUDSPPBvTree bvTree) throws IOException {
        bvTree.trimToSize();
        writeBitVector01Divider(bvTree.getDivider());
        writeSuccinctBitVector(bvTree.getR0());
        writeSuccinctBitVector(bvTree.getR1());
    }

    public void writeTailArray(TailArray tailArray) throws IOException {
        if (tailArray instanceof DefaultTailArray) {
            dos.writeShort(TYPE_TAILARRAY_DEFAULT);
            writeDefaultTailArray((DefaultTailArray) tailArray);
        } else {
            throw new IOException("unknown tail array: " + tailArray.getClass().getName());
        }
    }

    public void writeDefaultTailArray(DefaultTailArray tailArray) throws IOException {
        writeChars(tailArray.getTail());
        writeTailIndex(tailArray.getTailIndex());
    }

    public void writeTailIndex(TailIndex tailIndex) throws IOException {
        if (tailIndex instanceof ArrayTailIndex) {
            dos.writeShort(TYPE_TAILINDEX_ARRAY);
            writeArrayTailIndex((ArrayTailIndex) tailIndex);
        } else if (tailIndex instanceof DenseArrayTailIndex) {
            dos.writeShort(TYPE_TAILINDEX_DENSEARRAY);
            writeDenseArrayTailIndex((DenseArrayTailIndex) tailIndex);
        } else if (tailIndex instanceof SBVTailIndex) {
            dos.writeShort(TYPE_TAILINDEX_SBV);
            writeSBVTailIndex((SBVTailIndex) tailIndex);
        } else {
            throw new IOException("unknown tail index: " + tailIndex.getClass().getName());
        }
    }

    public void writeArrayTailIndex(ArrayTailIndex tailIndex) throws IOException {
        writeInts(tailIndex.getIndexes());
    }

    public void writeDenseArrayTailIndex(DenseArrayTailIndex tailIndex) throws IOException {
        writeSuccinctBitVector(tailIndex.getSbv());
        writeInts(tailIndex.getTail());
    }

    public void writeSBVTailIndex(SBVTailIndex tailIndex) throws IOException {
        writeSuccinctBitVector(tailIndex.getSbv());
        dos.writeInt(tailIndex.size());
    }

    /**
     * Write BitVector01Divider to OutputStream. This method doesn't care about
     * r0 and r1. Caller must write these bvs.
     *
     * @param divider
     * @throws IOException
     */
    public void writeBitVector01Divider(BitVector01Divider divider) throws IOException {
        dos.writeBoolean(divider.isFirst());
        dos.writeBoolean(divider.isZeroCounting());
    }

    public void writeSuccinctBitVector(SuccinctBitVector sbv) throws IOException {
        if (sbv instanceof BytesSuccinctBitVector) {
            dos.writeShort(TYPE_SBV_BYTES);
            writeBytesSuccinctBitVector((BytesSuccinctBitVector) sbv);
        } else if (sbv instanceof BytesRank0OnlySuccinctBitVector) {
            dos.writeShort(TYPE_SBV_RANK0ONLY);
            writeRank0OnlySuccinctBitVector((BytesRank0OnlySuccinctBitVector) sbv);
        } else if (sbv instanceof BytesRank1OnlySuccinctBitVector) {
            dos.writeShort(TYPE_SBV_RANK1ONLY);
            writeRank1OnlySuccinctBitVector((BytesRank1OnlySuccinctBitVector) sbv);
        } else if (sbv instanceof BytesRank1OnlySuccinctBitVector) {
            dos.writeShort(TYPE_SBV_LONGS);
            writeLongsSuccinctBitVector((LongsSuccinctBitVector) sbv);
        } else {
            throw new IOException("unknown sbv: " + sbv.getClass().getName());
        }
    }

    public void writeRank0OnlySuccinctBitVector(BytesRank0OnlySuccinctBitVector sbv)
            throws IOException {
        sbv.trimToSize();
        writeBytes(sbv.getVector());
        dos.writeInt(sbv.size());
        writeInts(sbv.getCountCache0());
    }

    public void writeRank1OnlySuccinctBitVector(BytesRank1OnlySuccinctBitVector sbv)
            throws IOException {
        sbv.trimToSize();
        writeBytes(sbv.getBytes());
        dos.writeInt(sbv.size());
        writeInts(sbv.getCountCache1());
    }

    public void writeBytesSuccinctBitVector(BytesSuccinctBitVector sbv)
            throws IOException {
        sbv.trimToSize();
        writeBytes(sbv.getBytes());
        dos.writeInt(sbv.size());
        dos.writeInt(sbv.getSize0());
        dos.writeInt(sbv.getNode1pos());
        dos.writeInt(sbv.getNode2pos());
        dos.writeInt(sbv.getNode3pos());
        writeInts(sbv.getCountCache0());
        writeIntArray(sbv.getIndexCache0());
    }

    public void writeLongsSuccinctBitVector(LongsSuccinctBitVector sbv)
            throws IOException {
        sbv.trimToSize();
        writeLongs(sbv.getLongs());
        dos.writeInt(sbv.size());
        dos.writeInt(sbv.getSize0());
        dos.writeInt(sbv.getNode1pos());
        dos.writeInt(sbv.getNode2pos());
        dos.writeInt(sbv.getNode3pos());
        writeInts(sbv.getCountCache0());
        writeIntArray(sbv.getIndexCache0());
    }

    public void writeBytes(byte[] data)
            throws IOException {
        dos.writeInt(data.length);
        dos.write(data);
    }

    public void writeChars(char[] data)
            throws IOException {
        dos.writeInt(data.length);
        for (char d : data) {
            dos.writeChar(d);
        }
    }

    public void writeChars(CharSequence data)
            throws IOException {
        if (data instanceof StringBuilder) {
            ((StringBuilder) data).trimToSize();
        }
        int n = data.length();
        dos.writeInt(data.length());
        for (int i = 0; i < n; i++) {
            dos.writeChar(data.charAt(i));
        }
    }

    public void writeInts(int[] data)
            throws IOException {
        dos.writeInt(data.length);
        for (int d : data) {
            dos.writeInt(d);
        }
    }

    public void writeLongs(long[] data)
            throws IOException {
        dos.writeInt(data.length);
        for (long d : data) {
            dos.writeLong(d);
        }
    }

    public void writeIntArray(IntArray data)
            throws IOException {
        writeInts(data.getElements());
    }

    private DataOutputStream dos;
}

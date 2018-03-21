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
package org.trie4j.tail.index;

import org.trie4j.bv.LongsRank1OnlySuccinctBitVector;
import org.trie4j.bv.SuccinctBitVector;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class DenseArrayTailIndex
        implements Externalizable, TailIndex {
    public DenseArrayTailIndex() {
    }

    public DenseArrayTailIndex(int[] tail, byte[] bits, int bitsSize) {
        this.sbv = new LongsRank1OnlySuccinctBitVector(bits, bitsSize);
        this.tail = tail;
    }

    public DenseArrayTailIndex(SuccinctBitVector sbv, int[] tail) {
        this.sbv = sbv;
        this.tail = tail;
    }

    public SuccinctBitVector getSbv() {
        return sbv;
    }

    public void setSbv(SuccinctBitVector sbv) {
        this.sbv = sbv;
    }

    public int[] getTail() {
        return tail;
    }

    public void setTail(int[] tail) {
        this.tail = tail;
    }

    @Override
    public int size() {
        return sbv.size();
    }

    @Override
    public int get(int nodeId) {
        if (sbv.isZero(nodeId)) return -1;
        return tail[sbv.rank1(nodeId) - 1];
    }

    @Override
    public void readExternal(ObjectInput in)
            throws ClassNotFoundException, IOException {
        sbv = (SuccinctBitVector) in.readObject();
        tail = (int[]) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(sbv);
        out.writeObject(tail);
    }

    private SuccinctBitVector sbv = new LongsRank1OnlySuccinctBitVector();
    private int[] tail = {};
}

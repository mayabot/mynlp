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
package org.trie4j.tail.index;

import org.trie4j.bv.BytesSuccinctBitVector;
import org.trie4j.bv.SuccinctBitVector;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SBVTailIndex
        implements Externalizable, TailIndex {
    public SBVTailIndex() {
        sbv = new BytesSuccinctBitVector();
    }

    public SBVTailIndex(SuccinctBitVector sbv, int size) {
        this.sbv = sbv;
        this.size = size;
    }

    public SBVTailIndex(byte[] bits, int bitSize, int size) {
        this.sbv = new BytesSuccinctBitVector(bits, bitSize);
        this.size = size;
    }

    public SuccinctBitVector getSbv() {
        return sbv;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int get(int nodeId) {
        if (nodeId == 0) {
            if (sbv.isZero(0)) return -1;
            else return 0;
        }
        int s = sbv.select0(nodeId);
        if (sbv.isZero(s + 1)) return -1;
        return sbv.rank1(s);
    }

    @Override
    public void readExternal(ObjectInput in)
            throws ClassNotFoundException, IOException {
        sbv = (SuccinctBitVector) in.readObject();
        size = in.readInt();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(sbv);
        out.writeInt(size);
    }

    private SuccinctBitVector sbv;
    private int size;
    private static final long serialVersionUID = 8843853578097509573L;
}

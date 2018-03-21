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
package org.trie4j.louds.bvtree;

import org.trie4j.bv.BitVector01Divider;
import org.trie4j.bv.BytesRank0OnlySuccinctBitVector;
import org.trie4j.bv.BytesSuccinctBitVector;
import org.trie4j.bv.SuccinctBitVector;
import org.trie4j.util.Range;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class LOUDSPPBvTree
        implements Externalizable, BvTree {
    public LOUDSPPBvTree() {
        this(0);
    }

    public LOUDSPPBvTree(int initialNodeCapacity) {
        r0 = new BytesRank0OnlySuccinctBitVector(initialNodeCapacity);
        r1 = new BytesSuccinctBitVector(initialNodeCapacity);
        divider = new BitVector01Divider(r0, r1);
    }

    /**
     * @param divider
     * @param r0      SBV for r0. Only rank0 method of this sbv will be invoked.
     * @param r1
     */
    public LOUDSPPBvTree(BitVector01Divider divider,
                         SuccinctBitVector r0, SuccinctBitVector r1) {
        this.divider = divider;
        this.r0 = r0;
        this.r1 = r1;
        divider.setVectors(r0, r1);
    }

    public LOUDSPPBvTree(
            SuccinctBitVector r0, SuccinctBitVector r1) {
        this.divider = new BitVector01Divider();
        this.r0 = r0;
        this.r1 = r1;
        divider.setVectors(r0, r1);
    }

    public BitVector01Divider getDivider() {
        return divider;
    }

    public SuccinctBitVector getR0() {
        return r0;
    }

    public SuccinctBitVector getR1() {
        return r1;
    }

    @Override
    public String toString() {
        return "r0: " + r0.toString() + "  r1: " + r1.toString();
    }

    @Override
    public void appendChild() {
        divider.append1();
    }

    @Override
    public void appendSelf() {
        divider.append0();
    }

    @Override
    public void getChildNodeIds(int selfNodeId, Range range) {
        if (r0.isZero(selfNodeId)) {
            int start = r1.select0(r0.rank0(selfNodeId)) + 1;
            range.set(start, r1.next0(start) + 1);
            return;
        }
        range.set(-1, -1);
    }

    @Override
    public void trimToSize() {
        r0.trimToSize();
        r1.trimToSize();
    }

    @Override
    public void readExternal(ObjectInput in)
            throws ClassNotFoundException, IOException {
        divider.readExternal(in);
        r0 = (SuccinctBitVector) in.readObject();
        r1 = (SuccinctBitVector) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        divider.writeExternal(out);
        out.writeObject(r0);
        out.writeObject(r1);
    }

    private BitVector01Divider divider;
    private SuccinctBitVector r0;
    private SuccinctBitVector r1;
}

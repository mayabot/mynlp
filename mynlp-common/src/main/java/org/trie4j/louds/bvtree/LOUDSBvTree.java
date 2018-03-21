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

import org.trie4j.bv.BytesSuccinctBitVector;
import org.trie4j.bv.SuccinctBitVector;
import org.trie4j.util.Range;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class LOUDSBvTree implements Externalizable, BvTree {
    public LOUDSBvTree() {
        this(0);
    }

    public LOUDSBvTree(int initialNodeCapacity) {
        sbv = new BytesSuccinctBitVector(initialNodeCapacity * 2);
    }

    public LOUDSBvTree(SuccinctBitVector sbv) {
        this.sbv = sbv;
    }

    public SuccinctBitVector getSbv() {
        return sbv;
    }

    @Override
    public String toString() {
        String bvs = sbv.toString();
        return "bitvec: " + ((bvs.length() > 100) ? bvs.substring(0, 100) : bvs);
    }

    @Override
    public void appendChild() {
        sbv.append1();
    }

    @Override
    public void appendSelf() {
        sbv.append0();
    }

    @Override
    public void getChildNodeIds(int selfNodeId, Range range) {
        int s = sbv.select0(selfNodeId) + 1;
        int e = sbv.next0(s);
        int startNodeId = sbv.rank1(s);
        range.set(startNodeId, startNodeId + e - s);
    }

    @Override
    public void trimToSize() {
        sbv.trimToSize();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        sbv = (SuccinctBitVector) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(sbv);
    }

    private SuccinctBitVector sbv;

    public static void main(String[] args) {
        LOUDSBvTree tree = new LOUDSBvTree();
        tree.appendSelf();
        tree.appendChild();
        tree.appendChild();
        tree.appendSelf();
        System.out.println(tree.toString());
    }
}

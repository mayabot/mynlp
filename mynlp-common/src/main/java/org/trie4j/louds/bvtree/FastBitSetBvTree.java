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
package org.trie4j.louds.bvtree;

import org.trie4j.util.FastBitSet;
import org.trie4j.util.Range;

public class FastBitSetBvTree implements BvTree {
    public FastBitSetBvTree(int initialCapacity) {
        bs = new FastBitSet(initialCapacity);
    }

    public int getSize() {
        return size;
    }

    public byte[] getBytes() {
        return bs.getBytes();
    }

    @Override
    public void appendChild() {
        bs.set(size++);
    }

    @Override
    public void appendSelf() {
        bs.ensureCapacity(size++);
    }

    @Override
    public void getChildNodeIds(int selfNodeId, Range range) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void trimToSize() {
    }

    private FastBitSet bs;
    private int size;

//	public static void main(String[] args) {
//		FastBitSetBvTree bv = new FastBitSetBvTree(8);
//		bv.appendChild();
//		bv.appendChild();
//		bv.appendChild();
//		System.out.println(bv.bs);
//
//	}
}

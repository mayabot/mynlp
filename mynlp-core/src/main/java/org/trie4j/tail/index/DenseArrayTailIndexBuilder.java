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

import org.trie4j.util.FastBitSet;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

public class DenseArrayTailIndexBuilder
        implements Externalizable, TailIndexBuilder {
    public DenseArrayTailIndexBuilder(int initialCapacity) {
        tail = new int[initialCapacity];
    }

    public DenseArrayTailIndexBuilder() {
    }

    @Override
    public void add(int nodeId, int start, int end) {
        if (nodeId != current) {
            throw new IllegalArgumentException("nodeId must be a monoinc.");
        }
        ensureCapacity();
        tail[currentIndex++] = start;
        bs.set(current++);
    }

    @Override
    public void addEmpty(int nodeId) {
        if (nodeId != current) {
            throw new IllegalArgumentException("nodeId must be a monoinc.");
        }
        bs.unsetIfLE(current++);
    }

    @Override
    public void trimToSize() {
        tail = Arrays.copyOf(tail, currentIndex);
        bs.trimToSize();
    }

    @Override
    public TailIndex build() {
        trimToSize();
        return new DenseArrayTailIndex(tail, bs.getBytes(), bs.size());
    }

    private void ensureCapacity() {
        if (currentIndex == tail.length) {
            tail = Arrays.copyOf(tail, (int) ((current + 1) * 1.2));
        }
    }

    @Override
    public void readExternal(ObjectInput in)
            throws ClassNotFoundException, IOException {
        current = in.readInt();
        currentIndex = in.readInt();
        tail = (int[]) in.readObject();
        bs = (FastBitSet) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        trimToSize();
        out.writeInt(current);
        out.writeInt(currentIndex);
        out.writeObject(tail);
        out.writeObject(bs);
    }

    private FastBitSet bs = new FastBitSet();
    private int[] tail = {};
    private int current;
    private int currentIndex;
}

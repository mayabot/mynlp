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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

public class ArrayTailIndexBuilder
        implements Externalizable, TailIndexBuilder {
    public ArrayTailIndexBuilder() {
    }

    public ArrayTailIndexBuilder(int initialCapacity) {
        tail = new int[initialCapacity];
        Arrays.fill(tail, -1);
    }

    @Override
    public void add(int nodeId, int start, int end) {
        ensureCapacity(nodeId);
        tail[nodeId] = start;
    }

    @Override
    public void addEmpty(int nodeId) {
        ensureCapacity(nodeId);
        tail[nodeId] = -1;
    }

    @Override
    public TailIndex build() {
        trimToSize();
        return new ArrayTailIndex(tail);
    }

    @Override
    public void trimToSize() {
        tail = Arrays.copyOf(tail, size);
    }

    private void ensureCapacity(int nodeId) {
        if (nodeId < size) {
            return;
        }
        if (nodeId >= tail.length) {
            tail = Arrays.copyOf(tail, (int) ((nodeId + 1) * 1.2));
            Arrays.fill(tail, size, tail.length, -1);
        }
        size = nodeId + 1;
    }

    @Override
    public void readExternal(ObjectInput in)
            throws ClassNotFoundException, IOException {
        size = in.readInt();
        tail = (int[]) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        trimToSize();
        out.writeInt(size);
        out.writeObject(tail);
    }

    private int[] tail = new int[]{};
    private int size;
}

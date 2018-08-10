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

import org.trie4j.util.FastBitSet;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SBVTailIndexBuilder
        implements Externalizable, TailIndexBuilder {
    public SBVTailIndexBuilder() {
        bs = new FastBitSet();
    }

    public SBVTailIndexBuilder(int initialCapacity) {
        bs = new FastBitSet(initialCapacity);
    }

    @Override
    public void add(int nodeId, int start, int end) {
        if (nodeId != current) {
            throw new IllegalArgumentException("nodeId must be a strictly increasing.");
        }
        int index = bs.size();
        for (int i = start; i < end; i++) {
            bs.set(index++);
        }
        bs.unsetIfLE(index);
        current++;
    }

    @Override
    public void addEmpty(int nodeId) {
        if (nodeId != current) {
            throw new IllegalArgumentException("nodeId must be a strictly increasing.");
        }
        bs.unsetIfLE(bs.size());
        current++;
    }

    @Override
    public void trimToSize() {
        bs.trimToSize();
    }

    @Override
    public TailIndex build() {
        return new SBVTailIndex(bs.getBytes(), bs.size(), current);
    }

    @Override
    public void readExternal(ObjectInput in)
            throws ClassNotFoundException, IOException {
        bs = (FastBitSet) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(bs);
    }

    private int current;
    private FastBitSet bs;
    private static final long serialVersionUID = 8843853578097509573L;
}

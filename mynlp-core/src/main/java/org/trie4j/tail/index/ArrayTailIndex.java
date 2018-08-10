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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ArrayTailIndex
        implements Externalizable, TailIndex {
    public ArrayTailIndex() {
    }

    public ArrayTailIndex(int[] indexes) {
        this.indexes = indexes;
    }

    @Override
    public int size() {
        return indexes.length;
    }

    public int[] getIndexes() {
        return indexes;
    }

    @Override
    public int get(int nodeId) {
        return indexes[nodeId];
    }

    @Override
    public void readExternal(ObjectInput in)
            throws ClassNotFoundException, IOException {
        indexes = (int[]) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(indexes);
    }

    private int[] indexes = new int[]{};
}

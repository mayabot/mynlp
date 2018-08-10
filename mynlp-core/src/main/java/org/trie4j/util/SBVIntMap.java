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
package org.trie4j.util;

import org.trie4j.bv.BytesRank0OnlySuccinctBitVector;

import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class SBVIntMap<T> implements Serializable {
    public SBVIntMap() {
        values = new Object[]{};
        valueIndices = new BytesRank0OnlySuccinctBitVector();
    }

    public SBVIntMap(int initialCapacity) {
        values = new Object[]{};
        valueIndices = new BytesRank0OnlySuccinctBitVector(initialCapacity);
    }

    public int size() {
        return valueIndices.size();
    }

    public int valuesSize() {
        return current;
    }

    public int addValue(T value) {
        valueIndices.append0();
        if (current >= values.length) {
            values = Arrays.copyOf(values, (int) (values.length * 1.2 + 1));
        }
        values[current] = value;
        current++;
        return valueIndices.size();
    }

    public int addNone() {
        valueIndices.append1();
        return valueIndices.size();
    }

    @SuppressWarnings("unchecked")
    public T get(int id) {
        if (!valueIndices.isZero(id)) {
            throw new NoSuchElementException("No element exists at " + id);
        }
        return (T) values[valueIndices.rank0(id) - 1];
    }

    @SuppressWarnings("unchecked")
    public T getUnsafe(int id) {
        return (T) values[valueIndices.rank0(id) - 1];
    }

    public void set(int id, T value) {
        if (!valueIndices.isZero(id)) throw new IllegalStateException(
                "try to set value for invalid id.");
        values[valueIndices.rank0(id) - 1] = value;
    }

    private int current;
    private Object[] values;
    private BytesRank0OnlySuccinctBitVector valueIndices = new BytesRank0OnlySuccinctBitVector();
    private static final long serialVersionUID = -4753279563025571408L;
}

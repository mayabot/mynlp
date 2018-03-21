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
package org.trie4j.util;

import java.io.Serializable;
import java.util.Arrays;

public class FastBitSet implements Serializable, BitSet {
    public FastBitSet() {
    }

    public FastBitSet(int bitSize) {
        bytes = new byte[bitSize / 8 + 1];
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        int n = Math.min(size, 32);
        for (int i = 0; i < n; i++) {
            b.append((bytes[(i / 8)] & (0x80 >> (i % 8))) != 0 ? "1" : "0");
        }
        return b.toString();
    }

    @Override
    public int size() {
        return size;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public boolean get(int index) {
        return (bytes[index / 8] & (0x80 >> (index % 8))) != 0;
    }

    public void set(int index) {
        if (size <= index) {
            if (index / 8 >= bytes.length) {
                extend(index);
            }
            size = index + 1;
        }
        bytes[index / 8] |= 0x80 >> (index % 8);
    }

    public void unset(int index) {
        if (size <= index) {
            if (index / 8 >= bytes.length) {
                extend(index);
            }
            size = index + 1;
        }
        bytes[index / 8] &= ~(0x80 >> (index % 8));
    }

    public void unsetIfLE(int index) {
        if (size <= index) {
            if (index / 8 >= bytes.length) {
                extend(index);
            }
            size = index + 1;
        }
    }

    public void ensureCapacity(int index) {
        if ((index / 8) >= bytes.length) {
            extend(index);
        }
    }

    public void trimToSize() {
        int sz = size / 8 + 1;
        if (bytes.length > sz) {
            bytes = Arrays.copyOf(bytes, sz);
        }
    }

    private void extend(int index) {
        bytes = Arrays.copyOf(bytes,
                Math.max(index / 8 + 1, (int) (bytes.length * 1.5))
        );
    }

    private int size;
    private byte[] bytes = {};
    private static final long serialVersionUID = -3346250300546707823L;
}

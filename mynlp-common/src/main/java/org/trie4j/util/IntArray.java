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

import java.io.Serializable;
import java.util.Arrays;

public class IntArray
        implements Serializable {
    public IntArray() {
        this.elements = new int[]{};
    }

    public IntArray(int initialCapacity) {
        this.elements = new int[initialCapacity];
    }

    public IntArray(int[] elements, int size) {
        this.size = size;
        this.elements = Arrays.copyOf(elements, size);
    }

    public int size() {
        return size;
    }

    public void trimToSize() {
        elements = Arrays.copyOf(elements, size);
    }

    public int[] getElements() {
        return elements;
    }

    public int get(int index) {
        return elements[index];
    }

    public void add(int value) {
        if (size == elements.length) {
            elements = Arrays.copyOf(elements, (int) (size * 1.2 + 1));
        }
        elements[size] = value;
        size++;
    }

    public void set(int index, int value) {
        if (index >= elements.length) {
            elements = Arrays.copyOf(elements, Math.max((int) (elements.length * 1.2), index + 1));
        }
        elements[index] = value;
        size = Math.max(size, index + 1);
    }

    private int[] elements;
    private int size;
    private static final long serialVersionUID = 5484622364177160367L;
}

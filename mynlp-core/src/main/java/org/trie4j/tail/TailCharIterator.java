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
package org.trie4j.tail;

import java.util.NoSuchElementException;

public class TailCharIterator {
    public TailCharIterator(CharSequence chars, int index) {
        this.chars = chars;
        this.index = index;
        if (this.index != -1) {
            this.next = chars.charAt(index);
        }
    }

    public void setOffset(int offset) {
        setIndex(offset);
    }

    public void setIndex(int index) {
        this.index = index;
        this.current = '\0';
        if (this.index != -1) {
            this.next = this.chars.charAt(index);
        } else {
            this.next = '\0';
        }
    }

    public int getNextIndex() {
        return index;
    }

    public boolean hasNext() {
        return index != -1;
    }

    public char next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        current = next;
        index++;
        char c = chars.charAt(index);
        if (c == '\0') {
            index = -1;
        } else if (c == '\1') {
            int i = chars.charAt(index + 1);
            i += chars.charAt(index + 2) << 16;
            index = i;
            c = chars.charAt(index);
        }
        next = c;
        return current;
    }

    public char current() {
        return current;
    }

    private CharSequence chars;
    private int index;
    private char current = '\0';
    private char next = '\0';
}

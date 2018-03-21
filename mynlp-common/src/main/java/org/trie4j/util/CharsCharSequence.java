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

import java.util.Arrays;

public class CharsCharSequence implements CharSequence {
    public CharsCharSequence(char[] chars) {
        this(chars, 0, chars.length);
    }

    public CharsCharSequence(char[] chars, int start, int end) {
        if (chars.length < end) throw new ArrayIndexOutOfBoundsException(end);
        this.chars = chars;
        this.start = start;
        this.end = end;
    }

    @Override
    public int length() {
        return end - start;
    }

    @Override
    public char charAt(int index) {
        int i = start + index;
        if (i >= end) throw new ArrayIndexOutOfBoundsException(i);
        return chars[i];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new CharsCharSequence(chars, this.start + start, this.start + end);
    }

    public char[] toCharArray() {
        return Arrays.copyOfRange(chars, start, end);
    }

    public char[] subChars(int start, int end) {
        int e = this.start + end;
        if (e >= end) {
            throw new ArrayIndexOutOfBoundsException(e);
        }
        return Arrays.copyOfRange(chars, this.start + start, this.start + end);
    }

    private char[] chars;
    private int start;
    private int end;
}

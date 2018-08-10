/*
 * Copyright 2013 Takao Nakaguchi
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

public class FastTailCharIterator {
    public FastTailCharIterator(CharSequence chars, int index) {
        this.chars = chars;
        setIndex(index);
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public char getNext() {
        if (index == chars.length()) return '\0';
        char ret = chars.charAt(index);
        if (ret <= '\1') {
            if (ret == '\0') {
                return ret;
            } else {
                int i = chars.charAt(index + 1);
                i += chars.charAt(index + 2) << 16;
                index = i;
                ret = chars.charAt(index);
            }
        }
        index++;
        return ret;
    }

    private CharSequence chars;
    private int index;
}

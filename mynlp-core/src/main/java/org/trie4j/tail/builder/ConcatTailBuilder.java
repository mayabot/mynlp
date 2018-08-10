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
package org.trie4j.tail.builder;

import java.io.Serializable;

public class ConcatTailBuilder
        implements Serializable, TailBuilder {
    public ConcatTailBuilder() {
        tails = new StringBuilder();
    }

    public ConcatTailBuilder(StringBuilder tails) {
        this.tails = tails;
    }

    public CharSequence getTails() {
        return tails;
    }

    @Override
    public int insert(CharSequence letters) {
        return insert(letters, 0, letters.length());
    }

    @Override
    public int insert(CharSequence letters, int offset, int len) {
        int ret = tails.length();
        tails.append(letters, offset, offset + len).append('\0');
        return ret;
    }

    @Override
    public int insert(char[] letters) {
        return insert(letters, 0, letters.length);
    }

    @Override
    public int insert(char[] letters, int offset, int len) {
        int ret = tails.length();
        tails.append(letters, offset, len).append('\0');
        return ret;
    }

    private StringBuilder tails;
    private static final long serialVersionUID = -2965476329952753114L;
}

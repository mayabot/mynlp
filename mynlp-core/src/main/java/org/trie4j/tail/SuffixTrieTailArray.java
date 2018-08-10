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

import org.trie4j.tail.builder.SuffixTrieTailBuilder;
import org.trie4j.tail.builder.TailBuilder;
import org.trie4j.tail.index.ArrayTailIndexBuilder;
import org.trie4j.tail.index.TailIndexBuilder;

public class SuffixTrieTailArray
        extends AbstractTailArrayBuilder
        implements TailArrayBuilder {
    public SuffixTrieTailArray() {
    }

    public SuffixTrieTailArray(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    protected TailBuilder newTailBuilder(StringBuilder tails) {
        return new SuffixTrieTailBuilder(tails);
    }

    @Override
    protected TailIndexBuilder newTailIndexBuilder(int initialCapacity) {
        return new ArrayTailIndexBuilder(initialCapacity);
    }
}

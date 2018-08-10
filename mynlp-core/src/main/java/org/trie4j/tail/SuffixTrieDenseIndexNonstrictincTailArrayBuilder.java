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
package org.trie4j.tail;

import org.trie4j.tail.builder.SuffixTrieTailBuilder;
import org.trie4j.tail.builder.TailBuilder;
import org.trie4j.tail.index.DenseArrayTailIndex;
import org.trie4j.util.ArrayUtil;
import org.trie4j.util.FastBitSet;

import java.util.SortedMap;
import java.util.TreeMap;

public class SuffixTrieDenseIndexNonstrictincTailArrayBuilder implements TailArrayBuilder {

    @Override
    public void append(int nodeId, CharSequence letters, int offset, int len) {
        int index = tailBuilder.insert(letters, offset, len);
        tailBs.set(nodeId);
        indexes.put(nodeId, index);
    }

    @Override
    public void append(int nodeId, char[] letters, int offset, int len) {
        int index = tailBuilder.insert(letters, offset, len);
        tailBs.set(nodeId);
        indexes.put(nodeId, index);
    }

    @Override
    public void appendEmpty(int nodeId) {
        tailBs.unsetIfLE(nodeId);
    }

    @Override
    public void trimToSize() {
    }

    @Override
    public TailArray build() {
        return new DefaultTailArray(
                tailBuilder.getTails(),
                new DenseArrayTailIndex(
                        ArrayUtil.unbox(indexes.values().toArray(new Integer[]{})),
                        tailBs.getBytes(),
                        tailBs.size()
                )
        );
    }

    private TailBuilder tailBuilder = new SuffixTrieTailBuilder();
    private FastBitSet tailBs = new FastBitSet();
    private SortedMap<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
}

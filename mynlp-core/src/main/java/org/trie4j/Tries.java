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
package org.trie4j;

import org.trie4j.bv.LongsRank0OnlySuccinctBitVector;
import org.trie4j.bv.LongsSuccinctBitVector;
import org.trie4j.doublearray.DoubleArray;
import org.trie4j.louds.TailLOUDSTrie;
import org.trie4j.louds.bvtree.LOUDSPPBvTree;
import org.trie4j.patricia.PatriciaTrie;
import org.trie4j.tail.SuffixTrieDenseTailArrayBuilder;

public class Tries {
    public static Trie mutableTrie() {
        return new PatriciaTrie();
    }

    public static Trie fastImmutableTrie(Trie orig) {
        return new DoubleArray(orig);
    }

    public static Trie fastImmutableTermIdTrie(Trie orig) {
        return new DoubleArray(orig);
    }

    public static Trie smallImmutableTrie(Trie orig) {
        return new TailLOUDSTrie(
                orig,
                new LOUDSPPBvTree(
                        new LongsRank0OnlySuccinctBitVector(orig.size() * 2),
                        new LongsSuccinctBitVector(orig.size() * 2)
                ),
                new SuffixTrieDenseTailArrayBuilder()
        );
    }

    public static TermIdTrie smallImmutableTermIdTrie(Trie orig) {
        return new TailLOUDSTrie(
                orig,
                new LOUDSPPBvTree(
                        new LongsRank0OnlySuccinctBitVector(orig.size() * 2),
                        new LongsSuccinctBitVector(orig.size() * 2)
                ),
                new SuffixTrieDenseTailArrayBuilder()
        );
    }
}

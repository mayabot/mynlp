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
package org.trie4j;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.trie4j.bv.BitVectorUtilTest;
import org.trie4j.bv.BytesConstantTimeSelect0SuccinctBitVectorTest;
import org.trie4j.bv.BytesRank0OnlySuccinctBitVectorTest;
import org.trie4j.bv.BytesRank1OnlySuccinctBitVectorTest;
import org.trie4j.bv.BytesSuccinctBitVectorTest;
import org.trie4j.bv.LongsRank1OnlySuccinctBitVectorTest;
import org.trie4j.bv.LongsSuccinctBitVectorTest;
import org.trie4j.bv.UnsafeBytesSuccinctBitVectorTest;
import org.trie4j.doublearray.DoubleArrayTest;
import org.trie4j.doublearray.MapDoubleArrayTest;
import org.trie4j.doublearray.MapTailDoubleArrayWithConcatTailBuilderTest;
import org.trie4j.doublearray.MapTailDoubleArrayWithSuffixTrieTailBuilderTest;
import org.trie4j.doublearray.TailDoubleArrayWithConcatTailBuilderTest;
import org.trie4j.doublearray.TailDoubleArrayWithSuffixTrieTailBuilderTest;
import org.trie4j.doublearray.UnsafeDoubleArrayTest;
import org.trie4j.louds.MapTailLOUDSPPTrieWithConcatTailArrayTest;
import org.trie4j.louds.MapTailLOUDSPPTrieWithSBVConcatTailArrayTest;
import org.trie4j.louds.MapTailLOUDSPPTrieWithSuffixTrieTailArrayTest;
import org.trie4j.louds.MapTailLOUDSTrieWithConcatTailArrayTest;
import org.trie4j.louds.MapTailLOUDSTrieWithSBVConcatTailArrayTest;
import org.trie4j.louds.MapTailLOUDSTrieWithSuffixTrieTailArrayTest;
import org.trie4j.louds.TailLOUDSPPTrieWithConcatTailArrayTest;
import org.trie4j.louds.TailLOUDSPPTrieWithSBVConcatTailArrayTest;
import org.trie4j.louds.TailLOUDSPPTrieWithSuffixTrieTailArrayTest;
import org.trie4j.louds.TailLOUDSTrieWithConcatTailArrayTest;
import org.trie4j.louds.TailLOUDSTrieWithSBVConcatTailArrayTest;
import org.trie4j.louds.TailLOUDSTrieWithSuffixTrieTailArrayTest;
import org.trie4j.patricia.MapPatriciaTrieTest;
import org.trie4j.patricia.MapTailPatriciaTrieWithConcatTailBuilderTest;
import org.trie4j.patricia.MapTailPatriciaTrieWithSuffixTrieTailBuilderTest;
import org.trie4j.patricia.PatriciaTrieTest;
import org.trie4j.patricia.TailPatriciaTrieWithConcatTailBuilderTest;
import org.trie4j.patricia.TailPatriciaTrieWithSuffixTrieTailBuilderTest;
import org.trie4j.tail.ConcatTailArrayTest;
import org.trie4j.tail.builder.SuffixTrieTailBuilderTest;
import org.trie4j.tail.index.ArrayTailIndexTest;
import org.trie4j.tail.index.DenseArrayTailIndexTest;
import org.trie4j.tail.index.SBVTailIndexTest;
import org.trie4j.util.CharsCharSequenceTest;
import org.trie4j.util.FastBitSetTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	AlgorithmsTest.class,
	BitVectorUtilTest.class,
	ConcatTailArrayTest.class,
	CharsCharSequenceTest.class,
	FastBitSetTest.class,
	BytesSuccinctBitVectorTest.class,
	UnsafeBytesSuccinctBitVectorTest.class,
	BytesConstantTimeSelect0SuccinctBitVectorTest.class,
	BytesRank0OnlySuccinctBitVectorTest.class,
	BytesRank1OnlySuccinctBitVectorTest.class,
	LongsSuccinctBitVectorTest.class,
	LongsRank1OnlySuccinctBitVectorTest.class,
	ArrayTailIndexTest.class,
	DenseArrayTailIndexTest.class,
	SBVTailIndexTest.class,
	SuffixTrieTailBuilderTest.class,
	PatriciaTrieTest.class,
	TailPatriciaTrieWithConcatTailBuilderTest.class,
	TailPatriciaTrieWithSuffixTrieTailBuilderTest.class,
	DoubleArrayTest.class,
	UnsafeDoubleArrayTest.class,
	TailDoubleArrayWithConcatTailBuilderTest.class,
	TailDoubleArrayWithSuffixTrieTailBuilderTest.class,
	TailLOUDSTrieWithConcatTailArrayTest.class,
	TailLOUDSTrieWithSBVConcatTailArrayTest.class,
	TailLOUDSTrieWithSuffixTrieTailArrayTest.class,
	TailLOUDSPPTrieWithConcatTailArrayTest.class,
	TailLOUDSPPTrieWithSBVConcatTailArrayTest.class,
	TailLOUDSPPTrieWithSuffixTrieTailArrayTest.class,
	MapPatriciaTrieTest.class,
	MapTailPatriciaTrieWithConcatTailBuilderTest.class,
	MapTailPatriciaTrieWithSuffixTrieTailBuilderTest.class,
	MapDoubleArrayTest.class,
	MapTailDoubleArrayWithConcatTailBuilderTest.class,
	MapTailDoubleArrayWithSuffixTrieTailBuilderTest.class,
	MapTailLOUDSTrieWithConcatTailArrayTest.class,
	MapTailLOUDSTrieWithSBVConcatTailArrayTest.class,
	MapTailLOUDSTrieWithSuffixTrieTailArrayTest.class,
	MapTailLOUDSPPTrieWithConcatTailArrayTest.class,
	MapTailLOUDSPPTrieWithSBVConcatTailArrayTest.class,
	MapTailLOUDSPPTrieWithSuffixTrieTailArrayTest.class,
})
public class AllTests {
}

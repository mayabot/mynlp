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
package org.trie4j.bv;

import org.junit.Assert;
import org.junit.Test;
import org.trie4j.util.FastBitSet;

public class LongsRank1OnlySuccinctBitVectorTest {//extends AbstractSuccinctBitVectorTest{
	protected SuccinctBitVector create(){
		return new LongsRank0OnlySuccinctBitVector();
	}

	protected SuccinctBitVector create(int initialCapacity){
		return new LongsRank0OnlySuccinctBitVector(initialCapacity);
	}

	protected SuccinctBitVector create(byte[] bytes, int bitsSize){
		return new LongsRank0OnlySuccinctBitVector(bytes, bitsSize);
	}
	
	@Test
	public void test_1() throws Exception{
		FastBitSet bs = new FastBitSet();
		bs.set(11);
		bs.set(22);
		bs.set(28);
		bs.set(34);
		bs.set(67);
		LongsRank0OnlySuccinctBitVector bv = new LongsRank0OnlySuccinctBitVector(
				bs.getBytes(), bs.size());
		Assert.assertEquals(1, bv.rank1(11));
		Assert.assertEquals(2, bv.rank1(22));
		Assert.assertEquals(3, bv.rank1(28));
		Assert.assertEquals(4, bv.rank1(34));
		Assert.assertEquals(5, bv.rank1(67));
	}

	@Test
	public void test_2() throws Exception{
		LongsRank0OnlySuccinctBitVector bv = new LongsRank0OnlySuccinctBitVector(
				new byte[]{127, -12, -102, -1, -6, 95, -1, -33, -128},
				65
				);
		Assert.assertEquals(52, bv.rank1(64));
	}

	@Test
	public void test_3() throws Exception{
		LongsRank0OnlySuccinctBitVector bv = new LongsRank0OnlySuccinctBitVector();
		for(int i = 0; i < 9; i++){
			bv.append1();
		}
		Assert.assertEquals(9, bv.rank1(8));
	}

	@Test
	public void test_from_bytes_rank_1() throws Exception{
		SuccinctBitVector sbv = new LongsRank0OnlySuccinctBitVector(
				new byte[]{0x01, 0x1f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0x80},
				68
				);
		Assert.assertEquals(0, sbv.rank1(4));
		Assert.assertEquals(1, sbv.rank1(8));
		Assert.assertEquals(6, sbv.rank1(15));
		Assert.assertEquals(7, sbv.rank1(64));
		Assert.assertEquals(1, sbv.rank0(0));
		Assert.assertEquals(11, sbv.rank0(16));
		Assert.assertEquals(65, sbv.rank0(71));
	}
}

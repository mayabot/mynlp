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
package org.trie4j.bv;

import org.junit.Assert;
import org.junit.Test;

public class LongsConstantTimeSelect0SuccinctBitVectorTest extends AbstractSuccinctBitVectorTest{
	protected SuccinctBitVector create(){
		return new LongsConstantTimeSelect0SuccinctBitVector();
	}

	protected SuccinctBitVector create(int initialCapacity){
		return new LongsConstantTimeSelect0SuccinctBitVector(initialCapacity);
	}

	protected SuccinctBitVector create(byte[] bytes, int bitsSize){
		return new LongsConstantTimeSelect0SuccinctBitVector(bytes, bitsSize);
	}

	@Test
	public void test_longsSize() throws Exception{
		Assert.assertEquals(0, new LongsConstantTimeSelect0SuccinctBitVector(0).getLongs().length);
		Assert.assertEquals(1, new LongsConstantTimeSelect0SuccinctBitVector(1).getLongs().length);
		Assert.assertEquals(1, new LongsConstantTimeSelect0SuccinctBitVector(64).getLongs().length);
		Assert.assertEquals(2, new LongsConstantTimeSelect0SuccinctBitVector(65).getLongs().length);
	}

	@Test
	public void test_count0CacheSize() throws Exception{
		Assert.assertEquals(0, new LongsConstantTimeSelect0SuccinctBitVector(0).getCountCache0().length);
		Assert.assertEquals(1, new LongsConstantTimeSelect0SuccinctBitVector(1).getCountCache0().length);
		Assert.assertEquals(1, new LongsConstantTimeSelect0SuccinctBitVector(64).getCountCache0().length);
		Assert.assertEquals(2, new LongsConstantTimeSelect0SuccinctBitVector(
				LongsConstantTimeSelect0SuccinctBitVector.BITS_IN_COUNTCACHE0 + 1).getCountCache0().length);
	}

	@Test
	public void test_append_countCache0_1() throws Exception{
		LongsConstantTimeSelect0SuccinctBitVector sbv = new LongsConstantTimeSelect0SuccinctBitVector();
		for(int i = 0; i < LongsConstantTimeSelect0SuccinctBitVector.BITS_IN_COUNTCACHE0; i++){
			sbv.append0();
			Assert.assertEquals(i + 1, sbv.getCountCache0()[0]);
		}
		for(int i = 0; i < LongsConstantTimeSelect0SuccinctBitVector.BITS_IN_COUNTCACHE0; i++){
			sbv.append0();
			Assert.assertEquals(LongsConstantTimeSelect0SuccinctBitVector.BITS_IN_COUNTCACHE0, sbv.getCountCache0()[0]);
			Assert.assertEquals(LongsConstantTimeSelect0SuccinctBitVector.BITS_IN_COUNTCACHE0 + i + 1, sbv.getCountCache0()[1]);
		}
	}

	@Test
	public void test_append_countCache0_2() throws Exception{
		LongsConstantTimeSelect0SuccinctBitVector sbv = new LongsConstantTimeSelect0SuccinctBitVector();
		for(int i = 0; i < 10000; i++){
			sbv.append0();
			Assert.assertEquals(i + 1, sbv.getCountCache0()[i / LongsConstantTimeSelect0SuccinctBitVector.BITS_IN_COUNTCACHE0]);
		}
	}

	@Test
	public void test_append_countCache0_3() throws Exception{
		LongsConstantTimeSelect0SuccinctBitVector sbv = new LongsConstantTimeSelect0SuccinctBitVector();
		for(int i = 0; i < 10000; i++){
			sbv.append0();
			sbv.append1();
			String msg = i + "th";
			Assert.assertEquals(msg, i + 1, sbv.getCountCache0()[i * 2 / LongsConstantTimeSelect0SuccinctBitVector.BITS_IN_COUNTCACHE0]);
		}
	}
}

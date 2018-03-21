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

public class BytesRank0OnlySuccinctBitVectorTest {
	@Test
	public void test() throws Exception{
		BytesRank0OnlySuccinctBitVector bv = new BytesRank0OnlySuccinctBitVector(1);
		bv.append0();
		bv.append1();
		Assert.assertEquals(1, bv.rank0(0));
		Assert.assertEquals(1, bv.rank0(1));
		Assert.assertEquals(2, bv.rank0(2));
		Assert.assertEquals(3, bv.rank0(3));
		Assert.assertEquals(4, bv.rank0(4));
		Assert.assertEquals(5, bv.rank0(5));
		Assert.assertEquals(6, bv.rank0(6));
		Assert.assertEquals(7, bv.rank0(7));
		try{
			bv.rank0(8);
			Assert.fail();
		} catch(ArrayIndexOutOfBoundsException e){
		}
	}

	@Test
	public void test2() throws Exception{
		BytesRank0OnlySuccinctBitVector bv = new BytesRank0OnlySuccinctBitVector(
				new byte[]{0x2d, 0x3f}, 16);
		Assert.assertEquals(1, bv.rank0(0));
		Assert.assertEquals(2, bv.rank0(1));
		Assert.assertEquals(2, bv.rank0(2));
		Assert.assertEquals(3, bv.rank0(3));
		Assert.assertEquals(3, bv.rank0(4));
		Assert.assertEquals(3, bv.rank0(5));
		Assert.assertEquals(4, bv.rank0(6));
		Assert.assertEquals(4, bv.rank0(7));
		Assert.assertEquals(5, bv.rank0(8));
		Assert.assertEquals(6, bv.rank0(9));
		Assert.assertEquals(6, bv.rank0(10));
		Assert.assertEquals(6, bv.rank0(11));
		Assert.assertEquals(6, bv.rank0(12));
		Assert.assertEquals(6, bv.rank0(13));
		Assert.assertEquals(6, bv.rank0(14));
		Assert.assertEquals(6, bv.rank0(15));
		try{
			bv.rank0(16);
			Assert.fail();
		} catch(ArrayIndexOutOfBoundsException e){
		}
	}
}

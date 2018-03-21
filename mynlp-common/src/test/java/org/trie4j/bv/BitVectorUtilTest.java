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

public class BitVectorUtilTest {
	@Test
	public void test() throws Exception{
		BytesSuccinctBitVector r = new BytesSuccinctBitVector();
		// build tree on r
		r.append1(); r.append0(); // super root
		r.append1(); r.append1(); r.append1(); r.append0(); // 0
		r.append1(); r.append1(); r.append1(); r.append0(); // 1
		r.append0(); // 2
		r.append1(); r.append1(); r.append0(); // 3
		r.append0(); // 4
		r.append0(); // 5
		r.append0(); // 6
		r.append1(); r.append0(); // 7
		r.append0(); // 8
		r.append0(); // 9

		// divide to two sbv
		BytesSuccinctBitVector r0 = new BytesSuccinctBitVector();
		BytesSuccinctBitVector r1 = new BytesSuccinctBitVector();
		r0.append0();
		r1.append0();
		BitVectorUtil.divide01(r, r0, r1);
		Assert.assertEquals("101110111001100001000", r.toString());
		Assert.assertEquals("00010111011", r0.toString());
		Assert.assertEquals("00110110100", r1.toString());
//		Assert.assertEquals("1101000100", r0.toString());
//		Assert.assertEquals("1001001011", r1.toString());
		// find first child
		Assert.assertEquals(1, r1.select0(r0.rank0(0)) + 1);
		Assert.assertEquals(5, r1.select0(r0.rank0(2)) + 1);
		Assert.assertEquals(8, r1.select0(r0.rank0(4)) + 1);
		Assert.assertEquals(10, r1.select0(r0.rank0(8)) + 1);
	}
}

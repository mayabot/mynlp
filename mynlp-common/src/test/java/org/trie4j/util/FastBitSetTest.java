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
package org.trie4j.util;

import org.junit.Assert;

import org.junit.Test;

public class FastBitSetTest {
	@Test
	public void test_set_1() throws Exception{
		FastBitSet bs = new FastBitSet();
		for(int i = 0; i < 1000; i++){
			bs.set(i);
			Assert.assertTrue(bs.get(i));
		}
	}

	@Test
	public void test_set_2() throws Exception{
		FastBitSet bs = new FastBitSet();
		for(int i = 0; i < 8; i++){
			int index = (int)Math.pow(10, i);
			bs.set(index);
			Assert.assertTrue(bs.get(index));
		}
	}

	@Test
	public void test_set_3() throws Exception{
		FastBitSet bs = new FastBitSet();
		bs.set(8);
		Assert.assertTrue(bs.get(8));
		Assert.assertEquals(9, bs.size());
	}

	@Test
	public void test_unset_1() throws Exception{
		FastBitSet bs = new FastBitSet();
		for(int i = 0; i < 1000; i++){
			bs.set(i);
			bs.unset(i);
			Assert.assertFalse(bs.get(i));
		}
	}

	@Test
	public void test_unset_2() throws Exception{
		FastBitSet bs = new FastBitSet();
		for(int i = 0; i < 8; i++){
			int index = (int)Math.pow(10, i);
			bs.set(index);
			bs.unset(index);
			Assert.assertFalse(bs.get(index));
		}
	}

	@Test
	public void test_unsetIfLE_1() throws Exception{
		FastBitSet bs = new FastBitSet();
		bs.unsetIfLE(0);
		Assert.assertEquals(1, bs.size());
		Assert.assertFalse(bs.get(0));
	}

	@Test
	public void test_unsetIfLE_2() throws Exception{
		FastBitSet bs = new FastBitSet();
		bs.set(0);
		bs.set(7);
		bs.unsetIfLE(8);
		Assert.assertEquals(9, bs.size());
		Assert.assertFalse(bs.get(8));
	}
}

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
package org.trie4j.util;

import org.junit.Assert;
import org.junit.Test;

public class CharsCharSequenceTest {
	@Test
	public void test_1() throws Exception{
		CharSequence seq = new CharsCharSequence("hello".toCharArray());
		Assert.assertEquals('h', seq.charAt(0));
		Assert.assertEquals('e', seq.charAt(1));
		Assert.assertEquals('l', seq.charAt(2));
		Assert.assertEquals('l', seq.charAt(3));
		Assert.assertEquals('o', seq.charAt(4));
		try{
			seq.charAt(5);
			Assert.fail();
		} catch(ArrayIndexOutOfBoundsException e){
		}
	}

	@Test
	public void test_2() throws Exception{
		CharSequence seq = new CharsCharSequence("hello".toCharArray(), 1, 3);
		Assert.assertEquals('e', seq.charAt(0));
		Assert.assertEquals('l', seq.charAt(1));
		try{
			seq.charAt(2);
			Assert.fail();
		} catch(ArrayIndexOutOfBoundsException e){
		}
	}

	@Test
	public void test_3() throws Exception{
		CharsCharSequence seq = new CharsCharSequence("hello".toCharArray(), 1, 3);
		char[] a = seq.toCharArray();
		Assert.assertEquals(2, a.length);
		Assert.assertEquals('e', a[0]);
		Assert.assertEquals('l', a[1]);
	}

	@Test
	public void test_4() throws Exception{
		CharSequence seq = new CharsCharSequence("hello".toCharArray(), 1, 5);
		CharSequence a = seq.subSequence(0, 2);
		Assert.assertEquals('e', a.charAt(0));
		Assert.assertEquals('l', a.charAt(1));
		try{
			a.charAt(2);
			Assert.fail();
		} catch(ArrayIndexOutOfBoundsException e){
		}
	}
}

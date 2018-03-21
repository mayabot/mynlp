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

import org.junit.Assert;
import org.junit.Test;

public class FastTailCharIteratorTest {
	@Test
	public void test_3() throws Exception{
		String chars = "abc\1\6\0def\0";
		FastTailCharIterator it = new FastTailCharIterator(chars, 0);
		Assert.assertEquals('a', it.getNext());
		Assert.assertEquals('b', it.getNext());
		Assert.assertEquals('c', it.getNext());
		Assert.assertEquals('d', it.getNext());
		Assert.assertEquals('e', it.getNext());
		Assert.assertEquals('f', it.getNext());
		Assert.assertEquals('\0', it.getNext());
	}
}

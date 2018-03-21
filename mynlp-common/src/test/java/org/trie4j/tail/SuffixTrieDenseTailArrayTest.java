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

import org.junit.Assert;
import org.junit.Test;

public class SuffixTrieDenseTailArrayTest {
	@Test
	public void test_tailtrie_1() throws Exception{
		TailArrayBuilder tab = new SuffixTrieDenseTailArrayBuilder(0);
		tab.append(0, "hello", 0, 5);
		tab.appendEmpty(1);
		tab.appendEmpty(2);
		tab.append(3, "llo", 0, 3);
		tab.appendEmpty(4);
		TailArray ta = tab.build();
		TailCharIterator it = ta.newIterator();
		it.setOffset(ta.getIteratorOffset(0));
		Assert.assertEquals("hello", TailUtil.readAll(it));
		it.setOffset(ta.getIteratorOffset(3));
		Assert.assertEquals("llo", TailUtil.readAll(it));
	}
}

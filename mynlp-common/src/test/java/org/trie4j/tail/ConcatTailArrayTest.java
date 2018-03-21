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

public class ConcatTailArrayTest {
	@Test
	public void test_tailtrie_1() throws Exception{
		TailArrayBuilder tab = new ConcatTailArrayBuilder(0);
		tab.append(0, "hello", 0, 5);
		tab.append(1, "mello", 0, 5);
		TailArray ta = tab.build();

		{
			TailCharIterator it2 = ta.newIterator();
			it2.setOffset(ta.getIteratorOffset(2));
			while (it2.hasNext()) {
				System.out.println(it2.next());
			}
		}
		TailCharIterator it = ta.newIterator();
		it.setOffset(ta.getIteratorOffset(0));
		Assert.assertEquals("hello", TailUtil.readAll(it));
		it.setOffset(ta.getIteratorOffset(1));
		Assert.assertEquals("mello", TailUtil.readAll(it));
	}
}

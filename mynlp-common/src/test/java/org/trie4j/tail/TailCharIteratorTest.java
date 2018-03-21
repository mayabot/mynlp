package org.trie4j.tail;

import org.junit.Assert;
import org.junit.Test;

public class TailCharIteratorTest {
	@Test
	public void test_1() throws Exception{
		String chars = "abc\0";
		TailCharIterator it = new TailCharIterator(chars, 0);
		Assert.assertEquals('a', it.next());
		Assert.assertEquals('b', it.next());
		Assert.assertEquals('c', it.next());
		Assert.assertFalse(it.hasNext());
	}
}

package org.trie4j.test;

import org.junit.Assert;
import org.junit.Test;

public class WikipediaTitlesTest {
	@Test
	public void test() throws Exception{
		Iterable<String> itb = new WikipediaTitles("jawiki-20120220-all-titles-in-ns0.gz");
		int len = 0;
		for(String w : itb){
			len += w.length();
		}
		itb = null;
		System.out.println(len);
		Assert.assertTrue(len > 100000);
	}
}

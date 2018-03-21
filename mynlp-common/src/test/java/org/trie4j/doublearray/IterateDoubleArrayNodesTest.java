package org.trie4j.doublearray;

import org.junit.Assert;
import org.junit.Test;
import org.trie4j.Algorithms;
import org.trie4j.Node;
import org.trie4j.NodeVisitor;
import org.trie4j.Trie;
import org.trie4j.patricia.PatriciaTrie;
import org.trie4j.patricia.TailPatriciaTrie;
import org.trie4j.test.WikipediaTitles;

public class IterateDoubleArrayNodesTest {
	private static class CountingVisitor implements NodeVisitor{
		public int getCount() {
			return c;
		}
		@Override
		public boolean visit(Node node, int nest) {
			c++;
			if(c % 10000 == 0){
				System.out.println(c + ": " + node.getLetters()[0]);
			}
			return true;
		}
		private int c;
	}

	@Test
	public void test() throws Exception{
		Trie t = new TailPatriciaTrie();
		for(String w : new String[]{"hello", "world", "java", "hell", "he"}){
			t.insert(w);
		}
		DoubleArray da = new DoubleArray(t);
		Assert.assertTrue(da.contains("hello"));
		Assert.assertEquals(15, da.nodeSize());
		CountingVisitor c = new CountingVisitor();
		Algorithms.traverseByBreadth(da.getRoot(), c);
		Assert.assertEquals(15, c.getCount());
	}

//	@Test
	public void test_withWikipediaTitles() throws Exception{
		DoubleArray da = new DoubleArray(new WikipediaTitles().insertTo(new PatriciaTrie()));
		System.out.println(da.nodeSize());
		Algorithms.traverseByBreadth(
				da.getRoot(),
				new CountingVisitor());
	}
}

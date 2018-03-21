package org.trie4j.louds;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.trie4j.AbstractTermIdTrieTest;
import org.trie4j.Node;
import org.trie4j.Trie;
import org.trie4j.bv.LongsSuccinctBitVector;
import org.trie4j.louds.bvtree.LOUDSBvTree;
import org.trie4j.tail.ConcatTailArrayBuilder;

public class LongsTailLOUDSTrieWithConcatTailArrayTest
extends AbstractTermIdTrieTest<TailLOUDSTrie>{
	@Override
	protected TailLOUDSTrie buildSecond(Trie firstTrie) {
		return new TailLOUDSTrie(
				firstTrie,
				new LOUDSBvTree(new LongsSuccinctBitVector(firstTrie.nodeSize() * 2)),
				new ConcatTailArrayBuilder(firstTrie.size() * 4));
	}

	@Test
	public void test() throws Exception{
		String[] words = {"こんにちは", "さようなら", "おはよう", "おおきなかぶ", "おおやまざき"};
		Trie lt = trieWithWords(words);
		for(String w : words){
			Assert.assertTrue(w, lt.contains(w));
		}
		Assert.assertFalse(lt.contains("おやすみなさい"));

		StringBuilder b = new StringBuilder();
		Node[] children = lt.getRoot().getChildren();
		for(Node n : children){
			char[] letters = n.getLetters();
			b.append(letters[0]);
		}
		Assert.assertEquals("おこさ", b.toString());
	}

	@Test
	public void test_save_load() throws Exception{
		String[] words = {"こんにちは", "さようなら", "おはよう", "おおきなかぶ", "おおやまざき"};
		TailLOUDSTrie lt = trieWithWords(words);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		lt.writeExternal(oos);
		oos.flush();
		lt = new TailLOUDSTrie();
		lt.readExternal(new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())));
		for(String w : words){
			Assert.assertTrue(lt.contains(w));
		}
		Assert.assertFalse(lt.contains("おやすみなさい"));

		StringBuilder b = new StringBuilder();
		Node[] children = lt.getRoot().getChildren();
		for(Node n : children){
			char[] letters = n.getLetters();
			b.append(letters[0]);
		}
		Assert.assertEquals("おこさ", b.toString());
	}
}

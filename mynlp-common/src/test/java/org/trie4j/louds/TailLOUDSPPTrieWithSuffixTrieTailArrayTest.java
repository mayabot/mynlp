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
import org.trie4j.louds.bvtree.LOUDSPPBvTree;
import org.trie4j.tail.SuffixTrieTailArray;

public class TailLOUDSPPTrieWithSuffixTrieTailArrayTest
extends AbstractTermIdTrieTest<TailLOUDSTrie>{
	@Override
	protected TailLOUDSTrie buildSecond(Trie firstTrie) {
		return new TailLOUDSTrie(
				firstTrie,
				new LOUDSPPBvTree(firstTrie.nodeSize()),
				new SuffixTrieTailArray(firstTrie.size()));
	}

	@Test
	public void test() throws Exception{
		String[] words = {"こんにちは", "さようなら", "おはよう", "おおきなかぶ", "おおやまざき"};
		TailLOUDSTrie lt = trieWithWords(words);
//		System.out.println(lt.getBvTree());
//		Algorithms.dump(lt.getRoot(), new OutputStreamWriter(System.out));
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
		try(
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos)){
			oos.writeObject(lt);
			oos.flush();
			lt = (TailLOUDSTrie)new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
			for(String w : words){
				Assert.assertTrue(lt.contains(w));
			}
			Assert.assertFalse(lt.contains("おやすみなさい"));
		}

		StringBuilder b = new StringBuilder();
		Node[] children = lt.getRoot().getChildren();
		for(Node n : children){
			char[] letters = n.getLetters();
			b.append(letters[0]);
		}
		Assert.assertEquals("おこさ", b.toString());
	}
}

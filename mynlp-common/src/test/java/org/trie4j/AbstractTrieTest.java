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
package org.trie4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractTrieTest<F extends Trie, S extends Trie> {
	protected abstract F createFirstTrie();
	protected abstract S buildSecondTrie(F firstTrie);

	protected S trieWithWords(String... words){
		F trie = createFirstTrie();
		for(String w : words) trie.insert(w);
		return buildSecondTrie(trie);
	}

	@Test
	public void test_empty() throws Exception{
		Trie trie = trieWithWords();
		Assert.assertEquals(0, trie.size());
		Assert.assertFalse(trie.contains("hello"));
		Assert.assertFalse(trie.commonPrefixSearch("hello").iterator().hasNext());
		Assert.assertFalse(trie.predictiveSearch("hello").iterator().hasNext());
		Assert.assertEquals(-1, trie.findShortestWord("hello", 0, 5, new StringBuilder()));
	}

	@Test
	public void test_size_1() throws Exception{
		Trie t = trieWithWords("hello", "world");
		Assert.assertEquals(2, t.size());
	}

	@Test
	public void test_size_2() throws Exception{
		Trie t = trieWithWords("hello", "hel", "world");
		Assert.assertEquals(3, t.size());
	}

	@Test
	public void test_size_3() throws Exception{
		Trie t = trieWithWords("hello", "hel", "world", "hel");
		Assert.assertEquals(3, t.size());
	}

	@Test
	public void test_size_4() throws Exception{
		Trie t = trieWithWords("hello", "helicoptor", "world", "hel");
		Assert.assertEquals(4, t.size());
	}

	@Test
	public void test_size_5() throws Exception{
		Trie t = trieWithWords("");
		Assert.assertEquals(1, t.size());
	}

	@Test
	public void test_contains_1() throws Exception{
		doTestContains("");
	}

	@Test
	public void test_contains_2() throws Exception{
		doTestContains("hello");
	}

	@Test
	public void test_contains_3() throws Exception{
		doTestContains("hello", "hi");
	}

	@Test
	public void test_contains_4() throws Exception{
		doTestContains("hello", "hi", "world");
	}

	@Test
	public void test_contains_5() throws Exception{
		doTestContains("hello", "hi", "hell", "helloworld", "world");
	}

	@Test
	public void test_contains_6() throws Exception{
		doTestContainsAndNot(new String[]{"hello", "hi", "helloworld", "world"}, new String[]{"h", "hell", "worl"});
	}

	@Test
	public void test_contains_7() throws Exception{
		doTestContains("hell", "hello", "apple", "orange", "banana", "watermelon",
				"peach", "kiwi", "cherry", "hassaku", 
				"yokan", "yatsuhashi", "anmitsu", "zenzai", "shiratama",
				"hiyokomanju", "zundamochi", "kuromitsu", "wasanbon", "botamochi",
				"warabimochi", "jelly", "momo", "nori", "donburi", "engawa",
				"gomokuni", "ikura"
				);
	}

	@Test
	public void test_commonPrefixSearch_1() throws Exception{
		Trie t = trieWithWords();
		Assert.assertFalse(t.commonPrefixSearch("hello").iterator().hasNext());
	}

	@Test
	public void test_commonPrefixSearch_2() throws Exception{
		String[] words = {"hello", "helloworld", "hell", "helloworld2"};
		Set<String> expects = new HashSet<>(Arrays.asList(words));
		Trie t = trieWithWords(words);
		Iterator<String> it = t.commonPrefixSearch("helloworld").iterator();
		while(it.hasNext()){
			Assert.assertTrue(expects.remove(it.next()));
		}
		Assert.assertEquals(1, expects.size());
	}

	@Test
	public void test_commonPrefixSearch_3() throws Exception{
		String[] words = {"hello", "helloworld", "hi", "howsgoing", "hell", "helloworld2", "world"};
		Set<String> expects = new HashSet<>(Arrays.asList(words));
		Trie t = trieWithWords(words);
		Iterator<String> it = t.commonPrefixSearch("helloworld").iterator();
		while(it.hasNext()){
			Assert.assertTrue(expects.remove(it.next()));
		}
		Assert.assertEquals(4, expects.size());
	}

	@Test
	public void test_findWord_1() throws Exception{
		Trie t = trieWithWords("hello", "helloworld", "hi", "howsgoing", "hell", "helloworld2", "world");
		String text = "This is the best sweets.";
		StringBuilder b = new StringBuilder();
		int i = t.findShortestWord(text, 0, text.length(), b);
		Assert.assertEquals(1, i);
		Assert.assertEquals("hi", b.toString());
	}

	@Test
	public void test_findShortestWord_1() throws Exception{
		Trie t = trieWithWords("hello", "helloworld", "hi", "howsgoing", "hell", "helloworld2", "world");
		String text = "The helloworld.";
		StringBuilder b = new StringBuilder();
		int i = t.findShortestWord(text, 0, text.length(), b);
		Assert.assertEquals(4, i);
		Assert.assertEquals("hell", b.toString());
	}

	//Docker（ドッカー[2]）はソフトウェアコンテナ内のアプリケーションのデプロイメントを自動化するオープンソースソフトウェアである。
	//Linuxカーネルにおける「libcontainer」と呼ばれるLinuxコンテナ技術[3]とaufsのような特殊なファイルシステムを利用してコンテナ型の仮想化を行う[4]。VMware製品などの完全仮想化を行うハイパーバイザー型製品と比べて、ディスク使用量は少なく、インスタンス作成やインスタンス起動は速く、性能劣化がほとんどないという利点を持つ。Dockerfileと呼ばれる設定ファイルからコンテナイメージファイルを作成可能という特性を持つ。一方で、コンテナOSとしてはホストOSと同じLinuxカーネルしか動作しない。
	@Test
	public void test_findShortestWord_2() throws Exception{
		String[] expected = {
			"ソフトウェア", "オープンソース", "ソフトウェア", "Linux", "Linux",
			"ファイル", "ファイル", "ファイル", "Linux"
		};
		Trie t = trieWithWords("ソフトウェア", "ソフトウェアコンテナ",
				"オープンソース", "オープンソースソフトウェア",
				"Linux", "Linuxカーネル",
				"Linuxコンテナ", "Linuxコンテナ技術",
				"ファイル", "ファイルシステム");
		List<String> actual = new ArrayList<>();
		for(String s : longSentences){
			int begin = 0;
			int found = -1;
			StringBuilder b = new StringBuilder();
			while((found = t.findShortestWord(s, begin, s.length(), b)) != -1){
				actual.add(b.toString());
				begin = found + b.length();
				b = new StringBuilder();
			}
		}
		Assert.assertArrayEquals(expected, actual.toArray(new String[]{}));
	}

	@Test
	public void test_findLongestWord_1() throws Exception{
		Trie t = trieWithWords("hello", "helloworld", "hi", "howsgoing", "hell", "helloworld2", "world");
		String text = "The helloworld.";
		StringBuilder b = new StringBuilder();
		int i = t.findLongestWord(text, 0, text.length(), b);
		Assert.assertEquals(4, i);
		Assert.assertEquals("helloworld", b.toString());
	}

	//Docker（ドッカー[2]）はソフトウェアコンテナ内のアプリケーションのデプロイメントを自動化するオープンソースソフトウェアである。
	//Linuxカーネルにおける「libcontainer」と呼ばれるLinuxコンテナ技術[3]とaufsのような特殊なファイルシステムを利用してコンテナ型の仮想化を行う[4]。
	//VMware製品などの完全仮想化を行うハイパーバイザー型製品と比べて、ディスク使用量は少なく、インスタンス作成やインスタンス起動は速く、
	//性能劣化がほとんどないという利点を持つ。Dockerfileと呼ばれる設定ファイルからコンテナイメージファイルを作成可能という特性を持つ。
	//一方で、コンテナOSとしてはホストOSと同じLinuxカーネルしか動作しない。
	@Test
	public void test_findLongestWord_2() throws Exception{
		String[] expected = {
			"ソフトウェアコンテナ", "オープンソースソフトウェア",
			"Linuxカーネル", "Linuxコンテナ技術", "ファイルシステム",
			"ファイル", "ファイル", "Linuxカーネル"
		};
		Trie t = trieWithWords("ソフトウェア", "ソフトウェアコンテナ",
				"オープンソース", "オープンソースソフトウェア",
				"Linux", "Linuxカーネル",
				"Linuxコンテナ", "Linuxコンテナ技術",
				"ファイル", "ファイルシステム");
		List<String> actual = new ArrayList<>();
		for(String s : longSentences){
			int begin = 0;
			int found = -1;
			StringBuilder b = new StringBuilder();
			while((found = t.findLongestWord(s, begin, s.length(), b)) != -1){
				actual.add(b.toString());
				begin = found + b.length();
				b = new StringBuilder();
			}
		}
		Assert.assertArrayEquals(expected, actual.toArray(new String[]{}));
	}

	@Test
	public void test_predictiveSearch_1() throws Exception{
		Trie t = trieWithWords();
		Assert.assertFalse(t.predictiveSearch("hello").iterator().hasNext());
	}

	@Test
	public void test_predictiveSearch_2() throws Exception{
		Trie t = trieWithWords("hello", "helloworld", "hell", "helloworld2");
		Iterator<String> it = t.predictiveSearch("he").iterator();
		Assert.assertEquals("hell", it.next());
		Assert.assertEquals("hello", it.next());
		Assert.assertEquals("helloworld", it.next());
		Assert.assertEquals("helloworld2", it.next());
		Assert.assertFalse(it.hasNext());
	}

	@Test
	public void test_predictiveSearch_3() throws Exception{
		Trie t = trieWithWords("hello", "helloworld1", "hell", "helloworld2");
		Iterator<String> it = t.predictiveSearch("hello").iterator();
		Assert.assertEquals("hello", it.next());
		Assert.assertEquals("helloworld1", it.next());
		Assert.assertEquals("helloworld2", it.next());
		Assert.assertFalse(it.hasNext());
	}

	@Test
	public void test_predictiveSearch_4() throws Exception{
		Trie t = trieWithWords("hello", "helloworld", "hi", "howsgoing", "hell", "helloworld2", "world");
		Iterator<String> it = t.predictiveSearch("hellow").iterator();
		Assert.assertEquals("helloworld", it.next());
		Assert.assertEquals("helloworld2", it.next());
		Assert.assertFalse(it.hasNext());
	}

	@Test
	public void test_getNode() throws Exception{
		Trie t = trieWithWords("hello", "helloworld", "hi", "howsgoing", "hell", "helloworld2", "world");
		Node n = t.getRoot().getChild('h');
		Assert.assertEquals(1, n.getLetters().length);
		Assert.assertEquals('h', n.getLetters()[0]);
	}

	private void doTestContains(String... words) throws Exception{
		Trie trie = trieWithWords(words);
		for(String w : words){
			Assert.assertTrue("must contain \"" + w  + "\"", trie.contains(w));
		}
		Assert.assertFalse("must not contain \"buzzbuzz\"", trie.contains("buzzbuzz"));
	}

	private void doTestContainsAndNot(String[] words, String[] notContains) throws Exception{
		Trie trie = trieWithWords(words);
		for(String w : words){
			Assert.assertTrue("must contain \"" + w  + "\"", trie.contains(w));
		}
		for(String w : notContains){
			Assert.assertFalse("must not contain \"" + w  + "\"", trie.contains(w));
		}
	}

	private static String[] longSentences;
	static{
		try(BufferedReader r = new BufferedReader(new InputStreamReader(
				AbstractTrieTest.class.getResourceAsStream("AbstractTrieTest_longsentences.txt"),
				"UTF-8"))){
			List<String> ret = new ArrayList<>();
			String line = null;
			while((line = r.readLine()) != null){
				line = line.trim();
				if(line.length() == 0) continue;
				ret.add(line);
			}
			longSentences = ret.toArray(new String[]{});
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}

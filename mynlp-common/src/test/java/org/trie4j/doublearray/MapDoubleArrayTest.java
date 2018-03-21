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
package org.trie4j.doublearray;

import org.junit.Assert;
import org.junit.Test;
import org.trie4j.AbstractImmutableMapTrieTest;
import org.trie4j.MapTrie;
import org.trie4j.bv.BytesRank1OnlySuccinctBitVector;
import org.trie4j.patricia.MapPatriciaTrie;
import org.trie4j.test.WikipediaTitles;

public class MapDoubleArrayTest
extends AbstractImmutableMapTrieTest<MapDoubleArray<Integer>>{
	@Override
	protected MapDoubleArray<Integer> buildSecond(MapTrie<Integer> firstTrie) {
		return new MapDoubleArray<Integer>(firstTrie);
	}

	@Test
	public void test() throws Exception{
		String[] words = {
				"!SHOUT!",
				"!_-attention-",
				"!wagero!",
				"!［ai-ou］",
				"\"74ers\"_LIVE_IN_OSAKA-JO_HALL_2003"
		};
		Integer[] values = {1, 3, 2, 6, 100};
		verifyMapTrie(words, values);
	}

	@Test
	public void test2() throws Throwable{
		MapPatriciaTrie<Object> mpt = new MapPatriciaTrie<>();
		mpt.insert("FOO");
		mpt.get("F");
		mpt.get("f");

		MapDoubleArray<Object> mda = new MapDoubleArray<>(mpt);
		mda.get("F");
		mda.get("f"); // exception here
	}

	public void investigate1() throws Exception{
		String wikipediaFilename = "data/jawiki-20140416-all-titles-in-ns0.gz";
		int start = 7;
		int end = 13;
		int i = 0;
		MapTrie<Integer> trie = new MapPatriciaTrie<Integer>();
		for(String s : new WikipediaTitles(wikipediaFilename)){
			if(i >= end){
				break;
			} else if(i >= start){
				trie.insert(s, i);
				System.out.println(s);
			}
			i++;
		}
		i = 0;
		MapTrie<Integer> v = new MapDoubleArray<Integer>(trie);
		for(String s : new WikipediaTitles(wikipediaFilename)){
			if(i >= end){
				break;
			} else if(i >= start){
				Assert.assertEquals(s, i, (int)v.get(s));
			}
			i++;
		}
	}

	public void investigate2() throws Exception{
		String[] words = {
				"!SHOUT!",
				"!_-attention-",
				"!wagero!",
				"!［ai-ou］",
				"\"74ers\"_LIVE_IN_OSAKA-JO_HALL_2003"
		};
		Integer[] values = {1, 3, 2, 6, 100};

		MapDoubleArray<Integer> trie = (MapDoubleArray<Integer>)trieWithWordsAndValues(words, values);
		DoubleArray da = (DoubleArray)trie.getTrie();
		int n = words.length;
		BytesRank1OnlySuccinctBitVector bv = (BytesRank1OnlySuccinctBitVector)da.getTerm();
		System.out.println(bv.rank1(67));
		for(int i = 0; i < n; i++){
			String s = words[i];
			System.out.println(String.format(
					"%s, nid: %d, tid: %d, ev: %d, av: %d",
					s, da.getNodeId(s), da.getTermId(s), values[i], trie.get(s)));
			Assert.assertEquals(values[i], trie.get(words[i]));
		}
	}

	protected <T> void verifyMapTrie(String[] words, Integer[] values){
		MapTrie<Integer> trie = trieWithWordsAndValues(words, values);
		int n = words.length;
		for(int i = 0; i < n; i++){
			Assert.assertEquals(values[i], trie.get(words[i]));
		}
	}
}

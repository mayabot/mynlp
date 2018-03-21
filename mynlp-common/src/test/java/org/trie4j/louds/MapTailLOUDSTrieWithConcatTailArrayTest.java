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
package org.trie4j.louds;

import org.junit.Assert;
import org.trie4j.AbstractImmutableMapTrieTest;
import org.trie4j.MapTrie;
import org.trie4j.bv.BytesRank1OnlySuccinctBitVector;
import org.trie4j.doublearray.DoubleArray;
import org.trie4j.patricia.MapPatriciaTrie;
import org.trie4j.tail.ConcatTailArrayBuilder;
import org.trie4j.test.WikipediaTitles;

public class MapTailLOUDSTrieWithConcatTailArrayTest
extends AbstractImmutableMapTrieTest<MapTailLOUDSTrie<Integer>>{
	@Override
	protected MapTailLOUDSTrie<Integer> buildSecond(MapTrie<Integer> firstTrie) {
		return new MapTailLOUDSTrie<Integer>(firstTrie, new ConcatTailArrayBuilder());
	}

	public void investigate1() throws Exception{
		int start = 0;
		int end = 2000000;
		int i = 0;
		MapTrie<Integer> trie = new MapPatriciaTrie<Integer>();
		for(String s : new WikipediaTitles()){
			if(i >= end){
				break;
			} else if(i >= start){
				trie.insert(s, i);
			}
			i++;
		}
		i = 0;
		MapTailLOUDSTrie<Integer> v = new MapTailLOUDSTrie<Integer>(trie);
//		TailLOUDSTrie tlt = (TailLOUDSTrie)v.getTrie();
		for(String s : new WikipediaTitles()){
			if(i >= end){
				break;
			} else if(i >= start){
/*				System.out.println(String.format(
						"%s, nid: %d, tid: %d, ev: %d, av: %d",
						s, tlt.getNodeId(s), tlt.getTermId(s), i, v.get(s)));
*/				Assert.assertEquals(i + "th word: " + s, i, (int)v.get(s));
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

		MapTailLOUDSTrie<Integer> trie = trieWithWordsAndValues(words, values);
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
}

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
package org.trie4j.patricia;

import org.junit.Assert;
import org.trie4j.AbstractMutableTrieTest;
import org.trie4j.Trie;
import org.trie4j.tail.builder.SuffixTrieTailBuilder;
import org.trie4j.test.WikipediaTitles;

public class TailPatriciaTrieWithSuffixTrieTailBuilderTest
extends AbstractMutableTrieTest<TailPatriciaTrie> {
	@Override
	protected TailPatriciaTrie createFirstTrie() {
		return new TailPatriciaTrie(new SuffixTrieTailBuilder());
	}

	public void investigate() throws Exception{
		Trie t = new TailPatriciaTrie(new SuffixTrieTailBuilder());
		int start = 0;
		int end = 5;
		int i = 0;
		for(String word : new WikipediaTitles()){
			if(i >= end) break;
			if(i >= start){
				t.insert(word);
				System.out.println(word);
			}
			i++;
		}
		i = 0;
		for(String word : new WikipediaTitles()){
			if(i >= end) break;
			if(i >= start) Assert.assertTrue(
					i + "th word: " + word, t.contains(word));
			i++;
		}
	}
}

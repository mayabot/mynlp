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
package org.trie4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;
import org.trie4j.doublearray.MapTailDoubleArray;
import org.trie4j.louds.MapTailLOUDSTrie;
import org.trie4j.patricia.MapPatriciaTrie;
import org.trie4j.patricia.MapTailPatriciaTrie;
import org.trie4j.test.LapTimer;
import org.trie4j.test.WikipediaTitles;

public abstract class AbstractMapTrieWikipediaSerializeTest
extends AbstractWikipediaSerializeTest{
	protected MapTrie<Integer> newTrie(){
		return new MapPatriciaTrie<Integer>();
	}

	protected MapTrie<Integer> buildSecondTrie(MapTrie<Integer> firstTrie){
		return firstTrie;
	}


	@Test
	@SuppressWarnings("unchecked")
	public void test() throws Exception{
		WikipediaTitles wt = new WikipediaTitles();
		MapTrie<Integer> trie = wt.insertTo(newTrie());
		trie = buildSecondTrie(trie);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		LapTimer lt = new LapTimer();
		oos.writeObject(trie);
		oos.flush();
		long wd = lt.lapMillis();
		byte[] serialized = baos.toByteArray();
		lt.reset();
		MapTrie<Integer> t = (MapTrie<Integer>)new ObjectInputStream(
				new ByteArrayInputStream(serialized))
				.readObject();
		long rd = lt.lapMillis();
		long vd = wt.assertAllContains(t);
		System.out.println(String.format(
				"%s%s%s, size: %d, write(ms): %d, read(ms): %d, verify(ms): %d.",
				trie.getClass().getSimpleName(),
				getBvTreeClassName(trie),
				getTailClassName(trie),
				serialized.length, wd, rd, vd
				));
	}

	@SuppressWarnings("rawtypes")
	private static String getBvTreeClassName(MapTrie<?> trie){
		if(trie instanceof MapTailLOUDSTrie){
			return getBvTreeClassName(((MapTailLOUDSTrie)trie).getTrie());
		} else{
			return "";
		}
	}

	@SuppressWarnings("rawtypes")
	private static String getTailClassName(MapTrie<?> trie){
		if(trie instanceof MapTailPatriciaTrie){
			return "(" + ((MapTailPatriciaTrie) trie).getTailBuilder().getClass().getSimpleName() + ")";
		} else if(trie instanceof MapTailDoubleArray){
			return "(unknown)";
		} else if(trie instanceof AbstractTermIdMapTrie){
			Trie orig = ((AbstractTermIdMapTrie) trie).getTrie();
			return getTailClassName(orig);
		} else{
			return "";
		}
	}
}

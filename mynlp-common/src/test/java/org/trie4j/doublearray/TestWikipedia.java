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
package org.trie4j.doublearray;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

import org.trie4j.Algorithms;
import org.trie4j.Node;
import org.trie4j.NodeVisitor;
import org.trie4j.Trie;
import org.trie4j.patricia.PatriciaTrie;
import org.trie4j.test.LapTimer;
import org.trie4j.test.WikipediaTitles;

public class TestWikipedia {
	private static final int maxCount = 20000000;

	public static void main(String[] args) throws Exception{
		System.out.println("--- building patricia trie ---");
		Trie trie = new PatriciaTrie();
//		Trie trie = new TailPatriciaTrie(new ConcatTailBuilder());
		int c = 0;
		LapTimer t1 = new LapTimer();
		for(String word : new WikipediaTitles()){
			trie.insert(word);
			c++;
			if(c == maxCount) break;
		}
		System.out.println("done in " + t1.lapMillis() + " millis.");
		System.out.println(c + "entries in ja wikipedia titles.");

		System.out.println("-- building double array.");
		t1.reset();
//		Trie da = new TailDoubleArray(trie, 65536, new ConcatTailBuilder());
//		Trie da = new DoubleArray(trie, 65536);
		Trie da = trie;
		trie = null;
		System.out.println("done in " + t1.lapMillis() + " millis.");
		final AtomicInteger count = new AtomicInteger();
		Algorithms.traverseByBreadth(da.getRoot(), new NodeVisitor() {
			@Override
			public boolean visit(Node node, int nest) {
				count.incrementAndGet();
				return true;
			}
		});
		System.out.println(count + " nodes in trie.");
		da.dump(new PrintWriter(System.out));

		verify(da);
		System.out.println("---- common prefix search ----");
		System.out.println("-- for 東京国際フォーラム");
		for(String s : da.commonPrefixSearch("東京国際フォーラム")){
			System.out.println(s);
		}
		System.out.println("-- for 大阪城ホール");
		for(String s : da.commonPrefixSearch("大阪城ホール")){
			System.out.println(s);
		}
		System.out.println("---- predictive search ----");
		System.out.println("-- for 大阪城");
		for(String s : da.predictiveSearch("大阪城")){
			System.out.println(s);
		}
		System.out.println("---- done ----");

		Thread.sleep(10000);
		da.contains("hello");
	}

	private static void verify(Trie da) throws Exception{
		System.out.println("verifying double array...");
		int c = 0;
		int sum = 0;
		LapTimer t1 = new LapTimer();
		LapTimer t = new LapTimer();
		for(String word : new WikipediaTitles()){
			if(c == maxCount) break;
			t.reset();
			boolean found = da.contains(word);
			sum += t.lapMillis();
			c++;
			if(!found){
				System.out.println("verification failed.  trie not contains " + c + " th word: [" + word + "]");
				break;
			}
		}
		System.out.println("done " + c + "words in " + t1.lapMillis() + " millis.");
		System.out.println("contains time: " + sum + " millis.");
	}
}

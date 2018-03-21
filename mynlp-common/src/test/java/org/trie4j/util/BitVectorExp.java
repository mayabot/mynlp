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
package org.trie4j.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.trie4j.Algorithms;
import org.trie4j.Node;
import org.trie4j.NodeVisitor;
import org.trie4j.Trie;
import org.trie4j.bv.BytesSuccinctBitVector;
import org.trie4j.patricia.PatriciaTrie;
import org.trie4j.test.LapTimer;
import org.trie4j.test.WikipediaTitles;

public class BitVectorExp {
	private static final int maxCount = 2000000;

	public static void main(String[] args) throws Exception{
		Trie trie = new PatriciaTrie();
		int c = 0;
		// You can download archive from http://dumps.wikimedia.org/jawiki/latest/
		LapTimer t = new LapTimer();
		for(String word : new WikipediaTitles()){
			trie.insert(word);
			c++;
			if(c == maxCount) break;
		}
		t.lapMillis("trie building done. %d words.", c);
		final BytesSuccinctBitVector bv = new BytesSuccinctBitVector(5000000);
		final AtomicInteger nodeCount = new AtomicInteger();
		Algorithms.traverseByDepth(trie.getRoot(), new NodeVisitor() {
			@Override
			public boolean visit(Node node, int nest) {
				Node[] children = node.getChildren();
				if(children != null){
					int n = node.getChildren().length;
					for(int i = 0 ;i  < n; i++){
						bv.append(true);
					}
				}
				bv.append(false);
				nodeCount.incrementAndGet();
				return true;
			}
		});
		trie = null;
		t.lapMillis("done. %d nodes inserted. do rank and select", nodeCount.intValue());
		for(int i = 0; i < c; i += 100){
			int count = bv.rank(i, true);
			bv.select(count, true);
		}
		t.lapMillis("done.");
		Thread.sleep(10000);
	}
}

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

import org.trie4j.AbstractWikipediaTest;
import org.trie4j.Node;
import org.trie4j.Trie;
import org.trie4j.bv.LongsConstantTimeSelect0SuccinctBitVector;
import org.trie4j.louds.TailLOUDSTrie.NodeListener;
import org.trie4j.louds.bvtree.LOUDSBvTree;
import org.trie4j.tail.SuffixTrieTailArray;
import org.trie4j.test.MonitoredSuccinctBitVector;
import org.trie4j.test.TrieMemoryUsage;

public class TailLOUDSTrieWithSuffixTrieTailArrayWikipediaMonitoredSBVTest extends AbstractWikipediaTest{
	@Override
	protected Trie buildSecondTrie(Trie first) {
		bv.resetCounts();
		TailLOUDSTrie t = new TailLOUDSTrie(
				first,
				new LOUDSBvTree(bv),
				new SuffixTrieTailArray(first.size()),
				new NodeListener() {
					@Override
					public void listen(Node node, int id) {
					}
				});
		t.trimToSize();
		return t;
	}

	@Override
	protected void afterVerification(Trie trie) throws Exception{
		System.out.println(String.format(
				"select0 time(ms): %.3f, min(ns): %d, max(ns): %d, count: %d",
				bv.getSelect0Time() / 1000000.0,
				bv.getSelect0MinTime(), bv.getSelect0MaxTime(),
				bv.getSelect0Count()
				));
		System.out.println(String.format(
				"next0 time(ms): %.3f, count: %d", bv.getNext0Time() / 1000000.0, bv.getNext0Count()
				));
		System.out.println(String.format(
				"rank1 time(ms): %.3f, count: %d", bv.getRank1Time() / 1000000.0, bv.getRank1Count()
				));
		new TrieMemoryUsage().print(trie);
//		System.out.println("select0Times:");
/*		int c = 0;
		for(long l : bv.getSelect0Times()){
			System.out.print(l + " ");
			if(++c % 100 == 0) System.out.println();
*/
/*
		for(Map.Entry<Long, Integer> entry : bv.getSelect0TimesMap().entrySet()){
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
*/
/*
		TailLOUDSTrie t = (TailLOUDSTrie)trie;
		final Map<Integer, List<Integer>> childrenCounts = new TreeMap<Integer, List<Integer>>(
				new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2) {
						return o2 - o1;
					}
				});
		Algorithms.traverseByBreadth(t.getRoot(), new NodeVisitor() {
			@Override
			public boolean visit(Node node, int nest) {
				int n = node.getChildren().length;
				List<Integer> nodes = childrenCounts.get(n);
				if(nodes == null){
					nodes = new ArrayList<Integer>();
					childrenCounts.put(n, nodes);
				}
				nodes.add(c++);
				return c < 6189;
			}
			int c = 0;
		});
		for(Map.Entry<Integer, List<Integer>> entry : childrenCounts.entrySet()){
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}

		Thread.sleep(10000);
//*/
	}

	private MonitoredSuccinctBitVector bv = new MonitoredSuccinctBitVector(
//			new BytesSuccinctBitVector(65536)
//			new BytesConstantTimeSelect0SuccinctBitVector(65536)
//			new LongsSuccinctBitVector(65536)
			new LongsConstantTimeSelect0SuccinctBitVector(65536)
			);
}

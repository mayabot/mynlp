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

import java.util.concurrent.atomic.AtomicInteger;

import org.trie4j.AbstractMapTrieWikipediaTest;
import org.trie4j.Algorithms;
import org.trie4j.MapTrie;
import org.trie4j.Node;
import org.trie4j.NodeVisitor;
import org.trie4j.Trie;
import org.trie4j.patricia.MapTailPatriciaTrie;
import org.trie4j.tail.builder.ConcatTailBuilder;

public class MapTailPatriciaTrieWithConcatTailBuilderWikipediaTest extends AbstractMapTrieWikipediaTest{
	@Override
	protected MapTrie<Integer> createFirstTrie() {
		return new MapTailPatriciaTrie<Integer>(new ConcatTailBuilder());
	}

	@Override
	protected void afterVerification(Trie trie) throws Exception {
		final AtomicInteger nodes = new AtomicInteger();
		final AtomicInteger leaves = new AtomicInteger();
		Algorithms.traverseByDepth(trie.getRoot(), new NodeVisitor() {
			@Override
			public boolean visit(Node node, int nest) {
				if(node.isTerminate()) leaves.incrementAndGet();
				else nodes.incrementAndGet();
				return true;
			}
		});
		System.out.println(String.format(
				"%d nodes and %d leaves", nodes.intValue(), leaves.intValue()
				));
		super.afterVerification(trie);
	}
}

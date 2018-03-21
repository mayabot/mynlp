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

import org.junit.Assert;
import org.junit.Test;
import org.trie4j.test.SimpleNode;

public class AlgorithmsTest {
	@Test
	public void test_traverseByBreadth(){
		final StringBuilder letters = new StringBuilder();
		final StringBuilder nests = new StringBuilder();
		Algorithms.traverseByBreadth(root, new NodeVisitor() {
			@Override
			public boolean visit(Node node, int nest) {
				letters.append(node.getLetters());
				nests.append(nest);
				return true;
			}
		});
		Assert.assertEquals("abcdefgh", letters.toString());
		Assert.assertEquals("01112223", nests.toString());
	}

	@Test
	public void test_traverseByDepth(){
		final StringBuilder letters = new StringBuilder();
		final StringBuilder nests = new StringBuilder();
		Algorithms.traverseByDepth(root, new NodeVisitor() {
			@Override
			public boolean visit(Node node, int nest) {
				letters.append(node.getLetters());
				nests.append(nest);
				return true;
			}
		});
		Assert.assertEquals("abefhcdg", letters.toString());
		Assert.assertEquals("01223112", nests.toString());
	}

	private Node root = new SimpleNode("a"
			, new SimpleNode("b"
					, new SimpleNode("e"), new SimpleNode("f"
							, new SimpleNode("h")))
			, new SimpleNode("c")
			, new SimpleNode("d"
					, new SimpleNode("g"))
			);

}

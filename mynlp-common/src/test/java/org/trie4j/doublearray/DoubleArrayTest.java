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
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;
import org.trie4j.AbstractTermIdTrieTest;
import org.trie4j.Node;
import org.trie4j.Trie;
import org.trie4j.util.StreamUtil;
import org.trie4j.util.StringUtil;

public class DoubleArrayTest
extends AbstractTermIdTrieTest<DoubleArray>{
	@Override
	protected DoubleArray buildSecond(Trie firstTrie) {
		return new DoubleArray(firstTrie);
	}

	private void print(Node n, int nest, PrintWriter w){
		w.println(
				StringUtil.repeted(" ", nest) +
				new String(n.getLetters()) +
				(n.isTerminate() ? "*" : ""));
		nest++;
		for(Node c : n.getChildren()){
			print(c, nest, w);
		}
	}

	@Test
	public void test_getNode_traverse() throws Exception{
		Trie t = trieWithWords("hello", "helloworld", "hi", "howsgoing", "hell", "helloworld2", "world");
		StringWriter sw = new StringWriter();
		PrintWriter w = new PrintWriter(sw);
		print(t.getRoot(), 0, w);
		String expected = StreamUtil.readAsString(getClass().getResourceAsStream("DoubleArrayTest_dump_expected.txt"), "UTF-8");
		String actual = sw.toString();
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void test_issue_035() throws Exception{
		Trie t = trieWithWords(
				"php.a", "php.e", "php.o", "e", "php.elu", "php.s", "php.x");
		DoubleArray da = new DoubleArray(t);
		da.commonPrefixSearchWithTermId("php.ele");
	}
}

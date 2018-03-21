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

import java.util.Arrays;

import org.trie4j.patricia.TailPatriciaTrieNodeAdapter;
import org.trie4j.patricia.TailPatriciaTrie;

public class Test {
	public static void main(String[] args) throws Exception{
		System.out.println("--- tail patricia trie ---");
		go(new TailPatriciaTrie());
//		System.out.println("--- multilayer patricia trie ---");
//		go(new MultilayerPatriciaTrie());
//		System.out.println("--- hash trie ---");
//		go(new HashSetTrie());
	}

	private static void go(Trie trie) throws Exception{
		String[] words = {
				"apple", "appear", "a", "orange"
				, "applejuice", "appletea", "appleshower"
				, "orangejuice"
				};
		trie.insert("");
		for(String w : words){
			System.out.println("insert \"" + w + "\"");
			trie.insert(w);
			System.out.println("--dump--");
			Algorithms.traverseByDepth(trie.getRoot(), new NodeVisitor() {
				@Override
				public boolean visit(Node node, int nest) {
					for(int i = 0; i < nest; i++){
						System.out.print(" ");
					}
					char[] letters = node.getLetters();
					if(letters == null || letters.length == 0){
						System.out.print("<empty>");
					} else{
						if(node instanceof TailPatriciaTrieNodeAdapter){
							TailPatriciaTrieNodeAdapter na = (TailPatriciaTrieNodeAdapter)node;
							System.out.print("[" + na.getFirstLetter() + "]");
							if(letters.length > 0){
								System.out.print(Arrays.copyOfRange(letters, 1, letters.length));
							}
						} else{
							System.out.print(letters);
						}
					}
					if(node.isTerminate()){
						System.out.println("*");
					} else{
						System.out.println("");
					}
					return true;
				}
			});
		}
		System.out.println(trie.contains(""));

		System.out.println("--test contains--");
		for(String w : words){
			System.out.print(w + ": ");
			System.out.println(trie.contains(w));
		}
		System.out.println("--test not contains--");
		for(String w : new String[]{"banana", "app", "applebeer", "applejuice2"}){
			System.out.println(w + ": " + trie.contains(w));
		}
		System.out.println("-- test common prefix search --");
		System.out.println("query: applejuicebar");
//		for(String w : trie.commonPrefixSearch("applejuicebar")){
//			System.out.println(w);
//		}
	}
}

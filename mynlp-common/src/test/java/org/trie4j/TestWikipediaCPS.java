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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.trie4j.patricia.PatriciaTrie;
import org.trie4j.util.CharsetUtil;

public class TestWikipediaCPS {
	private static final int maxCount = 2000000;

	public static void main(String[] args) throws Exception{
		System.out.println("--- recursive patricia trie ---");
		Trie trie = new PatriciaTrie();
		int c = 0;
		BufferedReader r = new BufferedReader(new InputStreamReader(
				new GZIPInputStream(new FileInputStream("jawiki-20120220-all-titles-in-ns0.gz"))
				, CharsetUtil.newUTF8Decoder()));
		String word = null;

		long sum = 0;
		long lap = System.currentTimeMillis();
		while((word = r.readLine()) != null){
			long d = System.currentTimeMillis();
			trie.insert(word);
			sum += System.currentTimeMillis() - d;
			if(c % 100000 == 0){
				d = System.currentTimeMillis() - lap;
				long free = Runtime.getRuntime().freeMemory();
				System.out.println(
						c + "," + free + "," + Runtime.getRuntime().maxMemory() + "," + d
						);
				lap = System.currentTimeMillis();
			}
			c++;
			if(c == maxCount) break;
		}
		System.out.println(c + "entries in ja wikipedia titles.");
		System.out.println("insert time: " + sum + " millis.");

		System.out.println("-- insert done.");
		System.out.println(Runtime.getRuntime().freeMemory() + " bytes free.");

		doSearches(trie);
	}

	private static void doSearches(Trie trie){
		long start = System.currentTimeMillis();
		System.out.println("---- common prefix search ----");
		System.out.println("-- for 東京国際フォーラム");
		for(String s : trie.commonPrefixSearch("東京国際フォーラム")){
			System.out.println(s);
		}
		System.out.println("-- for 大阪城ホール");
		for(String s : trie.commonPrefixSearch("大阪城ホール")){
			System.out.println(s);
		}
		System.out.println("---- predictive search ----");
		System.out.println("-- for 大阪城");
		for(String s : trie.predictiveSearch("大阪城")){
			System.out.println(s);
		}
		System.out.println("---- predictive search ----");
		System.out.println("-- for 東");
		for(String s : trie.predictiveSearch("東京国")){
			System.out.println(s);
		}
		System.out.println("-- total search millis: " + (System.currentTimeMillis() - start));
	}
}

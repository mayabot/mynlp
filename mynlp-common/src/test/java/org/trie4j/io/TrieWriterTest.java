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
package org.trie4j.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;
import org.trie4j.Trie;
import org.trie4j.louds.TailLOUDSTrie;
import org.trie4j.louds.bvtree.LOUDSPPBvTree;
import org.trie4j.patricia.PatriciaTrie;
import org.trie4j.tail.SuffixTrieDenseTailArrayBuilder;
import org.trie4j.test.LapTimer;
import org.trie4j.test.WikipediaTitles;

public class TrieWriterTest {
	@Test
	public void test() throws Exception{
		LapTimer lt = new LapTimer();
		PatriciaTrie origTrie = new PatriciaTrie();
		new WikipediaTitles().insertTo(origTrie);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		TrieWriter tw = new TrieWriter(baos);
		Trie trie = new TailLOUDSTrie(origTrie, new LOUDSPPBvTree(origTrie.nodeSize()),
				new SuffixTrieDenseTailArrayBuilder());
		lt.reset();
		tw.write(trie);
		tw.flush();
		lt.lapMillis("trie saved.");
		System.out.println(baos.size() + " bytes");
		TrieReader tr = new TrieReader(new ByteArrayInputStream(baos.toByteArray()));
		lt.reset();
		Trie trie2 = tr.read();
		lt.lapMillis("trie loaded.");
		long d = new WikipediaTitles().assertAllContains(trie2);
		System.out.println("[" + d + "ms]: verified");
	}
}

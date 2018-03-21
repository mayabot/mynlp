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

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

import org.trie4j.patricia.TailPatriciaTrie;
import org.trie4j.tail.builder.ConcatTailBuilder;
import org.trie4j.test.LapTimer;
import org.trie4j.test.WikipediaTitles;

public class TestWikipedia {
	private static final int maxCount = 20000000;

	public static void main2(String[] args) throws Exception{
		int base = 137320;
		int c = 0;
		for(String word : new WikipediaTitles()){
			if(c > base) System.out.println(word);
			c++;
			if(c == (base + 100)) break;
		}
	}

	public static void main(String[] args) throws Exception{
//		Trie trie = new org.trie4j.patricia.simple.PatriciaTrie();
//		Trie trie = new org.trie4j.patricia.multilayer.MultilayerPatriciaTrie();
		Trie trie = new TailPatriciaTrie(new ConcatTailBuilder());
		LapTimer t = new LapTimer();

		{
			System.out.println("-- building first trie: " + trie.getClass().getName());
			int c = 0;
			int charCount = 0;
			long sum = 0;
			for(String word : new WikipediaTitles()){
				t.reset();
				trie.insert(word);
				sum += t.lapMillis();
				charCount += word.length();
				c++;
				if(c == maxCount) break;
			}
			System.out.println(String.format(
					"-- done in %d millis with %d entries, %d chars"
					, sum / 1000000, c, charCount
					));
		}

		{
			System.out.println("-- building second trie.");
			t.reset();
			trie = new org.trie4j.doublearray.DoubleArray(trie, 65536);
//			trie = new org.trie4j.doublearray.TailDoubleArray(trie, 65536, new ConcatTailBuilder());
//			trie = new org.trie4j.louds.LOUDSTrie(trie, 65536, new ConcatTailBuilder());
//			trie = new org.trie4j.louds.LOUDSTrie(trie, 65536, new SuffixTrieTailBuilder());
			trie.trimToSize();
			System.out.println(String.format(
					"-- done in %d millis.", t.lapMillis() / 1000000
					));
			System.gc();
			System.gc();
			System.out.println("waiting 10 seconds.");
//			Thread.sleep(10000);
		}

		System.out.println("-- dump trie.");
		trie.dump(new PrintWriter(System.out));
		return;
/*
		System.out.println("-- traversing trie.");
		final AtomicInteger cnt = new AtomicInteger();
		trie.traverse(new NodeVisitor() {
			@Override
			public boolean visit(Node node, int nest) {
				if(node instanceof InternalCharsNode){
					if(((InternalCharsNode)node).getChildren().length == 1){
						cnt.incrementAndGet();
					}
				}
				return true;
			}
		});
		System.out.println(cnt + " nodes have 1 child.");
//		investigate(trie, charCount);
//*
//		dump(trie);
		System.out.println("-- pack");
		t.lap();
		if(trie instanceof MultilayerPatriciaTrie){
			MultilayerPatriciaTrie mt = (MultilayerPatriciaTrie)trie;
			mt.pack();
			System.out.println("-- pack done in " + (t.lap() / 1000000) + " millis.");
	//		dump(trie);
			System.gc();
			Thread.sleep(1000);
			System.out.println(Runtime.getRuntime().freeMemory() + " bytes free.");
			investigate(mt);
		}
//*/
	}

	@SuppressWarnings("unused")
	private static void investigate(Trie trie)
	throws Exception{
		System.out.println("-- dump root children.");
		for(Node n : trie.getRoot().getChildren()){
			System.out.print(n.getLetters()[0]);
		}
		System.out.println();
		System.out.println("-- count elements.");
		final AtomicInteger count = new AtomicInteger();
		Algorithms.traverseByDepth(trie.getRoot(), new NodeVisitor() {
			public boolean visit(Node node, int nest) {
				if(node.isTerminate()) count.incrementAndGet();
				return true;
			}
		});
		System.out.println(count.intValue() + " elements.");
//*
		System.out.println("-- list elements.");

		final AtomicInteger n = new AtomicInteger();
		final AtomicInteger l = new AtomicInteger();
		final AtomicInteger ln = new AtomicInteger();
		final AtomicInteger chars = new AtomicInteger();
		Algorithms.traverseByDepth(trie.getRoot(), new NodeVisitor() {
				public boolean visit(Node node, int nest) {
					if(node.isTerminate()){
						l.incrementAndGet();
					} else{
						n.incrementAndGet();
					}
					return true;
				}
			});
		System.out.println("node: " + n.intValue());
		System.out.println("leaf: " + l.intValue());
		System.out.println("label node: " + ln.intValue());
		System.out.println("total char count in trie: " + chars.intValue());

		System.out.println("verifying trie...");
		long lap = System.currentTimeMillis();
		int c = 0;
		int sum = 0;
		for(String word : new WikipediaTitles()){
			if(c == maxCount) break;
			long d = System.currentTimeMillis();
			boolean found = Algorithms.contains(trie.getRoot(), word);//trie.contains(word);
			sum += System.currentTimeMillis() - d;
			if(!found){
				System.out.println("trie not contains [" + word + "]");
				break;
			}
			if(c % 100000 == 0){
				System.out.println(c + " elements done.");
			}
			c++;
		}
		System.out.println("done in " + (System.currentTimeMillis() - lap) + " millis.");
		System.out.println("contains time: " + sum + " millis.");
		
//		System.out.println(trie.getRoot().getChildren().length + "children in root");
		if(trie instanceof TailPatriciaTrie){
//			((TailPatriciaTrie) trie).pack();
			System.out.println("tail length: " + ((TailPatriciaTrie) trie).getTailBuilder().getTails().length());
		}
		final Trie t = trie;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(100000);
					t.contains("hello");
				} catch (InterruptedException e) {
				}
			}
		}).start();
//*/
	}
}

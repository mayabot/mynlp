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
package org.trie4j.tail;

import org.junit.Assert;
import org.junit.Test;
import org.trie4j.bv.BytesSuccinctBitVector;
import org.trie4j.louds.TailLOUDSTrie;
import org.trie4j.patricia.TailPatriciaTrie;
import org.trie4j.tail.builder.ConcatTailBuilder;
import org.trie4j.tail.index.SBVTailIndex;
import org.trie4j.test.WikipediaTitles;
import org.trie4j.util.IntArray;

public class SBVConcatTailArrayTest {
	@Test
	public void test() throws Exception{
		// 普通にSBVConcatTailArrayIndexBuilder使った場合と、
		// add毎にappendするTailArrayIndexBuilderを作ってそれを使った
		// 場合でbitvectorやcacheに差が出るか調べる
		TailPatriciaTrie org = new TailPatriciaTrie(new ConcatTailBuilder());
		new WikipediaTitles().insertTo(org);
		TailLOUDSTrie louds1 = new TailLOUDSTrie(org, new SBVConcatTailArrayAppendingBuilder());
		new WikipediaTitles().assertAllContains(louds1);
		BytesSuccinctBitVector sbv1 = (BytesSuccinctBitVector)((SBVTailIndex)((DefaultTailArray)louds1.getTailArray()).getTailIndex()).getSbv();
		TailLOUDSTrie louds2 = new TailLOUDSTrie(org, new SBVConcatTailArrayBuilder());
		new WikipediaTitles().assertAllContains(louds2);
		BytesSuccinctBitVector sbv2 = (BytesSuccinctBitVector)((SBVTailIndex)((DefaultTailArray)louds2.getTailArray()).getTailIndex()).getSbv();
		{
			int n = sbv1.size();
			System.out.println("sbv size: " + n);
			Assert.assertEquals(n, sbv2.size());
			for(int i = 0; i < n; i++){
				Assert.assertEquals(i + "th bit", sbv1.get(i), sbv2.get(i));
			}
		}
		{
			int[] countCache1 = sbv1.getCountCache0();
			int[] countCache2 = sbv2.getCountCache0();
			int n = countCache1.length;
			System.out.println("countCache0 size should be: " + (sbv1.size() / 64 + 1));
			System.out.println("countCache0 size: " + n);
//			Assert.assertEquals(n, countCache2.length);
			n = Math.min(countCache1.length, countCache2.length);
			for(int i = 0; i < n; i++){
				Assert.assertEquals(i + "th index cache.", countCache1[i], countCache2[i]);
			}
		}
		{
			IntArray indexCache1 = sbv1.getIndexCache0();
			IntArray indexCache2 = sbv2.getIndexCache0();
			int n = indexCache1.size();
			System.out.println("indexCache0 size1: " + n);
			System.out.println("indexCache0 size2: " + indexCache2.size());
//			Assert.assertEquals(n, countCache2.length);
			n = Math.min(indexCache1.size(), indexCache2.size());
			for(int i = 0; i < 10; i++){
				System.out.print(indexCache1.get(i) + ", ");
			}
			System.out.println();
			for(int i = 0; i < 10; i++){
				System.out.print(indexCache2.get(i) + ", ");
			}
			System.out.println();
			for(int i = 0; i < n; i++){
				Assert.assertEquals(i + "th index cache.", indexCache1.get(i), indexCache2.get(i));
			}
		}
	}
}

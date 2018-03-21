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
package org.trie4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;
import org.trie4j.doublearray.TailDoubleArray;
import org.trie4j.louds.TailLOUDSTrie;
import org.trie4j.louds.bvtree.BvTree;
import org.trie4j.louds.bvtree.LOUDSBvTree;
import org.trie4j.louds.bvtree.LOUDSPPBvTree;
import org.trie4j.patricia.TailPatriciaTrie;
import org.trie4j.tail.DefaultTailArray;
import org.trie4j.test.LapTimer;
import org.trie4j.test.WikipediaTitles;

public abstract class AbstractWikipediaSerializeTest{
	protected Trie firstTrie(){
		return new TailPatriciaTrie();
	}
	protected Trie secondTrie(Trie first){
		return first;
	}

	@Test
	public void test() throws Exception{
		WikipediaTitles wt = new WikipediaTitles();
		Trie trie = wt.insertTo(firstTrie());
		trie = secondTrie(trie);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		LapTimer lt = new LapTimer();
		oos.writeObject(trie);
		oos.flush();
		long wd = lt.lapMillis();
		byte[] serialized = baos.toByteArray();
		lt.reset();
		Trie t = (Trie)new ObjectInputStream(new ByteArrayInputStream(serialized))
				.readObject();
		long rd = lt.lapMillis();
		long vd = wt.assertAllContains(t);
		System.out.println(String.format(
				"%s%s%s, size: %d, write(ms): %d, read(ms): %d, verify(ms): %d.",
				trie.getClass().getSimpleName(),
				getBvTreeClassName(trie),
				getTailClassName(trie),
				serialized.length, wd, rd, vd
				));
	}

	static String getBvTreeClassName(Trie trie){
		if(trie instanceof TailLOUDSTrie){
			StringBuilder b = new StringBuilder("(");
			BvTree bvTree = ((TailLOUDSTrie)trie).getBvTree();
			b.append(bvTree.getClass().getSimpleName());
			if(bvTree instanceof LOUDSBvTree){
				b.append("(");
				b.append(((LOUDSBvTree)bvTree).getSbv().getClass().getSimpleName());
				b.append(")");
			} else if(bvTree instanceof LOUDSPPBvTree){
				b.append("(");
				LOUDSPPBvTree pbvt = (LOUDSPPBvTree)bvTree;
				b.append("r0:")
					.append(pbvt.getR0().getClass().getSimpleName())
					.append(",r1:")
					.append(pbvt.getR1().getClass().getSimpleName())
					.append(")");
			}
			b.append(")");
			return b.toString();
		} else{
			return "";
		}
	}

	static String getTailClassName(Trie trie){
		if(trie instanceof TailPatriciaTrie){
			return "(" + ((TailPatriciaTrie) trie).getTailBuilder().getClass().getSimpleName() + ")";
		} else if(trie instanceof TailDoubleArray){
			return "(" + ((DefaultTailArray)((TailDoubleArray)trie).getTailArray()).getTailIndex().getClass().getSimpleName() + ")";
		} else if(trie instanceof TailLOUDSTrie){
			return "(" + ((DefaultTailArray)((TailLOUDSTrie)trie).getTailArray()).getTailIndex().getClass().getSimpleName() + ")";
		} else{
			return "";
		}
	}
}

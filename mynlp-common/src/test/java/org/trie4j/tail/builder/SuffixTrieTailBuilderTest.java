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
package org.trie4j.tail.builder;

import org.junit.Assert;
import org.junit.Test;

public class SuffixTrieTailBuilderTest {
	@Test
	public void test_tailtrie_1() throws Exception{
		SuffixTrieTailBuilder tb = new SuffixTrieTailBuilder();
		tb.insert("hello");
		tb.insert("mello");
		SuffixTrieTailBuilder.Node root = tb.getRoot();
		Assert.assertNotNull(root);
		Assert.assertNotNull(root.getChildren());
		Assert.assertEquals("ello", root.getLetters(tb.getTails()).toString());
		Assert.assertEquals("h", root.getChildren()[0].getLetters(tb.getTails()));
		Assert.assertEquals("m", root.getChildren()[1].getLetters(tb.getTails()));
	}

	@Test
	public void test_tailtrie_2() throws Exception{
		SuffixTrieTailBuilder tb = new SuffixTrieTailBuilder();
		tb.insert("world");
		tb.insert("helloworld");
		SuffixTrieTailBuilder.Node root = tb.getRoot();
		Assert.assertNotNull(root);
		Assert.assertNotNull(root.getChildren());
		Assert.assertEquals("world", root.getLetters(tb.getTails()));
		Assert.assertEquals("hello", root.getChildren()[0].getLetters(tb.getTails()));
	}

	@Test
	public void test_tailtrie_3() throws Exception{
		SuffixTrieTailBuilder tb = new SuffixTrieTailBuilder();
		tb.insert("world");
		tb.insert("hellorld");
		tb.insert("bold");
		SuffixTrieTailBuilder.Node root = tb.getRoot();
		Assert.assertNotNull(root);
		Assert.assertNotNull(root.getChildren());
		Assert.assertEquals("ld", root.getLetters(tb.getTails()));
		Assert.assertEquals("bo", root.getChildren()[0].getLetters(tb.getTails()));
		Assert.assertEquals("or", root.getChildren()[1].getLetters(tb.getTails()));
		Assert.assertEquals("hell", root.getChildren()[1].getChildren()[0].getLetters(tb.getTails()));
		Assert.assertEquals("w", root.getChildren()[1].getChildren()[1].getLetters(tb.getTails()));
		Assert.assertEquals("world\0hell\1\1\0bold\0", tb.getTails().toString());
	}

	@Test
	public void test_tailtrie_4() throws Exception{
		SuffixTrieTailBuilder tb = new SuffixTrieTailBuilder();
		int[] index = {0, 11, 13, 16, 21};
		int i = 0;
		for(String s : new String[]{
				"page_title",
				"!",
				"!!",
				"!!!",
				"!!!hello!!!"
				}){
			Assert.assertEquals(index[i++], tb.insert(s));
		}
		SuffixTrieTailBuilder.Node root = tb.getRoot();
		Assert.assertEquals("", root.getLetters(tb.getTails()));
		Assert.assertEquals("!", root.getChildren()[0]
				.getLetters(tb.getTails()));
		Assert.assertEquals("!", root.getChildren()[0]
				.getChildren()[0].getLetters(tb.getTails()));
		Assert.assertEquals("!", root.getChildren()[0]
				.getChildren()[0].getChildren()[0].getLetters(tb.getTails()));
		Assert.assertEquals("!!!hello", root.getChildren()[0]
				.getChildren()[0].getChildren()[0]
				.getChildren()[0].getLetters(tb.getTails()));
		Assert.assertEquals("page_title", root.getChildren()[1].getLetters(tb.getTails()));
	}
}

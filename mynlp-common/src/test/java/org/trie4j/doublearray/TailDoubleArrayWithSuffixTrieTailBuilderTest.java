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

import org.trie4j.AbstractTermIdTrieTest;
import org.trie4j.Trie;
import org.trie4j.tail.SuffixTrieTailArray;

public class TailDoubleArrayWithSuffixTrieTailBuilderTest
extends AbstractTermIdTrieTest<TailDoubleArray>{
	@Override
	protected TailDoubleArray buildSecond(Trie firstTrie) {
		return new TailDoubleArray(firstTrie, new SuffixTrieTailArray());
	}
}

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
package org.trie4j.patricia;

import org.trie4j.AbstractMapTrieWikipediaSerializeTest;
import org.trie4j.MapTrie;
import org.trie4j.patricia.MapTailPatriciaTrie;
import org.trie4j.tail.builder.ConcatTailBuilder;

public class MapTailPatriciaTrieWithConcatTailBuilderWikipediaSerializeTest
extends AbstractMapTrieWikipediaSerializeTest{
	@Override
	protected MapTrie<Integer> newTrie() {
		return new MapTailPatriciaTrie<Integer>(new ConcatTailBuilder());
	}
}

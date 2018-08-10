/*
 * Copyright 2013 Takao Nakaguchi
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
package org.trie4j.util;

import org.trie4j.Trie;

import java.util.AbstractSet;
import java.util.Iterator;

public class TrieSet extends AbstractSet<String> {
    public TrieSet(Trie trie) {
        this.trie = trie;
    }

    @Override
    public boolean add(String e) {
        int prev = trie.size();
        trie.insert(e);
        return prev != trie.size();
    }

    @Override
    public Iterator<String> iterator() {
        return trie.commonPrefixSearch("").iterator();
    }

    @Override
    public int size() {
        return trie.size();
    }

    private Trie trie;
}

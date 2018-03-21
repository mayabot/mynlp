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

import org.trie4j.util.IterableAdapter;
import org.trie4j.util.Pair;

public abstract class AbstractTermIdTrie extends AbstractTrie implements TermIdTrie {
    private static class StringIterableAdapter extends IterableAdapter<Pair<String, Integer>, String> {
        public StringIterableAdapter(Iterable<Pair<String, Integer>> iterable) {
            super(iterable);
        }

        @Override
        protected String convert(Pair<String, Integer> value) {
            return value.getFirst();
        }
    }

    @Override
    public boolean contains(String word) {
        return getTermId(word) != -1;
    }

    @Override
    public Iterable<String> commonPrefixSearch(String query) {
        return new StringIterableAdapter(commonPrefixSearchWithTermId(query));
    }

    @Override
    public Iterable<String> predictiveSearch(String prefix) {
        return new StringIterableAdapter(predictiveSearchWithTermId(prefix));
    }
}

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

import java.util.Map;

public abstract class AbstractMapTrie<T> extends AbstractTrie implements MapTrie<T> {

    private class StringIterableAdapter extends IterableAdapter<Map.Entry<String, T>, String> {
        public StringIterableAdapter(Iterable<Map.Entry<String, T>> iterable) {
            super(iterable);
        }

        @Override
        protected String convert(Map.Entry<String, T> value) {
            return value.getKey();
        }
    }

    @Override
    public Iterable<String> commonPrefixSearch(String query) {
        return new StringIterableAdapter(commonPrefixSearchEntries(query));
    }

    @Override
    public Iterable<String> predictiveSearch(String prefix) {
        return new StringIterableAdapter(predictiveSearchEntries(prefix));
    }
}

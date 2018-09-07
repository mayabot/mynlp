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

import org.trie4j.util.Pair;

/**
 * Represents the Trie that can manage the ID for containing keys.
 * The ID will be assigned to terminal node of trie tree and not be changed
 * through any method call.
 * The ID must be dense. "dense" means serial ID, it starts from 0 and
 * no need to be same order to keys.
 *
 * @author Takao Nakaguchi
 */
public interface TermIdTrie extends Trie {
    /**
     * Get the root node of this trie.
     *
     * @return root node.
     */
    @Override
    TermIdNode getRoot();

    /**
     * Returns the ID for text. If text doesn't exist in this Trie, this
     * method returns -1.
     *
     * @param text key to obtain key ID.
     * @return ID or -1
     */
    int getTermId(String text);

    /**
     * Search texts that is part of query and returns found keys with
     * key id.
     *
     * @param query
     * @return Iterable of found pairs (key and key id).
     */
    Iterable<Pair<String, Integer>> commonPrefixSearchWithTermId(String query);

    /**
     * Search texts that is begin with prefix and returns found keys with
     * key id.
     *
     * @param prefix
     * @return Iterable of found pairs (key and key id).
     */
    Iterable<Pair<String, Integer>> predictiveSearchWithTermId(String prefix);
}

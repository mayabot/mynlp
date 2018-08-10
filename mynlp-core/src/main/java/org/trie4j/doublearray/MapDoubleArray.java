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

import org.trie4j.doublearray.DoubleArray.TermNodeListener;
import org.trie4j.AbstractTermIdMapTrie;
import org.trie4j.MapNode;
import org.trie4j.MapTrie;
import org.trie4j.Node;

import java.io.Externalizable;
import java.util.Map;
import java.util.TreeMap;

/**
 * @param <T>
 * @author Takao Nakaguchi
 */
public class MapDoubleArray<T>
        extends AbstractTermIdMapTrie<T>
        implements Externalizable, MapTrie<T> {
    public MapDoubleArray() {
    }

    public MapDoubleArray(MapTrie<T> trie) {
        this(trie, trie.size() * 2);
    }

    public MapDoubleArray(MapTrie<T> trie, int arraySize) {
        final Map<Integer, Object> termValues = new TreeMap<Integer, Object>();
        setTrie(new DoubleArray(trie, arraySize, new TermNodeListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void listen(Node node, int nodeIndex) {
                termValues.put(nodeIndex, ((MapNode<T>) node).getValue());
            }
        }));
        setValues(termValues.values().toArray());
    }
}

/*
 * Copyright (C) 2012 Takao Nakaguchi
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

import org.trie4j.MapNode;

import java.io.Serializable;

public class MapPatriciaTrieNode<T>
        extends PatriciaTrieNode
        implements Serializable, MapNode<T> {
    public MapPatriciaTrieNode() {
        super(new char[]{}, false, emptyChildren);
    }

    public MapPatriciaTrieNode(char[] letters, boolean terminated) {
        super(letters, terminated, emptyChildren);
    }

    public MapPatriciaTrieNode(char[] letters, boolean terminated, T value) {
        super(letters, terminated, emptyChildren);
        this.value = value;
    }

    public MapPatriciaTrieNode(char[] letters, boolean terminated, MapPatriciaTrieNode<T>[] children) {
        super(letters, terminated, children);
    }

    public MapPatriciaTrieNode(char[] letters, boolean terminated, MapPatriciaTrieNode<T>[] children, T value) {
        super(letters, terminated, children);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapPatriciaTrieNode<T> getChild(char c) {
        return (MapPatriciaTrieNode<T>) super.getChild(c);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapPatriciaTrieNode<T>[] getChildren() {
        return (MapPatriciaTrieNode<T>[]) super.getChildren();
    }

    @Override
    public void setChildren(PatriciaTrieNode[] children) {
        super.setChildren(children);
    }

    public void setChildren(MapPatriciaTrieNode<T>[] children) {
        super.setChildren(children);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public PatriciaTrieNode addChild(int index, PatriciaTrieNode n) {
        MapPatriciaTrieNode[] newc = new MapPatriciaTrieNode[getChildren().length + 1];
        System.arraycopy(getChildren(), 0, newc, 0, index);
        newc[index] = (MapPatriciaTrieNode) n;
        System.arraycopy(getChildren(), index, newc, index + 1, getChildren().length - index);
        super.setChildren(newc);
        return this;
    }

    private T value;
    @SuppressWarnings("rawtypes")
    private static MapPatriciaTrieNode[] emptyChildren = {};
    private static final long serialVersionUID = 8611758181642617230L;
}

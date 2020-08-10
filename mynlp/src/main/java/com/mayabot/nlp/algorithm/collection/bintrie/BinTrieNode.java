/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mayabot.nlp.algorithm.collection.bintrie;

/**
 * @param <T>
 * @author jimichan
 */
public interface BinTrieNode<T> {

    BinTrieNode<T> addChildNode(BinTrieNode<T> nodeToInsert);

    BinTrieNode<T> findChild(char c);

    byte getStatus();

    T getValue();

    int compareTo(char c);

    boolean contains(char c);


    default BinTrieNode<T> findNode(char[] keyWord) {
        BinTrieNode<T> point = this;
        for (int j = 0; j < keyWord.length; j++) {
            point = point.findChild(keyWord[j]);
            if (point == null) {
                return null;
            }
        }
        return point;
    }

    /**
     * 寻找到这个路径的最后一个节点
     *
     * @param key
     * @return BinTrieNode
     */
    default BinTrieNode<T> findNode(CharSequence key) {
        BinTrieNode<T> branch = this;
        int len = key.length();
        for (int i = 0; i < len; i++) {
            char _char = key.charAt(i);
            if (branch == null) {
                return null;
            }
            branch = branch.findChild(_char);
        }
        return branch;
    }

}

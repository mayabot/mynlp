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

package com.mayabot.nlp.collection.bintrie;

import java.util.List;

/**
 * TireNode
 *
 * @param <V>
 * @author jimichan
 */
public abstract class AbstractTrieNode<V> implements BinTrieNode<V> {

    public static final byte Status_Begin = 1;
    public static final byte Status_Continue = 2;
    public static final byte Status_End = 3;
    public static final byte Status_Null = -1;


    // 节点字符
    public final char _char;
    // 状态
    public byte status; //
    // 词典后的参数
    public V value = null;

    /**
     * 层级
     */
    public short level;

    public AbstractTrieNode(char _char) {
        this._char = _char;
    }

    public abstract List<AbstractTrieNode<V>> getChildren();

    @Override
    public String toString() {
        return this._char + "[" + this.status + "]";
    }

    /**
     * 得道第几个参数
     *
     * @param i
     * @return
     */
    @SuppressWarnings("unchecked")
    public V getParam(int i) {
        if (value != null) {
            if (value instanceof String[]) {
                String[] _p = (String[]) value;
                if (_p.length > i) {
                    return (V) _p[i];
                }
            } else if (value instanceof List) {
                List<V> list = (List<V>) value;
                return list.get(i);
            }
        }
        return null;
    }


    public boolean equals(char c) {
        return this._char == c;
    }

    @Override
    public int hashCode() {
        return this._char;
    }

    @Override
    public int compareTo(char c) {
        if (this._char > c) {
            return 1;
        }
        if (this._char < c) {
            return -1;
        }
        return 0;
    }

    @Override
    public byte getStatus() {
        return status;
    }

    @Override
    public V getValue() {
        return this.value;
    }


    /**
     * 二分法查找
     *
     * @param branches
     * @param _char
     * @return 返回下标索引
     */
    static final int binarySearch(BinTrieNode<?>[] branches, char _char) {
        int high = branches.length - 1;
        if (branches.length < 1) {
            return high;
        }
        int low = 0;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = branches[mid].compareTo(_char);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1); // key not found.
    }
}

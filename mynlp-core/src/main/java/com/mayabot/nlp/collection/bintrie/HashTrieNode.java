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


import com.google.common.collect.Lists;
import com.mayabot.nlp.hppc.CharObjectHashMap;

import java.util.List;

/**
 * children 使用快速hash
 * CharObjectHashMap
 * 由3个数组组成。keys，values，和分配数组
 * 插入速度提升。内存占用比Default多了5%
 * crf数据测试，内存为465M，default为445M
 * 插入时间 10秒，default时间为15秒
 *
 * @param <T>
 * @author jimichan
 */
public class HashTrieNode<T> extends AbstractTrieNode<T> {

    private CharObjectHashMap<HashTrieNode<T>> map;

    HashTrieNode(char _char, byte status, T param) {
        super(_char);
        this.status = status;
        this.value = param;
    }

    @Override
    public List<AbstractTrieNode<T>> getChildren() {
        if (map == null) {
            return null;
        }

        return Lists.newArrayList(map.values());
    }

    /**
     * 插入子节点
     *
     * @param c
     * @return BinTrieNode
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BinTrieNode<T> addChildNode(BinTrieNode<T> c) {
        AbstractTrieNode<T> child = ((AbstractTrieNode<T>) c);

        if (map == null) {
            map = new CharObjectHashMap<>();
        }
        HashTrieNode<T> branch = map.get(child._char);
        if (branch != null) {
            switch (child.status) {
                case -1:
                    branch.status = Status_Begin;
                    break;
                case 1:
                    if (branch.status == Status_End) {
                        branch.status = Status_Continue;
                    }
                    break;
                case 3:
                    if (branch.status != Status_End) {
                        branch.status = Status_Continue;
                    }
                    branch.value = child.value;
            }
        } else {
            map.put(child._char, (HashTrieNode) child);
        }

        return map.get(child._char);

    }

    /**
     * 查找子节点
     *
     * @param c
     * @return HashTrieNode
     */
    @Override
    public HashTrieNode<T> findChild(char c) {
        if (map != null) {
            return map.get(c);
        }
        return null;
    }

    /**
     * 测试是否包含
     *
     * @param c
     * @return boolean
     */
    @Override
    public boolean contains(char c) {
        if (map != null) {
            return map.containsKey(c);
        }
        return false;
    }

}

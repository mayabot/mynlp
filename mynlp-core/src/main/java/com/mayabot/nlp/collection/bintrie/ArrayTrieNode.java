/*
 *  Copyright 2017 mayabot.com authors. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mayabot.nlp.collection.bintrie;

import java.util.ArrayList;
import java.util.List;

/**
 * TireNode
 * 采用数组存储。二分法查找。
 *
 * @param <T>
 * @author jimichan
 */
public class ArrayTrieNode<T> extends AbstractTrieNode<T> {

    private ArrayTrieNode<T>[] children = null;

    ArrayTrieNode(char _char, byte status, T value) {
        super(_char);
        this.status = status;
        this.value = value;
    }

    public List<AbstractTrieNode<T>> getChildren() {
        if (children == null) {
            return null;
        }
        List<AbstractTrieNode<T>> list = new ArrayList<>();
        for (ArrayTrieNode<T> e : children) {
            if (e != null) {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * 插入子节点
     *
     * @param c
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BinTrieNode<T> addChildNode(BinTrieNode<T> c) {

        AbstractTrieNode<T> child = ((AbstractTrieNode<T>) c);

        if (children == null) {
            children = new ArrayTrieNode[0];
        }
        int bs = binarySearch(children, child._char);
        if (bs >= 0) {
            ArrayTrieNode<T> branch = this.children[bs];// 单独查找出来的对象
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
            return branch;
        } else {
            ArrayTrieNode[] newBranches = new ArrayTrieNode[children.length + 1];
            int insert = -(bs + 1);
            System.arraycopy(children, 0, newBranches, 0, insert);
            System.arraycopy(children, insert, newBranches, insert + 1,
                    children.length - insert);
            newBranches[insert] = (ArrayTrieNode) child;
            children = newBranches;
            return child;
        }
    }

    /**
     * 查找子节点
     *
     * @param c
     * @return
     */
    public ArrayTrieNode<T> findChild(char c) {
        if (this.children == null) {
            return null;
        }
        int i = binarySearch(children, c);
        if (i < 0) {
            return null;
        }
        return this.children[i];
    }

    /**
     * 测试是否包含
     *
     * @param c
     * @return
     */
    public boolean contains(char c) {
        if (this.children == null) {
            return false;
        }
        return binarySearch(this.children, c) > -1;
    }

}

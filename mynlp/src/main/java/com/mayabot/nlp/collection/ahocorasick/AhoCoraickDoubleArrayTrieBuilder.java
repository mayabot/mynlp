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


/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * 源代码来自于 https://github.com/hankcs/HanLP
 */
package com.mayabot.nlp.collection.ahocorasick;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;


public class AhoCoraickDoubleArrayTrieBuilder<V> {

    /**
     * 根节点，仅仅用于构建过程
     */
    private State rootState = new State();
    /**
     * 是否占用，仅仅用于构建
     */
    private BitSet used;
    /**
     * 一个控制增长速度的变量
     */
    private int progress;
    /**
     * 下一个插入的位置将从此开始搜索
     */
    private int nextCheckPos;
    /**
     * 键值对的大小
     */
//    private int keySize;

    private int size;

    private AhoCorasickDoubleArrayTrie<V> trie = new AhoCorasickDoubleArrayTrie<V>();

    final int default_capacity = 1024 * 1024; // 1M

    private int array_capacity = default_capacity;

    public AhoCoraickDoubleArrayTrieBuilder() {
        used = new BitSet();
    }

    /**
     * 由一个排序好的map创建
     */
    @SuppressWarnings("unchecked")
    public AhoCorasickDoubleArrayTrie<V> build(TreeMap<String, V> map) {
        // 把值保存下来
        trie.values = new ArrayList<V>(map.values());
        trie.keylength = new int[trie.values.size()];

        trie.base = new int[array_capacity];
        trie.check = new int[array_capacity];

        Set<String> keySet = map.keySet();
        // 构建二分trie树
        addAllKeyword(keySet);
        // 在二分trie树的基础上构建双数组trie树
        buildDoubleArrayTrie(keySet);
        // 构建failure表并且合并output表
        constructFailureStates();
        rootState = null;
        loseWeight();
        return trie;
    }

    /**
     * 获取直接相连的子节点
     *
     * @param parent   父节点
     * @param siblings （子）兄弟节点
     * @return 兄弟节点个数
     */
    private int fetch(State parent, List<Map.Entry<Integer, State>> siblings) {
        if (parent.isAcceptable()) {
            State fakeNode = new State(-(parent.getDepth() + 1));  // 此节点是parent的子节点，同时具备parent的输出
            fakeNode.addEmit(parent.getLargestValueId());
            siblings.add(new AbstractMap.SimpleEntry<Integer, State>(0, fakeNode));
        }
        for (Map.Entry<Character, State> entry : parent.getSuccess().entrySet()) {
            siblings.add(new AbstractMap.SimpleEntry<Integer, State>(entry.getKey() + 1, entry.getValue()));
        }
        return siblings.size();
    }

    /**
     * 添加一个键
     *
     * @param keyword 键
     * @param index   值的下标
     */
    private void addKeyword(String keyword, int index) {
        State currentState = this.rootState;
        for (Character character : keyword.toCharArray()) {
            currentState = currentState.addState(character);
        }
        currentState.addEmit(index);
        trie.keylength[index] = keyword.length();
    }

    /**
     * 一系列键
     *
     * @param keywordSet
     */
    private void addAllKeyword(Collection<String> keywordSet) {
        int i = 0;
        for (String keyword : keywordSet) {
            addKeyword(keyword, i++);
        }
    }

    /**
     * 建立failure表
     */
    private void constructFailureStates() {
        trie.fail = new int[size + 1];
        trie.fail[1] = trie.base[0];
        trie.output = new int[size + 1][];
        Queue<State> queue = new LinkedBlockingDeque<State>();

        // 第一步，将深度为1的节点的failure设为根节点
        for (State depthOneState : this.rootState.getStates()) {
            depthOneState.setFailure(this.rootState, trie.fail);
            queue.add(depthOneState);
            constructOutput(depthOneState);
        }

        // 第二步，为深度 > 1 的节点建立failure表，这是一个bfs
        while (!queue.isEmpty()) {
            State currentState = queue.remove();

            for (Character transition : currentState.getTransitions()) {
                State targetState = currentState.nextState(transition);
                queue.add(targetState);

                State traceFailureState = currentState.failure();
                while (traceFailureState.nextState(transition) == null) {
                    traceFailureState = traceFailureState.failure();
                }
                State newFailureState = traceFailureState.nextState(transition);
                targetState.setFailure(newFailureState, trie.fail);
                targetState.addEmit(newFailureState.emit());
                constructOutput(targetState);
            }
        }
    }

    /**
     * 建立output表
     */
    private void constructOutput(State targetState) {
        Collection<Integer> emit = targetState.emit();
        if (emit == null || emit.size() == 0) {
            return;
        }
        int[] output = new int[emit.size()];
        Iterator<Integer> it = emit.iterator();
        for (int i = 0; i < output.length; ++i) {
            output[i] = it.next();
        }
        trie.output[targetState.getIndex()] = output;
    }

    private void buildDoubleArrayTrie(Set<String> keySet) {
        progress = 0;
        //keySize = keySet.size();
        resize(65536 * 32); // 32个双字节

        trie.base[0] = 1;
        nextCheckPos = 0;

        State root_node = this.rootState;

        List<Map.Entry<Integer, State>> siblings = new ArrayList<Map.Entry<Integer, State>>(root_node.getSuccess().entrySet().size());
        fetch(root_node, siblings);
        insert(siblings);
    }

    /**
     * 拓展数组
     *
     * @param new_capacity
     * @return
     */
    private void resize(int new_capacity) {
        if (new_capacity <= array_capacity) {
            return;
        }
        if (new_capacity - array_capacity < 1024 * 64) { //64K
            new_capacity = array_capacity + 1024 * 64;
        }
        int[] base2 = new int[new_capacity];
        int[] check2 = new int[new_capacity];

        System.arraycopy(trie.base, 0, base2, 0, array_capacity);
        System.arraycopy(trie.check, 0, check2, 0, array_capacity);

        trie.base = base2;
        trie.check = check2;

        array_capacity = new_capacity;
    }

    /**
     * 插入节点
     *
     * @param siblings 等待插入的兄弟节点
     * @return 插入位置
     */
    private int insert(List<Map.Entry<Integer, State>> siblings) {
        int begin = 0;
        int pos = Math.max(siblings.get(0).getKey() + 1, nextCheckPos) - 1;
        int nonzero_num = 0;
        int first = 0;

        if (array_capacity <= pos) {
            resize(pos + 1);
        }

        outer:
        // 此循环体的目标是找出满足base[begin + a1...an]  == 0的n个空闲空间,a1...an是siblings中的n个节点
        while (true) {
            pos++;

            if (array_capacity <= pos) {
                resize(pos + 1);
            }

            if (trie.check[pos] != 0) {
                nonzero_num++;
                continue;
            } else if (first == 0) {
                nextCheckPos = pos;
                first = 1;
            }

            begin = pos - siblings.get(0).getKey(); // 当前位置离第一个兄弟节点的距离
            if (array_capacity <= (begin + siblings.get(siblings.size() - 1).getKey())) {
                // progress can be zero // 防止progress产生除零错误
                //  double l = (1.05 > 1.0 * keySize / (progress + 1)) ? 1.05 : 1.0 * keySize / (progress + 1);
                resize((begin + siblings.get(siblings.size() - 1).getKey()) + 64 * 1024);
            }

            if (used.get(begin)) {
                continue;
            }

            for (int i = 1; i < siblings.size(); i++) {
                if (trie.check[begin + siblings.get(i).getKey()] != 0) {
                    continue outer;
                }
            }

            break;
        }

        // -- Simple heuristics --
        // if the percentage of non-empty contents in check between the
        // index
        // 'next_check_pos' and 'check' is greater than some constant value
        // (e.g. 0.9),
        // new 'next_check_pos' index is written by 'check'.
        if (1.0 * nonzero_num / (pos - nextCheckPos + 1) >= 0.95) {
            nextCheckPos = pos; // 从位置 next_check_pos 开始到 pos 间，如果已占用的空间在95%以上，下次插入节点时，直接从 pos 位置处开始查找
        }
        used.set(begin);

        size = (size > begin + siblings.get(siblings.size() - 1).getKey() + 1) ? size : begin + siblings.get(siblings.size() - 1).getKey() + 1;

        for (Map.Entry<Integer, State> sibling : siblings) {
            trie.check[begin + sibling.getKey()] = begin;
        }

        for (Map.Entry<Integer, State> sibling : siblings) {
            List<Map.Entry<Integer, State>> new_siblings = new ArrayList<Map.Entry<Integer, State>>(sibling.getValue().getSuccess().entrySet().size() + 1);

            if (fetch(sibling.getValue(), new_siblings) == 0)  // 一个词的终止且不为其他词的前缀，其实就是叶子节点
            {
                trie.base[begin + sibling.getKey()] = (-sibling.getValue().getLargestValueId() - 1);
                progress++;
            } else {
                int h = insert(new_siblings);   // dfs
                trie.base[begin + sibling.getKey()] = h;
            }
            sibling.getValue().setIndex(begin + sibling.getKey());
        }
        return begin;
    }

    /**
     * 释放空闲的内存
     */
    private void loseWeight() {
        int[] nbase = new int[size + 65535];
        System.arraycopy(trie.base, 0, nbase, 0, size);
        trie.base = nbase;

        int[] ncheck = new int[size + 65535];
        System.arraycopy(trie.check, 0, ncheck, 0, size);
        trie.check = ncheck;
    }

}

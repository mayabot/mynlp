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

package com.mayabot.nlp.collection.dat;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * DAT 构建器
 *
 * @author jimichan
 */
public class DoubleArrayTrieBuilder<V> {

    final int default_capacity = 256 * 1024; // 1M

    private int array_capacity = default_capacity;
    protected int[] check = new int[default_capacity];
    protected int[] base = new int[default_capacity];

    /**
     * base 和 check 的大小
     */
    protected int size;

    private BitSet used = new BitSet();

    private ArrayList<String> keyList;

    private int keySize;
    private int progress;
    private int nextCheckPos;
    int error_;

    public DoubleArrayTrie<V> build(TreeMap<String, V> map) {
        return build(Lists.newArrayList(map.keySet()), Lists.newArrayList(map.values()));
    }

    /**
     * 唯一的构建方法
     *
     * @param _key     值set，必须字典序
     * @param valueObj key的长度，应该设为_key.size
     * @return 是否出错
     */
    public DoubleArrayTrie<V> build(ArrayList<String> _key, ArrayList<V> valueObj) {
        keyList = _key;
        keySize = _key.size();
        progress = 0;

        base[0] = 1;
        nextCheckPos = 0;

        Node root_node = new Node();
        root_node.left = 0;
        root_node.right = keySize;
        root_node.depth = 0;

        List<Node> siblings = new ArrayList<Node>();
        fetch(root_node, siblings);
        insert(siblings);

        used = null;
        keyList = null;

        return new DoubleArrayTrie<>(valueObj, check, base);
    }

    /**
     * 要求已经按照key字典顺序排序
     * 每行的格式 word[空格]args
     *
     * @param lines
     * @param transFun
     * @return
     */
    public DoubleArrayTrie<V> buildOnSorted(String split, Iterable<String> lines, Function<String[], V> transFun) {

        ArrayList<String> key = Lists.newArrayList();
        ArrayList<V> valueList = Lists.newArrayList();

        final String[] empty = new String[0];
        for (String line : lines) {
            String[] list = line.split(split);
            key.add(list[0]);
            if (list.length == 1) {
                valueList.add(transFun.apply(empty));
            } else {
                String[] sublist = new String[list.length - 1];
                System.arraycopy(list, 1, sublist, 0, sublist.length);
                valueList.add(transFun.apply(sublist));
            }
        }
        return build(key, valueList);
    }

    public DoubleArrayTrie<V> buildNotSorted(String split, Iterable<String> lines, Function<String[], V> transFun) {

        TreeMap<String, V> map = new TreeMap<>();

        final String[] empty = new String[0];
        for (String line : lines) {
            String[] list = line.split(split);
            if (list.length == 1) {
                map.put(list[0], transFun.apply(empty));
            } else {
                String[] sublist = new String[list.length - 1];
                System.arraycopy(list, 1, sublist, 0, sublist.length);
                map.put(list[0], transFun.apply(sublist));
            }
        }
        return build(map);
    }

    /**
     * 插入节点
     *
     * @param siblings 等待插入的兄弟节点
     * @return 插入位置
     */
    private int insert(List<Node> siblings) {
        if (error_ < 0) {
            return 0;
        }

        int begin = 0;
        int pos = Math.max(siblings.get(0).code + 1, nextCheckPos) - 1;
        int nonzero_num = 0;
        int first = 0;

        if (array_capacity <= pos) {
            resize(pos + 1);
        }

        outer:
        // 此循环体的目标是找出满足base[begin + a1...an] == 0的n个空闲空间,a1...an是siblings中的n个节点
        while (true) {
            pos++;

            if (array_capacity <= pos) {
                resize(pos + 1);
            }

            if (check[pos] != 0) {
                nonzero_num++;
                continue;
            } else if (first == 0) {
                nextCheckPos = pos;
                first = 1;
            }

            begin = pos - siblings.get(0).code; // 当前位置离第一个兄弟节点的距离
            if (array_capacity <= (begin + siblings.get(siblings.size() - 1).code)) {
                // progress can be zero // 防止progress产生除零错误
//				double l = (1.05 > 1.0 * keySize / (progress + 1)) ? 1.05 : 1.0
//						* keySize / (progress + 1);
//				resize((int) (array_capacity * l));
                resize(begin + siblings.get(siblings.size() - 1).code + Character.MAX_VALUE);
            }

            if (used.get(begin)) {
                continue;
            }

            for (int i = 1; i < siblings.size(); i++) {
                if (check[begin + siblings.get(i).code] != 0) {
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
            nextCheckPos = pos; // 从位置 next_check_pos 开始到 pos
        }
        // 间，如果已占用的空间在95%以上，下次插入节点时，直接从 pos 位置处开始查找

        used.set(begin); //used[begin]=true

        size = (size > begin + siblings.get(siblings.size() - 1).code + 1) ? size
                : begin + siblings.get(siblings.size() - 1).code + 1;

        for (int i = 0; i < siblings.size(); i++) {
            check[begin + siblings.get(i).code] = begin;
            // System.out.println(this);
        }

        for (int i = 0; i < siblings.size(); i++) {
            List<Node> new_siblings = new ArrayList<Node>();

            if (fetch(siblings.get(i), new_siblings) == 0) // 一个词的终止且不为其他词的前缀
            {
//				base[begin + siblings.get(i).code] = (value != null) ? (-value[siblings
//						.get(i).left] - 1) : (-siblings.get(i).left - 1);
                base[begin + siblings.get(i).code] = (-siblings.get(i).left - 1);
                // System.out.println(this);

//				if (value != null && (-value[siblings.get(i).left] - 1) >= 0) {
//					error_ = -2;
//					return 0;
//				}

                progress++;
                // if (progress_func_) (*progress_func_) (progress,
                // keySize);
            } else {
                int h = insert(new_siblings); // dfs
                base[begin + siblings.get(i).code] = h;
                // System.out.println(this);
            }
        }
        return begin;
    }


    /**
     * 获取直接相连的子节点
     *
     * @param parent   父节点
     * @param siblings （子）兄弟节点
     * @return 兄弟节点个数
     */
    private int fetch(Node parent, List<Node> siblings) {
        if (error_ < 0) {
            return 0;
        }

        int prev = 0;

        for (int i = parent.left; i < parent.right; i++) {
            if (keyList.get(i).length() < parent.depth) {
                continue;
            }

            String tmp = keyList.get(i);

            int cur = 0;
            if (tmp.length() != parent.depth) {
                cur = (int) tmp.charAt(parent.depth) + 1;
            }

            if (prev > cur) {
                error_ = -3;
                return 0;
            }

            if (cur != prev || siblings.size() == 0) {
                Node tmp_node = new Node();
                tmp_node.depth = parent.depth + 1;
                tmp_node.code = cur;
                tmp_node.left = i;
                if (siblings.size() != 0) {
                    siblings.get(siblings.size() - 1).right = i;
                }

                siblings.add(tmp_node);
            }

            prev = cur;
        }

        if (siblings.size() != 0) {
            siblings.get(siblings.size() - 1).right = parent.right;
        }

        return siblings.size();
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
        if (new_capacity - array_capacity < 1024 * 1024) { //64K
            new_capacity = array_capacity + 1024 * 1024;
        }
        int[] base2 = new int[new_capacity];
        int[] check2 = new int[new_capacity];

        System.arraycopy(base, 0, base2, 0, array_capacity);
        System.arraycopy(check, 0, check2, 0, array_capacity);

        base = base2;
        check = check2;

        array_capacity = new_capacity;
    }

    private static class Node {
        int code;
        int depth;
        int left;
        int right;

        @Override
        public String toString() {
            return "Node{" + "code=" + code + ", depth=" + depth + ", left="
                    + left + ", right=" + right + '}';
        }
    }

    static int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : n + 1;
    }

}

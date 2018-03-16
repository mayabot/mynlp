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

/**
 * DoubleArrayTrie: Java implementation of Darts (Double-ARray Trie System)
 * <p/>
 * <p>
 * Copyright(C) 2001-2007 Taku Kudo &lt;taku@chasen.org&gt;<br />
 * Copyright(C) 2009 MURAWAKI Yugo &lt;murawaki@nlp.kuee.kyoto-u.ac.jp&gt;
 * Copyright(C) 2012 KOMIYA Atsushi &lt;komiya.atsushi@gmail.com&gt;
 * </p>
 * <p/>
 * <p>
 * The contents of this file may be used under the terms of either of the GNU
 * Lesser General Public License Version 2.1 or later (the "LGPL"), or the BSD
 * License (the "BSD").
 * </p>
 */

/*
 * 源代码参考和部分引用来自 https://github.com/hankcs/HanLP https://github.com/NLPchina/ansj_seg
 */
package com.mayabot.nlp.collection.dat;

import com.mayabot.nlp.collection.Trie;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.mayabot.nlp.utils.DataInOutputUtils.*;

/**
 * 双数组Trie树。
 * 字典树，
 * 用数字存放values。也就是每个项目都是有个整数id。
 */
public class DoubleArrayTrie<T> implements Trie<T> {


    public static <T> void write(
            DoubleArrayTrie<T> dat, DataOutput out, BiConsumer<T, DataOutput> biConsumer
    ) throws IOException {
        writeIntArray(dat.check, out);
        writeIntArray(dat.base, out);
        writeArrayList(dat.values, biConsumer, out);
    }

    public static <T> DoubleArrayTrie<T> read(
            DataInput in, Function<DataInput, T> supplier) throws IOException {
        int[] check = readIntArray(in);
        int[] base = readIntArray(in);
        ArrayList<T> values = readArrayList(in, supplier);
        //noinspection unchecked
        return new DoubleArrayTrie(values, check, base);
    }

    ArrayList<T> values;
    int[] check;
    int[] base;

    protected DoubleArrayTrie(ArrayList<T> values, int[] check, int[] base) {
        super();
        this.values = values;
        this.check = check;
        this.base = base;
    }

    /**
     * DAT的搜索器
     *
     * @param text   带计算的文本
     * @param offset 文本中的偏移量
     * @return
     */
    public DATMatcher<T> match(String text, int offset) {
        return new DATMatcher<T>(this, text, offset);
    }

    /**
     * DAT的搜索器
     *
     * @param text   带计算的文本
     * @param offset 文本中的偏移量
     * @return
     */
    public DATMatcher<T> match(char[] text, int offset) {
        return new DATMatcher<T>(this, text, offset);
    }

    public int getSize() {
        return values.size();
    }

    public int getNonzeroSize() {
        int result = 0;
        for (int i = 0; i < check.length; ++i) {
            if (check[i] != 0) {
                ++result;
            }
        }
        return result;
    }

    /**
     * 精确匹配
     *
     * @param key 键
     * @return 值
     */
    public int indexOf(CharSequence key) {
        return indexOf(key, 0, 0, 0);
    }

    public int indexOf(CharSequence key, int pos, int len, int nodePos) {
        if (len <= 0) {
            len = key.length();
        }
        if (nodePos <= 0) {
            nodePos = 0;
        }

        int result = -1;

        int b = base[nodePos];
        int p;

        for (int i = pos; i < pos + len; i++) {
            p = b + (int) (key.charAt(i)) + 1;
            if (b == check[p]) {
                b = base[p];
            } else {
                return result;
            }
        }

        p = b;
        int n = base[p];
        if (b == check[p] && n < 0) {
            result = -n - 1;
        }
        return result;
    }

    /**
     * 精确查询
     *
     * @param chars 键的char数组
     * @param pos   char数组的起始位置
     * @param len   键的长度
     *              开始查找的位置（本参数允许从非根节点查询）
     * @return 查到的节点代表的value ID，负数表示不存在
     */
    public int indexOf(char[] chars, int pos, int len) {
        return indexOf(chars, pos, len, 0);
    }

    /**
     * 精确查询
     *
     * @param keyChars 键的char数组
     * @param pos      char数组的起始位置
     * @param len      键的长度
     * @param nodePos  开始查找的位置（本参数允许从非根节点查询）
     * @return 查到的节点代表的value ID，负数表示不存在
     */

    public int indexOf(char[] keyChars, int pos, int len, int nodePos) {
        if (len <= 0) {
            len = keyChars.length;
        }
        if (nodePos <= 0) {
            nodePos = 0;
        }

        int result = -1;

        int b = base[nodePos];
        int p;

        for (int i = pos; i < len + pos; i++) {
            p = b + (int) (keyChars[i]) + 1;
            if (b == check[p]) {
                b = base[p];
            } else {
                return result;
            }
        }

        p = b;
        int n = base[p];
        if (b == check[p] && n < 0) {
            result = -n - 1;
        }
        return result;
    }


    /**
     * 精确查询
     *
     * @param key 键
     * @return 值
     */
    @Override
    public T get(CharSequence key) {
        int index = indexOf(key);
        if (index >= 0) {
            return getValueAt(index);
        }

        return null;
    }

    public T get(CharSequence key, int offset, int length) {
        int index = indexOf(key, offset, length, 0);
        if (index >= 0) {
            return getValueAt(index);
        }

        return null;
    }

    @Override
    public T get(char[] key) {
        int index = indexOf(key, 0, key.length, 0);
        if (index >= 0) {
            return getValueAt(index);
        }

        return null;
    }

    @Override
    public T get(char[] key, int offset, int len) {

        int index = indexOf(key, offset, len, 0);
        if (index >= 0) {
            return getValueAt(index);
        }
        return null;
    }

    public List<Integer> commonPrefixSearch(String key) {
        return commonPrefixSearch(key, 0, 0, 0);
    }

    /**
     * 前缀查询
     *
     * @param key     查询字串
     * @param pos     字串的开始位置
     * @param len     字串长度
     * @param nodePos base中的开始位置
     * @return 一个含有所有下标的list
     */
    public List<Integer> commonPrefixSearch(String key, int pos, int len,
                                            int nodePos) {
        if (len <= 0) {
            len = key.length();
        }

        if (nodePos <= 0) {
            nodePos = 0;
        }

        List<Integer> result = new ArrayList<Integer>();

        int b = base[nodePos];
        int n;
        int p;

        for (int i = pos; i < len; i++) {
            p = b + (int) (key.charAt(i)) + 1; // 状态转移 p = base[char[i-1]] +
            // char[i] + 1
            if (b == check[p]) // base[char[i-1]] == check[base[char[i-1]] +
                // char[i] + 1]
            {
                b = base[p];
            } else {
                return result;
            }
            p = b;
            n = base[p];
            if (b == check[p] && n < 0) // base[p] == check[p] && base[p] < 0
            // 查到一个词
            {
                result.add(-n - 1);
            }
        }

        return result;
    }

    public List<Integer> commonPrefixSearch(char[] key) {
        return commonPrefixSearch(key, 0, 0, 0);
    }

    public List<Integer> commonPrefixSearch(char[] key, int offset) {
        return commonPrefixSearch(key, offset, 0, 0);
    }

    public List<Integer> commonPrefixSearch(char[] key, int pos, int len,
                                            int nodePos) {
        if (len <= 0) {
            len = key.length;
        }

        if (nodePos <= 0) {
            nodePos = 0;
        }

        List<Integer> result = new ArrayList<Integer>();

        int b = base[nodePos];
        int n;
        int p;

        for (int i = pos; i < len; i++) {
            p = b + (int) (key[i]) + 1; // 状态转移 p = base[char[i-1]] +
            // char[i] + 1
            if (b == check[p]) // base[char[i-1]] == check[base[char[i-1]] +
                // char[i] + 1]
            {
                b = base[p];
            } else {
                return result;
            }
            p = b;
            n = base[p];
            if (b == check[p] && n < 0) // base[p] == check[p] && base[p] < 0
            { // 查到一个词
                result.add(-n - 1);
            }
        }

        return result;
    }


    /**
     * 优化的前缀查询，可以复用字符数组
     *
     * @param keyChars
     * @param begin
     * @return
     */
    public LinkedList<Map.Entry<String, T>> commonPrefixSearchWithValue(char[] keyChars, int begin) {
        int len = keyChars.length;
        LinkedList<Map.Entry<String, T>> result = new LinkedList<Map.Entry<String, T>>();
        int b = base[0];
        int n;
        int p;

        for (int i = begin; i < len; ++i) {
            p = b;
            n = base[p];
            if (b == check[p] && n < 0)         // base[p] == check[p] && base[p] < 0 查到一个词
            {
                result.add(new AbstractMap.SimpleEntry<String, T>(new String(keyChars, begin, i - begin), values.get(-n - 1)));
            }

            p = b + (int) (keyChars[i]) + 1;    // 状态转移 p = base[char[i-1]] + char[i] + 1
            // 下面这句可能产生下标越界，不如改为if (p < size && b == check[p])，或者多分配一些内存
            if (b == check[p])                  // base[char[i-1]] == check[base[char[i-1]] + char[i] + 1]
            {
                b = base[p];
            } else {
                return result;
            }
        }

        p = b;
        n = base[p];

        if (b == check[p] && n < 0) {
            result.add(new AbstractMap.SimpleEntry<String, T>(new String(keyChars, begin, len - begin), values.get(-n - 1)));
        }

        return result;
    }


    /**
     * 树叶子节点个数
     *
     * @return
     */
    public int size() {
        return values.size();
    }

//    /**
//     * 获取check数组引用，不要修改check
//     *
//     * @return
//     */
//    public int[] getCheck() {
//        return check;
//    }

    /**
     * 获取base数组引用，不要修改base
     *
     * @return
     */
    private int[] getBase() {
        return base;
    }

    /**
     * 获取index对应的值
     *
     * @param index
     * @return
     */
    public T getValueAt(int index) {
        return values.get(index);
    }


//    public ImmutableList<T> getValueArray(T[] a) {
//        return ImmutableList.copyOf(this.values);
//        // int size = values.size();
//        // if (a.length < size)
//        // a = (T[]) java.lang.reflect.Array.newInstance(a.getClass()
//        // .getComponentType(), size);
//        // System.arraycopy(values, 0, a, 0, size);
//        // return a;
//    }


    @Override
    public boolean containsKey(String key) {
        return indexOf(key) >= 0;
    }

    /**
     * 沿着路径转移状态
     *
     * @param path
     * @return
     */
    protected int transition(String path) {
        return transition(path.toCharArray());
    }

    /**
     * 沿着节点转移状态
     *
     * @param path
     * @return
     */
    protected int transition(char[] path) {
        int b = base[0];
        int p;

        for (int i = 0; i < path.length; ++i) {
            p = b + (int) (path[i]) + 1;
            if (b == check[p]) {
                b = base[p];
            } else {
                return -1;
            }
        }

        p = b;
        return p;
    }


    /**
     * 转移状态
     *
     * @param c
     * @param from
     * @return
     */
    public int transition(char c, int from) {
        int b = from;
        int p;

        p = b + (int) (c) + 1;
        if (b == check[p]) {
            b = base[p];
        } else {
            return -1;
        }

        return b;
    }

    /**
     * 沿着路径转移状态
     *
     * @param path 路径
     * @param from 起点（根起点为base[0]=1）
     * @return 转移后的状态（双数组下标）
     */
    public int transition(String path, int from) {
        int b = from;
        int p;

        for (int i = 0; i < path.length(); ++i) {
            p = b + (int) (path.charAt(i)) + 1;
            if (b == check[p]) {
                b = base[p];
            } else {
                return -1;
            }
        }

        p = b;
        return p;
    }


    /**
     * 检查状态是否对应输出
     *
     * @param state 双数组下标
     * @return 对应的值，null表示不输出
     */
    public T output(int state) {
        if (state < 0) {
            return null;
        }
        int n = base[state];
        if (state == check[state] && n < 0) {
            return values.get(-n - 1);
        }
        return null;
    }

    /**
     * 转移状态
     *
     * @param current
     * @param c
     * @return
     */
    protected int transition(int current, char c) {
        int b = base[current];
        int p;

        p = b + c + 1;
        if (b == check[p]) {
            b = base[p];
        } else {
            return -1;
        }

        p = b;
        return p;
    }

    /**
     * 更新某个键对应的值
     *
     * @param key   键
     * @param value 值
     * @return 是否成功（失败的原因是没有这个键）
     */
    public boolean set(String key, T value) {
        int index = indexOf(key);
        if (index >= 0) {
            values.set(index, value);
            return true;
        }
        return false;
    }

    /**
     * 从值数组中提取下标为index的值<br>
     * 注意为了效率，此处不进行参数校验
     *
     * @param index 下标
     * @return 值
     */
    public T get(int index) {
        return values.get(index);
    }


}
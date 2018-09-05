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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.mayabot.nlp.utils.DataInOutputUtils.*;

/**
 * 基于双数组Trie树的AhoCorasick自动机
 *
 * FROM HANLP https://github.com/hankcs/HanLP
 *
 * @author hankcs
 */
public class AhoCorasickDoubleArrayTrie<V> {
    /**
     * 双数组值check
     */
    int[] check;

    /**
     * 双数组之base
     */
    int[] base;

    /**
     * fail表
     */
    int[] fail;
    /**
     * 输出表
     */
    int[][] output;
    /**
     * 保存value
     */
    ArrayList<V> values;

    /**
     * 每个key的长度
     */
    int[] keylength;

    AhoCorasickDoubleArrayTrie() {

    }

    public static <T> void write(
            AhoCorasickDoubleArrayTrie<T> dat, DataOutput out, BiConsumer<T, DataOutput> biConsumer) throws IOException {
        writeIntArray(dat.check, out);
        writeIntArray(dat.keylength, out);
        writeIntArray(dat.base, out);
        writeIntArray(dat.fail, out);
        writeArrayList(dat.values, biConsumer, out);
        writeIntArray(dat.output, out);

    }

    @SuppressWarnings("unchecked")
    public static <T> AhoCorasickDoubleArrayTrie<T> read(DataInput in, Function<DataInput, T> function) throws IOException {
        AhoCorasickDoubleArrayTrie x = new AhoCorasickDoubleArrayTrie();

        x.check = readIntArray(in);
        x.keylength = readIntArray(in);
        x.base = readIntArray(in);
        x.fail = readIntArray(in);
        x.values = readArrayList(in, function);
        x.output = readDoubleIntArray(in);
        return x;
    }

    /**
     * 匹配母文本
     *
     * @param text 一些文本
     * @return 一个pair列表
     */
    public List<Hit<V>> parseText(CharSequence text) {
        int position = 1;
        int currentState = 0;
        List<Hit<V>> collectedEmits = new LinkedList<Hit<V>>();
        for (int i = 0; i < text.length(); ++i) {
            currentState = getState(currentState, text.charAt(i));
            storeEmits(position, currentState, collectedEmits);
            ++position;
        }

        return collectedEmits;
    }

    /**
     * 处理文本
     *
     * @param text      文本
     * @param processor 处理器
     */
    public void parseText(CharSequence text, IHit<V> processor) {
        int position = 1;
        int currentState = 0;
        for (int i = 0; i < text.length(); ++i) {
            currentState = getState(currentState, text.charAt(i));
            int[] hitArray = output[currentState];
            if (hitArray != null) {
                for (int hit : hitArray) {
                    processor.hit(position - keylength[hit], position, values.get(hit));
                }
            }
            ++position;
        }
    }

    /**
     * 处理文本
     *
     * @param text
     * @param processor
     */
    public void parseText(char[] text, IHit<V> processor) {
        int position = 1;
        int currentState = 0;
        for (char c : text) {
            currentState = getState(currentState, c);
            int[] hitArray = output[currentState];
            if (hitArray != null) {
                for (int hit : hitArray) {
                    processor.hit(position - keylength[hit], position, values.get(hit));
                }
            }
            ++position;
        }
    }

    /**
     * 处理文本
     *
     * @param text
     * @param processor
     */
    public void parseText(char[] text, IHitFull<V> processor) {
        int position = 1;
        int currentState = 0;
        for (char c : text) {
            currentState = getState(currentState, c);
            int[] hitArray = output[currentState];
            if (hitArray != null) {
                for (int hit : hitArray) {
                    processor.hit(position - keylength[hit], position, values.get(hit), hit);
                }
            }
            ++position;
        }
    }

    /**
     * 获取值
     *
     * @param key 键
     * @return
     */
    public V get(String key) {
        int index = exactMatchSearch(key);
        if (index >= 0) {
            return values.get(index);
        }

        return null;
    }

    /**
     * 更新某个键对应的值
     *
     * @param key   键
     * @param value 值
     * @return 是否成功（失败的原因是没有这个键）
     */
    public boolean set(String key, V value) {
        int index = exactMatchSearch(key);
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
    public V get(int index) {
        return values.get(index);
    }

    /**
     * 转移状态，支持failure转移
     *
     * @param currentState
     * @param character
     * @return
     */
    private int getState(int currentState, char character) {
        int newCurrentState = transitionWithRoot(currentState, character); // 先按success跳转
        while (newCurrentState == -1) // 跳转失败的话，按failure跳转
        {
            currentState = fail[currentState];
            newCurrentState = transitionWithRoot(currentState, character);
        }
        return newCurrentState;
    }

    /**
     * 保存输出
     *
     * @param position
     * @param currentState
     * @param collectedEmits
     */
    private void storeEmits(int position, int currentState,
                            List<Hit<V>> collectedEmits) {
        int[] hitArray = output[currentState];
        if (hitArray != null) {
            for (int hit : hitArray) {
                collectedEmits.add(new Hit<V>(position - keylength[hit], position,
                        values.get(hit)));
            }
        }
    }

    /**
     * 转移状态
     *
     * @param current
     * @param c
     * @return
     */
    protected int transition(int current, char c) {
        int b = current;
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
     * c转移，如果是根节点则返回自己
     *
     * @param nodePos
     * @param c
     * @return
     */
    protected int transitionWithRoot(int nodePos, char c) {
        int b = base[nodePos];
        int p;

        p = b + c + 1;
        if (b != check[p]) {
            if (nodePos == 0) {
                return 0;
            }
            return -1;
        }

        return p;
    }

    /**
     * 精确匹配
     *
     * @param key 键
     * @return 值的下标
     */
    public int exactMatchSearch(String key) {
        return exactMatchSearch(key, 0, 0, 0);
    }

    /**
     * 精确匹配
     *
     * @param key
     * @param pos
     * @param len
     * @param nodePos
     * @return
     */
    private int exactMatchSearch(String key, int pos, int len, int nodePos) {
        if (len <= 0) {
            len = key.length();
        }
        if (nodePos <= 0) {
            nodePos = 0;
        }

        int result = -1;

        int b = base[nodePos];
        int p;

        for (int i = pos; i < len; i++) {
            p = b + key.codePointAt(i) + 1;
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
     * @param keyChars 键的char数组
     * @param pos      char数组的起始位置
     * @param len      键的长度
     * @param nodePos  开始查找的位置（本参数允许从非根节点查询）
     * @return 查到的节点代表的value ID，负数表示不存在
     */
    int exactMatchSearch(char[] keyChars, int pos, int len, int nodePos) {
        int result = -1;

        int b = base[nodePos];
        int p;

        for (int i = pos; i < len; i++) {
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
     * 大小，即包含多少个模式串
     *
     * @return
     */
    public int size() {
        return values.size();
    }
}

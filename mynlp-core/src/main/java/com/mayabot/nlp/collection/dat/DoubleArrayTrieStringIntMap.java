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
 * DoubleArrayTrieMap: Java implementation of Darts (Double-ARray Trie System)
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

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import static com.mayabot.nlp.utils.DataInOutputUtils.readIntArray;
import static com.mayabot.nlp.utils.DataInOutputUtils.writeIntArray;

/**
 * 【String int】的map
 */
public class DoubleArrayTrieStringIntMap {

    private int[] values;

    private DoubleArrayTrie dat;

    /**
     * 从IO里面恢复
     *
     * @param in
     * @throws IOException
     */
    public DoubleArrayTrieStringIntMap(
            DataInput in) throws IOException {
        DoubleArrayTrie dat = new DoubleArrayTrie(in);
        int[] values = readIntArray(in);

        this.dat = dat;
        this.values = values;
    }

    /**
     * @param dat
     * @param values
     */
    public DoubleArrayTrieStringIntMap(DoubleArrayTrie dat, int[] values) {
        this.values = values;
        this.dat = dat;
    }

    /**
     * @param keys   一定是字典有序
     * @param values
     */
    public DoubleArrayTrieStringIntMap(ArrayList<String> keys, int[] values) {
        this(new DoubleArrayTrie(keys), values);
    }

    public DoubleArrayTrieStringIntMap(TreeMap<String, Integer> map) {
        ArrayList<String> keys = Lists.newArrayList(map.keySet());
        int[] values = Ints.toArray(Lists.newArrayList(map.values()));
        this.dat = new DoubleArrayTrie(keys);
        this.values = values;
    }

    public void save(DataOutput out) throws IOException {
        dat.write(out);
        writeIntArray(values, out);
    }

    /**
     * DAT的搜索器
     *
     * @param text   带计算的文本
     * @param offset 文本中的偏移量
     * @return
     */
    public DATMapMatcherInt match(String text, int offset) {
        return new DATMapMatcherInt(dat.matcher(text, offset));
    }

    /**
     * DAT的搜索器
     *
     * @param text   带计算的文本
     * @param offset 文本中的偏移量
     * @return
     */
    public DATMapMatcherInt match(char[] text, int offset) {
        return new DATMapMatcherInt(dat.matcher(text, offset));
    }

    public class DATMapMatcherInt {

        DATMatcher datMater;

        public DATMapMatcherInt(DATMatcher datMater) {
            this.datMater = datMater;
        }

        public boolean next() {
            return datMater.next();
        }

        public int getBegin() {
            return datMater.getBegin();
        }

        public int getLength() {
            return datMater.getLength();
        }

        public int getValue() {
            int index = datMater.getIndex();
            if (index == -1) {
                return -1;
            } else {
                return values[index];
            }
        }

        public int getIndex() {
            return datMater.getIndex();
        }
    }


    /**
     * 树叶子节点个数
     *
     * @return
     */
    public int size() {
        return values.length;
    }

    /**
     * 精确匹配
     *
     * @param key 键
     * @return 值
     */
    public int indexOf(CharSequence key) {
        return dat.indexOf(key, 0, 0, 0);
    }

    public int indexOf(CharSequence key, int pos, int len, int nodePos) {
        return dat.indexOf(key, pos, len, nodePos);
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
        return dat.indexOf(chars, pos, len, 0);
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
        return dat.indexOf(keyChars, pos, len, nodePos);
    }


    /**
     * 精确查询
     *
     * @param key 键
     * @return 值
     */
    public int get(CharSequence key) {
        int index = indexOf(key);
        if (index >= 0) {
            return getValueAt(index);
        }
        return -1;
    }

    public int get(CharSequence key, int offset, int length) {
        int index = indexOf(key, offset, length, 0);
        if (index >= 0) {
            return getValueAt(index);
        }

        return -1;
    }

    public int get(char[] key) {
        int index = indexOf(key, 0, key.length, 0);
        if (index >= 0) {
            return getValueAt(index);
        }

        return -1;
    }

    public int get(char[] key, int offset, int len) {

        int index = indexOf(key, offset, len, 0);
        if (index >= 0) {
            return getValueAt(index);
        }
        return -1;
    }

    /**
     * 获取index对应的值
     *
     * @param index
     * @return
     */
    public int getValueAt(int index) {
        return values[index];
    }

    public boolean containsKey(String key) {
        return indexOf(key) >= 0;
    }

    /**
     * 更新某个键对应的值
     *
     * @param key   键
     * @param value 值
     * @return 是否成功（失败的原因是没有这个键）
     */
    public boolean set(String key, int value) {
        int index = indexOf(key);
        if (index >= 0) {
            values[index] = value;
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
    public int get(int index) {
        return values[index];
    }

}
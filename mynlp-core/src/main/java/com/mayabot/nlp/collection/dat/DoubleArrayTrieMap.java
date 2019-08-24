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

import com.mayabot.nlp.collection.Trie;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.mayabot.nlp.utils.DataInOutputUtils.readArrayList;
import static com.mayabot.nlp.utils.DataInOutputUtils.writeArrayList;

/**
 * 双数组Trie树。
 * 字典树，
 * 用数字存放values。也就是每个项目都是有个整数id。
 * @author jimichan
 */
public class DoubleArrayTrieMap<T> implements Trie<T> {

    private ArrayList<T> values;

    private DoubleArrayTrie dat;

    /**
     * 从IO里面恢复
     *
     * @param in
     * @param supplier
     * @throws IOException
     */
    public DoubleArrayTrieMap(
            DataInput in, Function<DataInput, T> supplier) throws IOException {
        DoubleArrayTrie dat = new DoubleArrayTrie(in);
        ArrayList<T> values = readArrayList(in, supplier);

        this.dat = dat;
        this.values = values;
    }

    /**
     * @param dat
     * @param values
     */
    public DoubleArrayTrieMap(DoubleArrayTrie dat, ArrayList<T> values) {
        this.values = values;
        this.dat = dat;
    }

    public DoubleArrayTrieMap(TreeMap<String, T> treeMap) {
        ArrayList<String> keys = new ArrayList<>(treeMap.size());
        ArrayList<T> values = new ArrayList<>(treeMap.size());
        treeMap.forEach((a,b)->{
            keys.add(a);
            values.add(b);
        });
        this.dat = new DoubleArrayTrie(keys);
        this.values = values;
    }

    public DoubleArrayTrieMap(List<String> keys, ArrayList<T> values) {
        this(new DoubleArrayTrie(keys), values);
    }

    public void save(DataOutput out, BiConsumer<T, DataOutput> biConsumer) throws IOException {
        dat.write(out);
        writeArrayList(values, biConsumer, out);
    }

    /**
     * DAT的搜索器
     *
     * @param text   带计算的文本
     * @param offset 文本中的偏移量
     * @return DATMapMatcher
     */
    public DATMapMatcher<T> match(String text, int offset) {
        return new DATMapMatcherInner(dat.matcher(text, offset));
    }

    /**
     * DAT的搜索器
     *
     * @param text   带计算的文本
     * @param offset 文本中的偏移量
     * @return DATMapMatcher
     */
    public DATMapMatcher<T> match(char[] text, int offset) {
        return new DATMapMatcherInner(dat.matcher(text, offset));
    }

    class DATMapMatcherInner<T> implements DATMapMatcher<T> {

        DATMatcher datMater;

        public DATMapMatcherInner(DATMatcher datMater) {
            this.datMater = datMater;
        }

        @Override
        public boolean next() {
            return datMater.next();
        }

        @Override
        public int getBegin() {
            return datMater.getBegin();
        }

        @Override
        public int getLength() {
            return datMater.getLength();
        }

        @Override
        public T getValue() {
            int index = datMater.getIndex();
            if (index == -1) {
                return null;
            } else {
                return (T) values.get(index);
            }
        }

        @Override
        public int getIndex() {
            return datMater.getIndex();
        }
    }


    /**
     * 树叶子节点个数
     *
     * @return DATMapMatcher
     */
    public int size() {
        return values.size();
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

    /**
     * 获取index对应的值
     *
     * @param index
     * @return DATMapMatcher
     */
    public T getValueAt(int index) {
        return values.get(index);
    }

    @Override
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
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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static com.mayabot.nlp.utils.DataInOutputUtils.readIntArray;
import static com.mayabot.nlp.utils.DataInOutputUtils.writeIntArray;

/**
 * 双数组Trie树。
 * 主要解决的给一个已经排序的list，快速查询指定的String，的下标
 * 字典树，
 * 用数字存放values。也就是每个项目都是有个整数id。
 */
public class DATArrayIndex {

    public static void main(String[] args) {
        TreeSet<String> tr = Sets.newTreeSet();
        tr.add("a");
        tr.add("b");

        ArrayList<String> list = Lists.newArrayList(tr);


        DATArrayIndex index = new DATArrayIndex(list);

        System.out.println(index.indexOf("a"));
        System.out.println(index.indexOf("b"));
        System.out.println(index.indexOf("c"));
    }

    public int[] check;
    public int[] base;
    private int size;

    public DATArrayIndex(DataInput in) throws IOException {
        size = in.readInt();
        base = readIntArray(in);
        check = readIntArray(in);
    }

    public DATArrayIndex(TreeSet<String> set) {
        this(Lists.newArrayList(set));
    }

    public DATArrayIndex(List<String> sortedKeys) {
        DATDoubleArrayMaker datDoubleArrayMaker = new DATDoubleArrayMaker(sortedKeys);
        datDoubleArrayMaker.build();

        size = sortedKeys.size();
        check = datDoubleArrayMaker.getCheck();
        base = datDoubleArrayMaker.getBase();
    }

//    public DATArrayIndex(int[] check, int[] base, int size) {
//        super();
//        this.check = check;
//        this.base = base;
//        this.size = 0;
//    }

    public void write(DataOutput out) throws IOException {
        out.writeInt(size);
        writeIntArray(base, out);
        writeIntArray(check, out);
    }


    /**
     * 树叶子节点个数
     *
     * @return
     */
    public int size() {
        return size;
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

    public boolean containsKey(String key) {
        return indexOf(key) >= 0;
    }
}



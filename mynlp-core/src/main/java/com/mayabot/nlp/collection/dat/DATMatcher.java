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

package com.mayabot.nlp.collection.dat;

/**
 * 一个搜索工具（注意，当调用next()返回false后不应该继续调用next()，除非reset状态）
 * <p>
 * DAT的匹配器是一个多匹配器，把各种可能都计算出来
 */
public class DATMatcher<V> {

    /**
     * key的起点(在原始文本中)
     */
    private int begin;
    /**
     * key的长度
     */
    private int length;
    /**
     * key的字典序坐标
     */
    private int index;
    /**
     * key对应的value
     */
    private V value;
    /**
     * 传入的字符数组
     */
    private char[] charArray;
    /**
     * 上一个node位置
     */
    private int last;
    /**
     * 上一个字符的下标
     */
    private int i;
    /**
     * charArray的长度，效率起见，开个变量
     */
    private int arrayLength;

    private DoubleArrayTrie<V> dat;

    /**
     * 构造一个双数组搜索工具
     *
     * @param offset 搜索的起始位置
     * @param text   搜索的目标字符数组
     */
    DATMatcher(DoubleArrayTrie<V> dat, String text, int offset) {
        this(dat, text.toCharArray(), offset);
    }

    DATMatcher(DoubleArrayTrie<V> dat, char[] charArray, int offset) {
        this.charArray = charArray;
        this.dat = dat;
        i = offset;
        last = dat.base[0];
        arrayLength = charArray.length;
        // A trick，如果文本长度为0的话，调用next()时，会带来越界的问题。
        // 所以我要在第一次调用next()的时候触发begin == arrayLength进而返回false。
        // 当然也可以改成begin >= arrayLength，不过我觉得操作符>=的效率低于==
        if (arrayLength == 0)
            begin = -1;
        else
            begin = offset;
    }

    /**
     * 取出下一个命中输出
     *
     * @return 是否命中，当返回false表示搜索结束，否则使用公开的成员读取命中的详细信息
     */
    public boolean next() {
        int b = last;
        int n;
        int p;

        for (; ; ++i) {
            if (i == arrayLength) // 指针到头了，将起点往前挪一个，重新开始，状态归零
            {
                ++begin;
                if (begin == arrayLength)
                    break;
                i = begin;
                b = dat.base[0];
            }
            p = b + (int) (charArray[i]) + 1; // 状态转移 p = base[char[i-1]] +
            // char[i] + 1
            if (b == dat.check[p]) // base[char[i-1]] == check[base[char[i-1]] +
                // char[i] + 1]
                b = dat.base[p]; // 转移成功
            else {
                i = begin; // 转移失败，也将起点往前挪一个，重新开始，状态归零
                ++begin;
                if (begin == arrayLength)
                    break;
                b = dat.base[0];
                continue;
            }
            p = b;
            n = dat.base[p];
            if (b == dat.check[p] && n < 0) // base[p] == check[p] && base[p] <
            // 0
            // 查到一个词
            {
                length = i - begin + 1;
                index = -n - 1;
                value = dat.values.get(index);
                last = b;
                ++i;
                return true;
            }
        }

        return false;
    }

    public int getBegin() {
        return begin;
    }

//	public void setBegin(int begin) {
//		this.begin = begin;
//	}

    public int getLength() {
        return length;
    }

    //public void setLength(int length) {
//		this.length = length;
//	}

    public V getValue() {
        return value;
    }

//	public void setValue(V value) {
//		this.value = value;
//	}

    public int getIndex() {
        return index;
    }
}
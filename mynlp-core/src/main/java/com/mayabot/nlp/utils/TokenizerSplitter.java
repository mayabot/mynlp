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

package com.mayabot.nlp.utils;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * 取得文本中的一个个文字段落的from，to
 * <p>
 * 调用next之后=true，才可以方法 from() and to()方法
 * 如果英文的话，返回的就是单词
 *
 * @author jimichan
 */
public class TokenizerSplitter {

    //包含index
    private int fromIndex = -1;
    //(不包含)
    private int toIndex = -1;

    private CharSequence sequence;
    private int length;

    public static TokenizerSplitter create(CharSequence sequence) {
        return new TokenizerSplitter(sequence);
    }

    private TokenizerSplitter(CharSequence sequence) {
        rest(sequence);
    }

    public void rest(CharSequence sequence) {
        this.sequence = sequence;
        this.length = sequence.length();
    }

    private int point = 0;

    /**
     * 移动到下一游标
     *
     * @return
     */
    public boolean next() {

        if (point == length) { //最后一个也是标点
            return false;
        }

        //找到第一个不是标点的字母
        while (point < length && isSpliter(sequence.charAt(point))) {
            point++;
        }

        if (point == length) { //最后一个也是标点
            return false;
        }

        //记录start位置
        this.fromIndex = point;

        //找到下一个标点符号的位置
        while (point < length && !isSpliter(sequence.charAt(point))) {
            point++;
        }

        this.toIndex = point;

        return true;
    }

    public CharSequence group() {
        return sequence.subSequence(fromIndex, toIndex);
    }

    public final int from() {
        return fromIndex;
    }

    public final int to() {
        return toIndex;
    }

    /**
     * 是否段落切分器
     *
     * @param c
     * @return
     */
    private boolean isSpliter(char c) {
        return Characters.isPunctuation(c);
    }

    public static List<String> parts(String string) {
        TokenizerSplitter p = create(string);
        ArrayList<String> list = Lists.newArrayList();
        while (p.next()) {
            list.add((String) p.group());
        }
        return list;
    }

    public static void main(String[] args) {
        System.out.println(parts(""));
        System.out.println(parts(",abc,efg"));
        System.out.println(parts(",,abc,efg."));
        System.out.println(parts(",,abc efg."));
        System.out.println(parts("abcefg"));
        System.out.println(parts("ou may skip through a book, reading only those passages concerned  "));
        System.out.println(parts("你可以跳读一本书，只拣那些有关的段落读一下即可。"));
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            parts("你可以跳读一本书，只拣那些有关的段落读一下即可。");
        }
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
    }

}

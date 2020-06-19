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

package com.mayabot.nlp.collection.bintrie;

import java.util.List;

/**
 * 算法改造来自Ansj开源的分词项目。
 * 匹配器，可以扫描一次文本，获得词典中的最大前向匹配或者全匹配。
 *
 * @param <T>
 * @author jimichan
 * @author ansj
 */
public class TrieTreeForwardMaxMatcher<T> implements TrieTreeMatcher<T> {

    private static final String EMPTY_STRING = "";

    private final BinTrieTree<T> tree;

    private int offset;
    private int root = 0;
    private int i = this.root;
    private boolean isBack = false;
    private BinTrieNode<T> branch;
    private String text;
    private String str;
    private int tempOffset;

    private final int len;

    private T param;

    TrieTreeForwardMaxMatcher(BinTrieTree<T> tree, String content) {
        this.text = content;
        this.tree = tree;
        this.branch = tree;
        this.len = content.length();
    }


    /**
     * 前向最大長度匹配
     *
     * @return String
     */
    @Override
    public String next() {
        String temp = this.frontWordNext();
        while (EMPTY_STRING.equals(temp)) {
            temp = this.frontWordNext();
        }
        return temp;
    }

    private String frontWordNext() {

        for (int up = len + 1; this.i < up; this.i++) {
            if (i == len) {
                this.branch = null;
            } else {
                this.branch = this.branch.findChild(this.text.charAt(this.i));
            }
            if (this.branch == null) {
                this.branch = this.tree;
                if (this.isBack) {
                    this.offset = this.root;

                    this.str = this.text.substring(this.root, this.root + this.tempOffset);
                    //this.str = new String(this.chars, this.root, this.tempOffset);

                    if ((this.root > 0) && (isE(this.text.charAt(this.root - 1))) && (isE(this.str.charAt(0)))) {
                        this.str = EMPTY_STRING;
                    }

                    if ((this.str.length() != 0) && (this.root + this.tempOffset < this.text.length()) && (isE(this.str.charAt(this.str.length() - 1)))
                            && (isE(this.text.charAt(this.root + this.tempOffset)))) {
                        this.str = EMPTY_STRING;
                    }
                    if (this.str.length() == 0) {
                        this.root += 1;
                        this.i = this.root;
                    } else {
                        this.i = (this.root + this.tempOffset);
                        this.root = this.i;
                    }
                    this.isBack = false;

                    if (EMPTY_STRING.equals(this.str)) {
                        return EMPTY_STRING;
                    }
                    return this.str;
                }
                this.i = this.root;
                this.root += 1;
            } else {
                switch (this.branch.getStatus()) {
                    case AbstractTrieNode.Status_Continue:
                        this.isBack = true;
                        this.tempOffset = (this.i - this.root + 1);
                        this.param = this.branch.getValue();
                        break;
                    case AbstractTrieNode.Status_End:
                        this.offset = this.root;
                        //this.str = new String(this.chars, this.root, this.i - this.root + 1);
                        this.str = this.text.substring(this.root, this.i + 1);
                        String temp = this.str;

                        if ((this.root > 0) && (isE(this.text.charAt(this.root - 1))) && (isE(this.str.charAt(0)))) {
                            this.str = EMPTY_STRING;
                        }

                        if ((this.str.length() != 0) && (this.i + 1 < this.text.length()) && (isE(this.str.charAt(this.str.length() - 1)))
                                && (isE(this.text.charAt(this.i + 1)))) {
                            this.str = EMPTY_STRING;
                        }
                        this.param = this.branch.getValue();
                        this.branch = this.tree;
                        this.isBack = false;
                        if (temp.length() > 0) {
                            this.i += 1;
                            this.root = this.i;
                        } else {
                            this.i = (this.root + 1);
                        }
                        if (EMPTY_STRING.equals(this.str)) {
                            return EMPTY_STRING;
                        }
                        return this.str;
                }
            }
        }
        this.tempOffset += len;
        return null;
    }

    private boolean isE(char c) {
        return c == '.' || ((c >= 'a') && (c <= 'z'));
    }

//    public void reset(String content) {
//        this.offset = 0;
//        this.root = 0;
//        this.i = this.root;
//        this.isBack = false;
//        this.tempOffset = 0;
//        this.text = content;
//        this.branch = this.tree;
//    }

    /**
     * 当参数对象是列表或者数组的时候，返回指定下标的内容。否则返回null
     *
     * @param i
     * @return String
     */
    @Override
    public String getParam(int i) {
        if (param != null) {
            if (param instanceof String[]) {
                String[] _p = (String[]) param;
                if (_p.length > i) {
                    return _p[i];
                }
            } else if (param instanceof List) {
                List list = (List) param;
                return list.get(i).toString();
            }
        }
        return null;
    }

    /**
     * 得到全部参数
     *
     * @return String
     */
    @Override
    public T getParams() {
        return this.param;
    }

    @Override
    public int getOffset() {
        return offset;
    }
}

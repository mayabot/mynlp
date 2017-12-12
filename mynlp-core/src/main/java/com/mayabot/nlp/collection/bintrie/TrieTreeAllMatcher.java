/*
 *  Copyright 2017 mayabot.com authors. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
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
public class TrieTreeAllMatcher<T> implements TrieTreeMatcher<T> {

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

    private T param;

    private final int len;

    TrieTreeAllMatcher(BinTrieTree<T> tree, String content) {
        this.text = content;
        this.tree = tree;
        this.branch = tree;

        this.len = content.length();
    }

    /**
     * 詞典中全部命中的詞語
     *
     * @return
     */
    public String next() {
        String temp = this.allWordNext();
        while (EMPTY_STRING.equals(temp)) {
            temp = this.allWordNext();
        }
        return temp;
    }


    private String allWordNext() {
        if ((!this.isBack) || (this.i == len - 1)) {
            this.i = (this.root - 1);
        }
        for (this.i += 1; this.i < len; this.i = (this.i + 1)) {
            this.branch = this.branch.findChild(this.text.charAt(this.i));
            if (this.branch == null) {
                this.root += 1;
                this.branch = this.tree;
                this.i = (this.root - 1);
                this.isBack = false;
            } else {
                switch (this.branch.getStatus()) {
                    case AbstractTrieNode.Status_Continue:
                        this.isBack = true;
                        this.offset = (this.tempOffset + this.root);
                        this.param = this.branch.getValue();
                        //return new String(this.chars, this.root, this.i - this.root + 1);
                        return this.text.substring(this.root, this.i + 1);
                    case AbstractTrieNode.Status_End:
                        this.offset = (this.tempOffset + this.root);
                        //this.str = new String(this.chars, this.root, this.i - this.root + 1);
                        this.str = this.text.substring(this.root, this.i + 1);
                        this.param = this.branch.getValue();
                        this.branch = this.tree;
                        this.isBack = false;
                        this.root += 1;
                        return this.str;
                }
            }
        }
        this.tempOffset += this.text.length();
        return null;
    }

    private boolean isE(char c) {
        if (c == '.' || ((c >= 'a') && (c <= 'z'))) {
            return true;
        }
        return false;
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
     * @return
     */
    public String getParam(int i) {
        if (param != null) {
            if (param instanceof String[]) {
                String[] _p = (String[]) param;
                if (_p.length > i) {
                    return _p[i];
                }
            } else if (param instanceof List) {
                List<String> list = (List<String>) param;
                return list.get(i);
            }
        }
        return null;
    }

    /**
     * 得到全部参数
     *
     * @return
     */
    public T getParams() {
        return this.param;
    }

    public int getOffset() {
        return offset;
    }
}

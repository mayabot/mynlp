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
package com.mayabot.nlp.segment.tokenizer.crf;

/**
 * 给一个实例生成一个元素表
 *
 * @author hankcs
 */
public class Table {
    /**
     * 真实值，请不要直接读取
     */
    public String[][] v;
    static final String HEAD = "_B";

    @Override
    public String toString() {
        if (v == null) {
            return "null";
        }
        final StringBuilder sb = new StringBuilder(v.length * v[0].length * 2);
        for (String[] line : v) {
            for (String element : line) {
                sb.append(element).append('\t');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * 获取表中某一个元素
     *
     * @param x
     * @param y
     * @return
     */
    public String get(int x, int y) {
        if (x < 0) {
            return HEAD + x;
        }
        if (x >= v.length) {
            return HEAD + "+" + (x - v.length + 1);
        }

        return v[x][y];
    }

    public void setLast(int x, String t) {
        v[x][v[x].length - 1] = t;
    }

    public int size() {
        return v.length;
    }
}

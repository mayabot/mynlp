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
package com.mayabot.nlp.segment;

import com.mayabot.nlp.segment.corpus.tag.Nature;

import java.util.List;

/**
 * 一个单词，用户可以直接访问此单词的全部属性
 *
 * @author hankcs
 */
public class MyTerm {
    /**
     * 词语
     */
    public String word;

    /**
     * 词性
     */
    public Nature nature;

    /**
     * 在文本中的起始位置（需开启分词器的offset选项）
     */
    public int offset;

    /**
     * 索引分词，切分子词
     */
    public List<MyTerm> subword;

    /**
     * 构造一个单词
     *
     * @param word   词语
     * @param nature 词性
     */
    public MyTerm(String word, Nature nature) {
        this.word = word;
        this.nature = nature;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (nature == null) {
            sb.append(word);
        } else {
            sb.append(word).append("/").append(nature);
        }


        if (subword != null) {
            sb.append("[ ");
            sb.append(subword);
            sb.append(" ]");
        }

        return sb.toString();
    }

    public String getWord() {
        return word;
    }

    /**
     * 长度
     *
     * @return
     */
    public int length() {
        return word.length();
    }

}

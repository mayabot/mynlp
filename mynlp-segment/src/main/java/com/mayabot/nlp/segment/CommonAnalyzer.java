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

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.mayabot.nlp.utils.ParagraphReader;
import com.mayabot.nlp.utils.ParagraphReaderSmart;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 分词器通用实现。有状态。无创建成本。非线程安全.
 * 可以输入大流量的Reader对象。并没有一次性加载全部内容。
 * 这个类主要处理文本分段、和字符处理、等外部逻辑。该对象可以重用
 * 把分词具体的逻辑委托给MyTokenizer，它自己处理一些周边的事情，优化性能
 *
 * @author jimichan
 */
public class CommonAnalyzer implements MyAnalyzer, Iterable<MyTerm> {

    private MyTokenizer tokenizer;

    private ParagraphReader paragraphReader;

    /**
     * 段落的偏移位置
     */
    private int baseOffset = 0;

    /**
     * 内部变量
     */
    private int lastTextLength = -1;

    private LinkedList<MyTerm> buffer = null;

    /**
     * 构造函数
     * @param reader 需要分词的数据源
     * @param tokenizer 具体的分词器
     */
    public CommonAnalyzer(Reader reader, MyTokenizer tokenizer) {
        this.reset(reader);
        this.tokenizer = tokenizer;
    }

    /**
     * 构造函数
     * @param text 需要分词的String文本
     * @param tokenizer 具体的分词器
     */
    public CommonAnalyzer(String text, MyTokenizer tokenizer) {
        this.reset(new StringReader(text));
        this.tokenizer = tokenizer;
    }

    /**
     * 构造函数.
     * 需要调用reset方法设置分词信息来源
     * @param tokenizer 具体的分词器
     */
    public CommonAnalyzer(MyTokenizer tokenizer) {
        this.reset(new StringReader(""));
        this.tokenizer = tokenizer;
    }

    /**
     * 返回下一个词项，如果到达最后的结果，那么返回null
     * @return
     */
    public MyTerm next() {

        if (buffer == null || buffer.isEmpty()) {

            String paragraph;
            try {
                paragraph = paragraphReader.next();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (paragraph == null || paragraph.isEmpty()) {
                return null;
            } else {
                if (lastTextLength == -1) {
                    this.baseOffset = 0;
                    this.lastTextLength = 0;
                } else {
                    this.baseOffset = lastTextLength;
                }

                lastTextLength += paragraph.length();

                char[] text = paragraph.toCharArray();

                //TODO 这里插入CharFilter的操作
                this.buffer = tokenizer.token(text);
            }
        }

        MyTerm term = buffer.pop();

        if (baseOffset != 0) { //补充偏移量
            term.offset += baseOffset;
        }
        return term;
    }

    /**
     * 重置新的信息来源.
     *
     * @param reader
     * @return
     */
    public CommonAnalyzer reset(Reader reader) {
        this.paragraphReader = new ParagraphReaderSmart(reader); //智能分段器
        baseOffset = 0;
        lastTextLength = -1;
        return this;
    }

    @Override
    public Iterator<MyTerm> iterator() {
        return new AbstractIterator<MyTerm>() {
            @Override
            protected MyTerm computeNext() {
                MyTerm n = CommonAnalyzer.this.next();
                if (n != null) {
                    return n;
                } else {
                    return endOfData();
                }
            }
        };
    }

    /**
     * 重置String作为分词源.
     * @param text
     * @return
     */
    public CommonAnalyzer reset(String text) {
        return this.reset(new StringReader(text));
    }

    /**
     * 一个便捷的方法，获得分词结果
     *
     * @return
     */
    public List<String> toWords() {
        return Lists.newArrayList(Iterators.transform(iterator(), MyTerm::getWord));
    }

}

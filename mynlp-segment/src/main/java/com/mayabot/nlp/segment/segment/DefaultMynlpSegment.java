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

package com.mayabot.nlp.segment.segment;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mayabot.nlp.segment.CharNormalize;
import com.mayabot.nlp.segment.MynlpSegment;
import com.mayabot.nlp.segment.MynlpTerm;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.utils.ParagraphReader;
import com.mayabot.nlp.utils.ParagraphReaderSmart;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 分词器通用实现。有状态。无创建成本。非线程安全.
 * 可以输入大流量的Reader对象。并没有一次性加载全部内容。
 * 这个类主要处理文本分段、和字符处理、等外部逻辑。该对象可以重用
 * 把分词具体的逻辑委托给MyTokenizer，它自己处理一些周边的事情，优化性能
 *
 * @author jimichan
 */
public class DefaultMynlpSegment implements MynlpSegment {

    private MynlpTokenizer tokenizer;

    private ParagraphReader paragraphReader;

    /**
     * 段落的偏移位置
     */
    private int baseOffset = 0;

    /**
     * 内部变量
     */
    private int lastTextLength = -1;

    private LinkedList<MynlpTerm> buffer = null;

    private List<CharNormalize> charNormalize;
    private Reader reader;

    /**
     * 构造函数
     *
     * @param reader    需要分词的数据源
     * @param tokenizer 具体的分词器
     */
    public DefaultMynlpSegment(Reader reader, MynlpTokenizer tokenizer) {
        this.reset(reader);
        this.tokenizer = tokenizer;
    }


    /**
     * 构造函数
     *
     * @param text      需要分词的String文本
     * @param tokenizer 具体的分词器
     */
    public DefaultMynlpSegment(String text, MynlpTokenizer tokenizer) {
        this.reset(new StringReader(text));
        this.tokenizer = tokenizer;
    }

    /**
     * 指定消费者来访问所有的词。底层可以使用多核线程来进行优化速度
     *
     * @param action
     */
    @Override
    public void forEach(Consumer<? super MynlpTerm> action) {
        // 之后改造成支持使用Stream函数，改造成支持多线程并发的版本
        Objects.requireNonNull(action);
        for (MynlpTerm t : this) {
            action.accept(t);
        }
    }

    /**
     * 构造函数.
     * 需要调用reset方法设置分词信息来源
     *
     * @param tokenizer 具体的分词器
     */
    public DefaultMynlpSegment(MynlpTokenizer tokenizer) {
        this.reset(new StringReader(""));
        this.tokenizer = tokenizer;
    }

    /**
     * 返回下一个词项，如果到达最后的结果，那么返回null
     *
     * @return
     */
    @Override
    public MynlpTerm next() {

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

                if (charNormalize != null) {
                    for (CharNormalize normalize : charNormalize) {
                        normalize.normal(text);
                    }
                }

                this.buffer = tokenizer.token(text);
            }
        }

        MynlpTerm term = buffer.pop();

        if (baseOffset != 0) { //补充偏移量
            term.setOffset(term.getOffset() + baseOffset);
        }
        return term;
    }

    /**
     * 重置新的信息来源.
     *
     * @param reader
     * @return
     */
    @Override
    public DefaultMynlpSegment reset(Reader reader) {
        close();
        this.reader = reader;

        this.paragraphReader = new ParagraphReaderSmart(reader); //智能分段器
        baseOffset = 0;
        lastTextLength = -1;
        return this;
    }

    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException("", e);
            }
        }
    }

    public List<CharNormalize> getCharNormalize() {
        return charNormalize;
    }

    public void setCharNormalize(List<CharNormalize> charNormalize) {
        this.charNormalize = charNormalize;
    }


    public static void main(String[] args) {

        List<Integer> xlist = Lists.newArrayList();
        for (int i = 0; i < 100000; i++) {
            xlist.add(i);
        }

        Spliterator<Integer> spliterator = Iterables.concat(xlist).spliterator();


        Set<String> xset = Sets.newHashSet();

        Stream<Double> stream = StreamSupport.stream(spliterator, true).map(
                num -> {
                    double r = num / 2.0;
                    xset.add(Thread.currentThread().getName());
                    return r;
                });

        Iterator<Double> iterator = stream.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }

        System.out.println(xset);
    }
}

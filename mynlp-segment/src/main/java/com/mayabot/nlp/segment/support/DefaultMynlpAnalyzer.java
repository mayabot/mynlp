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

package com.mayabot.nlp.segment.support;

import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.CharNormalize;
import com.mayabot.nlp.segment.MynlpAnalyzer;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.utils.ParagraphReader;
import com.mayabot.nlp.utils.ParagraphReaderSmart;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 分词器通用实现。有状态。无创建成本。非线程安全.
 * 可以输入大流量的Reader对象。并没有一次性加载全部内容。
 * 这个类主要处理文本分段、和字符处理、等外部逻辑。该对象可以重用
 * 把分词具体的逻辑委托给MyTokenizer，它自己处理一些周边的事情，优化性能
 *
 * @author jimichan
 */
public class DefaultMynlpAnalyzer implements MynlpAnalyzer {

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

    private MynlpTermBuffer buffer = new MynlpTermBuffer();

    private List<CharNormalize> charNormalize;
    private Reader reader;

    /**
     * 构造函数
     *
     * @param reader    需要分词的数据源
     * @param tokenizer 具体的分词器
     */
    public DefaultMynlpAnalyzer(Reader reader, MynlpTokenizer tokenizer) {
        this.reset(reader);
        this.tokenizer = tokenizer;
    }


    /**
     * 构造函数
     *
     * @param text      需要分词的String文本
     * @param tokenizer 具体的分词器
     */
    public DefaultMynlpAnalyzer(String text, MynlpTokenizer tokenizer) {
        this.reset(new StringReader(text));
        this.tokenizer = tokenizer;
    }

    /**
     * 指定消费者来访问所有的词。底层可以使用多核线程来进行优化速度
     *
     * @param action
     */
    @Override
    public void forEach(Consumer<? super WordTerm> action) {
        // 之后改造成支持使用Stream函数，改造成支持多线程并发的版本
        Objects.requireNonNull(action);
        for (WordTerm t : this) {
            action.accept(t);
        }
    }

    /**
     * 构造函数.
     * 需要调用reset方法设置分词信息来源
     *
     * @param tokenizer 具体的分词器
     */
    public DefaultMynlpAnalyzer(MynlpTokenizer tokenizer) {
        this.reset(new StringReader(""));
        this.tokenizer = tokenizer;
    }

    /**
     * 返回下一个词项，如果到达最后的结果，那么返回null
     *
     * @return
     */
    @Override
    public WordTerm next() {

        if (!buffer.hasRemain()) {

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

                if (buffer.list.size() > 5000) { //如果buffer太大了就
                    buffer.list = Lists.newArrayListWithExpectedSize(256);
                }
                tokenizer.token(text, this.buffer.list);

                buffer.reset();
            }
        }

        WordTerm term = buffer.pop();

        if (term == null) {
            return null;
        }

        if (baseOffset != 0) { //补充偏移量
            term.setOffset(term.getOffset() + baseOffset);
        }
        return term;
    }

    static class MynlpTermBuffer {
        ArrayList<WordTerm> list = Lists.newArrayListWithExpectedSize(256);
        int po = 0;
        int cap = 0;

        public boolean hasRemain() {
            return po < cap;
        }

        public WordTerm pop() {
            if (po >= cap) {
                return null;
            }
            return list.get(po++);
        }

        public void reset() {
            cap = list.size();
            po = 0;
        }
    }

    /**
     * 重置新的信息来源.
     *
     * @param reader
     * @return
     */
    @Override
    public DefaultMynlpAnalyzer reset(Reader reader) {
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

}

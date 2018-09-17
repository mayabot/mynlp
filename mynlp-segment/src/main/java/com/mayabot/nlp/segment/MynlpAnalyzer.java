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
package com.mayabot.nlp.segment;


import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.Reader;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * MynlpAnalyzer主要面向Reader进行分词，里面包含了WordTerm序列的处理逻辑，可以进行停用词、标点符号的过滤。
 * 是无状态的，可多线程调用。
 * 具体参考StandardMynlpAnalyzer。用户自定义实现该接口需从BaseMynlpAnalyzer继承。
 *
 * @see com.mayabot.nlp.segment.analyzer.BaseMynlpAnalyzer
 * @see com.mayabot.nlp.segment.analyzer.StandardMynlpAnalyzer
 * @author jimichan jimichan@gmail.com
 */
public interface MynlpAnalyzer {

    /**
     * 对文本进行分词，返回一个延迟计算的Iterable&lt;Term&gt;。
     *
     * @param reader 文本源
     * @return 可迭代的WordTerm序列
     */
    Iterable<WordTerm> parse(Reader reader);

    /**
     * 对文本进行分词，返回一个延迟计算的Iterable&lt;Term&gt;。
     *
     * @param text
     * @return
     */
    Iterable<WordTerm> parse(String text);


    default List<String> parseToStringList(String text) {
        return Lists.newArrayList(Iterables.transform(parse(text), x -> x.word));
    }


    /**
     * 对文本进行分词，返回一个延迟计算的StreamWord&lt;Term&gt;。
     *
     * @param reader 文本源
     * @return 可迭代的WordTerm序列
     */
    Stream<WordTerm> stream(Reader reader);

    /**
     * 对文本进行分词，返回一个延迟计算的StreamWord&lt;Term&gt;。
     *
     * @param reader
     * @return
     */
    Stream<WordTerm> stream(String reader);

}

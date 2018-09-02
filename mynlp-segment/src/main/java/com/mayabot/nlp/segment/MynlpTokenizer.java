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

import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.analyzer.TokenWordTermGenerator;
import com.mayabot.nlp.segment.analyzer.WordTermGeneratorIterable;

import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MynlpTokenizer切词器接口。
 * MynlpTokenizer只需要关心把一句话或一小段有限的文本分词完成,切词器的实现类是个无状态，所以可以多线程安全，同一个配置只需要一个实例共享使用。
 * 所以一个固定的算法的切分,只需要一个实例。
 *
 * @author jimichan
 */
public interface MynlpTokenizer {

    /**
     * 对text进行分词，结构保存在List<WordTerm>,
     * token不会对target清空，只会add。
     * @param text
     * @param target 结果保存在list里面，这个target如果重复使用的，那么会自动clear
     * @return
     */
    void token(char[] text, List<WordTerm> target);

    /**
     * 对text是String字符串的一个便捷包装.
     *
     * @param text
     * @param target
     */
    default void token(String text, List<WordTerm> target) {
        token(text.toCharArray(), target);
    }


    /**
     * 便捷方法。不适用于超大文本
     * @param text
     * @return
     */
    default List<String> tokenToList(String text) {
        if (text == null || text.isEmpty()) {
            return Lists.newArrayListWithCapacity(1);
        }
        List<WordTerm> target = Lists.newArrayListWithExpectedSize(text.length() / 2);
        token(text, target);
        return target.stream().map(x -> x.word).collect(Collectors.toList());
    }

    /**
     * 便捷方法。不适用于超大文本
     *
     * @param text
     * @return
     */
    default List<WordTerm> tokenToTermList(String text) {
        if (text == null || text.isEmpty()) {
            return Lists.newArrayListWithCapacity(1);
        }
        List<WordTerm> target = Lists.newArrayListWithExpectedSize(text.length() / 2);
        token(text, target);
        return target;
    }

    default Iterable<WordTerm> parse(Reader reader) {
        return new WordTermGeneratorIterable(new TokenWordTermGenerator(reader, this));
    }

    default Stream<WordTerm> stream(Reader reader) {
        return new WordTermGeneratorIterable(new TokenWordTermGenerator(reader, this)).stream();
    }

}

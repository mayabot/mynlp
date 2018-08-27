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
import com.mayabot.nlp.segment.support.DefaultMynlpAnalyzer;

import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MynlpTokenizer是一个一个切词器接口。
 * 和MynlpAnalyzer的差别是MynlpTokenizer只需要关心把一句话或一小段有限的文本分词完成。
 * MynlpTokenizer切词器的实现类是个无状态，所以可以多线程安全，系统只需要一个实例共享使用。
 * 所以一个固定的算法的切分,只需要一个实例
 * @author jimichan
 */
public interface MynlpTokenizer {

    /**
     * 对text进行分词，结构保存在List<MynlpTerm>,
     * token不会对target清空，只会add。
     * @param text
     * @param target 结果保存在list里面，这个target如果重复使用的，那么会自动clear
     * @return
     */
    void token(char[] text, List<MynlpTerm> target);

    /**
     * 对text是String字符串的一个便捷包装.
     *
     * @param text
     * @param target
     */
    default void token(String text, List<MynlpTerm> target) {
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
        List<MynlpTerm> target = Lists.newArrayListWithExpectedSize(text.length() / 2);
        token(text, target);
        return target.stream().map(x -> x.word).collect(Collectors.toList());
    }

    /**
     * 便捷方法。不适用于超大文本
     *
     * @param text
     * @return
     */
    default List<MynlpTerm> tokenToTermList(String text) {
        if (text == null || text.isEmpty()) {
            return Lists.newArrayListWithCapacity(1);
        }
        List<MynlpTerm> target = Lists.newArrayListWithExpectedSize(text.length() / 2);
        token(text, target);
        return target;
    }

    default MynlpAnalyzer createAnalyzer() {
        return new DefaultMynlpAnalyzer("", this);
    }

    default MynlpAnalyzer createAnalyzer(Reader reader) {
        return new DefaultMynlpAnalyzer(reader, this);
    }
}

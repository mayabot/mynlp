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

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

/**
 * 一个面向Reader词分析器实现。它是有状态的，不能被多线程访问.
 * 通过调用reset方法，可以重复使用对象
 *
 * @author jimichan
 */
public interface MynlpAnalyzer extends Iterable<WordTerm> {

    /**
     * 返回下一个词
     *
     * @return 返回null表示已经完全返回完毕
     */
    WordTerm next();


    /**
     * 重置切词源
     * @param reader
     * @return
     */
    MynlpAnalyzer reset(Reader reader);

    /**
     * 默认实现，String版本的源
     * @param text
     * @return
     */
    default MynlpAnalyzer reset(String text) {
        return this.reset(new StringReader(text));
    }

    /**
     * 默认实现Iterable接口，便于使用Java的For来访问WordTerm元素。
     * @return
     */
    @Override
    default Iterator<WordTerm> iterator() {
        return new AbstractIterator<WordTerm>() {
            @Override
            protected WordTerm computeNext() {
                WordTerm n = MynlpAnalyzer.this.next();
                if (n != null) {
                    return n;
                } else {
                    return endOfData();
                }
            }
        };
    }

    /**
     * 一个便捷的方法，获得分词结果
     *
     * @return
     */
    default List<String> toList() {
        return Lists.newArrayList(Iterators.transform(iterator(), WordTerm::getWord));
    }

}

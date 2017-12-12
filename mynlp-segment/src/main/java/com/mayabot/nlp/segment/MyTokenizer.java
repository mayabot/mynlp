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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 一个切词器接口。和Analyzer的差别是Tokenizer只需要关心把一句话或一小段有限的文本分词完成。
 * 改切词器，应该是个无状态，而且可以多线程安全.
 * 所以一个固定的算法的切分。只需要一个实例
 */
public interface MyTokenizer {

    /**
     * 返回LinkedList是方便上层进行一个个戳去
     *
     * @param text
     * @return
     */
    LinkedList<MyTerm> token(char[] text);

    /**
     * 便捷方法。不适用于超大文本
     *
     * @param text
     * @return
     */
    default List<String> toStringList(String text) {
        return token(text.toCharArray()).stream().map(x -> x.toString()).collect(Collectors.toList());
    }

}

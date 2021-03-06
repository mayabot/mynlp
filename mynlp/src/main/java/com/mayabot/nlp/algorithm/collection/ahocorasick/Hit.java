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

/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * 源代码来自于 https://github.com/hankcs/HanLP
 */
package com.mayabot.nlp.algorithm.collection.ahocorasick;

/**
 * 一个命中结果
 *
 * @param <V>
 */
public class Hit<V> {
    /**
     * 模式串在母文本中的起始位置
     */
    public final int begin;
    /**
     * 模式串在母文本中的终止位置
     */
    public final int end;
    /**
     * 模式串对应的值
     */
    public final V value;

    public Hit(int begin, int end, V value) {
        this.begin = begin;
        this.end = end;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("[%d:%d]=%s", begin, end, value);
    }
}

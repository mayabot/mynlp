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

package com.mayabot.nlp.collection.ahocorasick;

/**
 * 命中一个模式串的处理方法
 */
public interface IHit<V>
{
    /**
     * 命中一个模式串
     *
     * @param begin 模式串在母文本中的起始位置
     * @param end   模式串在母文本中的终止位置
     * @param value 模式串对应的值
     */
    void hit(int begin, int end, V value);
}


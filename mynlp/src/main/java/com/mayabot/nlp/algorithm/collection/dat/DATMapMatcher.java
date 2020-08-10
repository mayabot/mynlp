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
 * 源代码参考和部分引用来自 https://github.com/hankcs/HanLP https://github.com/NLPchina/ansj_seg
 */
package com.mayabot.nlp.algorithm.collection.dat;

/**
 * 一个搜索工具（注意，当调用next()返回false后不应该继续调用next()，除非reset状态）
 * <p>
 * DAT的匹配器是一个多匹配器，把各种可能都计算出来
 *
 * @author jimichan
 */
public interface DATMapMatcher<V> {

    boolean next();

    int getBegin();

    int getLength();

    V getValue();

    int getIndex();
}
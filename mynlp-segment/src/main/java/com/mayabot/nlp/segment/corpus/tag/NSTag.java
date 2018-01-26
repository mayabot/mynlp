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
package com.mayabot.nlp.segment.corpus.tag;

/**
 * 地名角色标签
 *
 * @author hankcs
 */
public enum NSTag {
    /**
     * 地名的上文 我【来到】中关园
     */
    A,
    /**
     * 地名的下文刘家村/和/下岸村/相邻
     */
    B,
    /**
     * 中国地名的第一个字
     */
    C,
    /**
     * 中国地名的第二个字
     */
    D,
    /**
     * 中国地名的第三个字
     */
    E,
    /**
     * 其他整个的地名
     */
    G,
    /**
     * 中国地名的后缀海/淀区
     */
    H,
    /**
     * 连接词刘家村/和/下岸村/相邻
     */
    X,
    /**
     * 其它非地名成分
     */
    Z,

    /**
     * 句子的开头
     */
    S,
}

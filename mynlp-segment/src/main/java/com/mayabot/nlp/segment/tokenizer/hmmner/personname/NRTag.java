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
package com.mayabot.nlp.segment.tokenizer.hmmner.personname;

/**
 * 人名标签
 * 参考HanLP https://github.com/hankcs/HanLP 中的实现
 *
 * @author hankcs
 */
public enum NRTag {
    /**
     * Pf	姓氏	【张】华平先生
     */
    B,

    /**
     * Pm	双名的首字	张【华】平先生
     */
    C,

    /**
     * Pt	双名的末字	张华【平】先生
     */
    D,

    /**
     * Ps	单名	张【浩】说：“我是一个好人”
     */
    E,

    /**
     * Ppf	前缀	【老】刘、【小】李
     */
    F,

    /**
     * Plf	后缀	王【总】、刘【老】、肖【氏】、吴【妈】、叶【帅】
     */
    G,

    /**
     * Pp	人名的上文	又【来到】于洪洋的家。
     */
    K,

    /**
     * Pn	人名的下文	新华社记者黄文【摄】
     */
    L,

    /**
     * Ppn	两个中国人名之间的成分	编剧邵钧林【和】稽道青说
     */
    M,

    /**
     * Ppf	人名的上文和姓成词	这里【有关】天培的壮烈
     */
    U,

    /**
     * Pnw	三字人名的末字和下文成词	龚学平等领导, 邓颖【超生】前
     */
    V,

    /**
     * Pfm	姓与双名的首字成词	【王国】维、
     */
    X,

    /**
     * Pfs	姓与单名成词	【高峰】、【汪洋】
     */
    Y,

    /**
     * Pmt	双名本身成词	张【朝阳】
     */
    Z,

    /**
     * Po	以上之外其他的角色
     */
    A,

    /**
     * 句子的开头
     */
    S,
}

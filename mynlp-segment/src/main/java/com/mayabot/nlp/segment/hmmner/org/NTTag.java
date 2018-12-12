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
package com.mayabot.nlp.segment.hmmner.org;

/**
 * 参考HanLP https://github.com/hankcs/HanLP 中的实现
 *
 * @author hankcs
 */
public enum NTTag {
    /**
     * 上文	[参与]亚太经合组织的活动
     */
    A,
    /**
     * 下文	中央电视台[报道]
     */
    B,
    /**
     * 连接词	北京电视台[和]天津电视台
     */
    X,
    /**
     * 特征词的一般性前缀	 北京[电影]学院
     */
    C,
    /**
     * 特征词的译名性前缀 	美国[摩托罗拉]公司
     */
    F,
    /**
     * 特征词的地名性前缀 	交通银行[北京]分行
     */
    G,
    /**
     * 特征词的机构名前缀	  [中共中央]顾问委员会
     */
    H,
    /**
     * 特征词的特殊性前缀	 [华谊]医院
     */
    I,
    /**
     * 特征词的简称性前缀 	[巴]政府
     */
    J,
    /**
     * 整个机构 [麦当劳]
     */
    K,
    /**
     * 方位词
     */
    L,
    /**
     * 数词 公交集团[五]分公司
     */
    M,
    /**
     * 单字碎片
     */
    P,
    /**
     * 符号
     */
    W,
    /**
     * 机构名的特征词	国务院侨务[办公室]
     */
    D,
    /**
     * 非机构名成份
     */
    Z,

    /**
     * 句子的开头
     */
    S
}

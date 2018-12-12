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

import java.util.HashMap;
import java.util.Map;

/**
 * 词性.
 * 采用北大词性标注集。
 * 加了一些扩展
 *
 * @author jimichan
 */
public enum Nature {


    /**
     * 形容词
     */
    a,
    /**
     * 形语素
     */
    Ag,

    /**
     * 副形词
     */
    ad,


    /**
     * 名形词
     */
    an,

    /**
     * 区别词
     */
    b,


    /**
     * 区别语素
     */
    Bg,


    /**
     * 连词
     */

    c,


    /**
     * 副词
     */
    d,


    /**
     * 副语素
     */
    Dg,


    /**
     * 叹词
     */
    e,


    /**
     * 方位词
     */
    f,


    /**
     * 前接成分
     */
    h,


    /**
     * 成语
     */
    i,


    /**
     * 简称略语
     */
    j,


    /**
     * 后接成分
     */
    k,


    /**
     * 习用语
     */
    l,


    /**
     * 数词
     */
    m,


    /**
     * 数词语素 甲/Mg
     */
    Mg,


    /**
     * 名词
     */
    n,


    /**
     * 人名
     */
    nr,


    /**
     * 地名
     */
    ns,


    /**
     * 机构团体
     */
    nt,


    /**
     * 外文字符串 vcd/nx
     */
    nx,


    /**
     * 其他专名
     */
    nz,


    /**
     * 名语素
     */
    Ng,


    /**
     * 拟声词
     */
    o,

    /**
     * 介词
     */
    p,


    /**
     * 量词
     */
    q,


    /**
     * 代词
     */
    r,


    /**
     * 代词语素
     * 诸/Rg 学者/n
     */
    Rg,


    /**
     * 处所词
     */
    s,


    /**
     * 时间词
     * 去年/t
     */
    t,


    /**
     * 时间语素
     */
    Tg,


    /**
     * 助词
     */
    u,


    /**
     * 动词
     */
    v,
    /**
     * 副动词
     */
    vd,


    /**
     * 动名词
     */
    vn,


    /**
     * 动语素
     */
    Vg,


    /**
     * 标点符号
     */
    w,


    /**
     * 语气词
     */
    y,


    /**
     * 语气语素
     * 唯/d 大力/d 者/k 能/v 致/v 之/u 耳/Yg
     */
    Yg,


    /**
     * 状态词
     */
    z,

    /**-------------以上为标准的北大词性标注集合------------**/

    /**
     * 非语素字。 ict
     */
    xx,

    /**
     * 数量词
     */
    mq,

    /**
     * 字符串。同nx
     */
    x,

    /**
     * 句子开头
     */
    begin,

    /**
     * 句子结尾
     */
    end,

    /**
     * 新词
     */
    newWord;


    /**
     * 是否名词
     *
     * @return
     */
    public boolean isN() {
        return (this.name().charAt(0) == 'n' || this == Ng) && this != nx;
    }


    static Map<String, Nature> map;

    static {
        //三倍空间，降低hash冲突
        map = new HashMap<>(Nature.values().length * 5);
        for (Nature n : Nature.values()) {
            map.put(n.name(), n);
        }
    }

    public static Nature parse(String pos) {
        //不存在就返回字符串x
        if (pos == null || pos.isEmpty()) {
            return Nature.x;
        }
        Nature n = map.get(pos);
        if (n != null) {
            return n;
        } else {
            switch (pos.charAt(0)) {
                case 'n':
                    return Nature.n;
                case 'a':
                    return Nature.a;
                case 'v':
                    return Nature.v;
                case 'd':
                    return Nature.d;
                default:
                    return Nature.x;
            }
        }
    }
}
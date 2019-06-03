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
    a("形容词"),
    /**
     * 形语素
     */
    Ag("形语素"),

    /**
     * 副形词
     */
    ad("副形词"),


    /**
     * 名形词
     */
    an("名形词"),

    /**
     * 区别词
     */
    b("区别词"),


    /**
     * 区别语素
     */
    Bg("区别语素"),


    /**
     * 连词
     */

    c("连词"),


    /**
     * 副词
     */
    d("副词"),


    /**
     * 副语素
     */
    Dg("副语素"),


    /**
     * 叹词
     */
    e("叹词"),


    /**
     * 方位词
     */
    f("方位词"),


    /**
     * 前接成分
     */
    h("前接成分"),


    /**
     * 成语
     */
    i("成语"),


    /**
     * 简称略语
     */
    j("简称略语"),


    /**
     * 后接成分
     */
    k("后接成分"),


    /**
     * 习用语
     */
    l("习用语"),


    /**
     * 数词
     */
    m("数词"),


    /**
     * 数词语素 甲/Mg
     */
    Mg("数词语素 "),


    /**
     * 名词
     */
    n("名词"),


    /**
     * 人名
     */
    nr("人名"),


    /**
     * 地名
     */
    ns("地名"),


    /**
     * 机构团体
     */
    nt("机构团体"),


    /**
     * 外文字符串 vcd/nx
     */
    nx("字符串"),


    /**
     * 其他专名
     */
    nz("其他专名"),


    /**
     * 名语素
     */
    Ng("名语素"),


    /**
     * 拟声词
     */
    o("拟声词"),

    /**
     * 介词
     */
    p("介词"),


    /**
     * 量词
     */
    q("量词"),


    /**
     * 代词
     */
    r("代词"),


    /**
     * 代词语素
     * 诸/Rg 学者/n
     */
    Rg("代词语素"),


    /**
     * 处所词
     */
    s("处所词"),


    /**
     * 时间词
     * 去年/t
     */
    t("时间词"),


    /**
     * 时间语素
     */
    Tg("时间语素"),


    /**
     * 助词
     */
    u("助词"),


    /**
     * 动词
     */
    v("动词"),
    /**
     * 副动词
     */
    vd("副动词"),


    /**
     * 动名词
     */
    vn("动名词"),


    /**
     * 动语素
     */
    Vg("动语素"),


    /**
     * 标点符号
     */
    w("标点"),


    /**
     * 语气词
     */
    y("语气词"),


    /**
     * 语气语素
     * 唯/d 大力/d 者/k 能/v 致/v 之/u 耳/Yg
     */
    Yg("语气语素"),


    /**
     * 状态词
     */
    z("状态词"),

    /**-------------以上为标准的北大词性标注集合------------**/

    /**
     * 非语素字。 ict
     */
    xx("非语素字"),

    /**
     * 数量词
     */
    mq("数量词"),

    /**
     * 字符串。同nx
     */
    x("字符串"),

    /**
     * 句子开头
     */
    begin("句子开头"),

    /**
     * 句子结尾
     */
    end(" 句子结尾"),

    /**
     * 新词
     */
    newWord("新词");

    private String show;

    Nature(String show) {
        this.show = show;
    }

    /**
     * 是否名词
     *
     * @return 是否名词
     */
    public boolean isN() {
        return (this.name().charAt(0) == 'n' || this == Ng) && this != nx;
    }


    static Map<String, Nature> map;

    static {
        //五倍空间，降低hash冲突
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

    public String show() {
        return show;
    }
}
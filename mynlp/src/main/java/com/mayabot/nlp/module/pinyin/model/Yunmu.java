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
 * <create-date>2014/11/2 8:09</create-date>
 *
 * <copyright file="Yunmu.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.mayabot.nlp.module.pinyin.model;

/**
 * 使用v 代替 ü
 * https://github.com/System-T/DimSim/blob/master/dimsim/utils/maps.py
 * <p>
 * vowelMap_TwoDCode = {
 * <p>
 * <p>
 * "u:an":(2.0,1.0),
 * <p>
 * "io":(20,2.5),
 * "iou":(20,4),
 * "uo":(20,6.0),
 * <p>
 * "u:e":(40,5.0),
 * "ve":(40,5.0),
 * "uei":(40,3.0),
 * <p>
 * "uen":(43,0.5),
 * "ueng":(43,1.0),
 * <p>
 * "v":(61,1.0),
 * "u:n":(61,2.5),
 * "vn":(61,2.5),
 * "u":(80,0.0),
 * <p>
 * "":(99999.0,99999.0)
 * }
 *
 * @author hankcs
 */
public enum Yunmu {
    a(1.0f, 0.0f),
    ai(8.0f, 0.0f),
    an(1.0f, 1.0f),
    ang(1.0f, 1.5f),
    ao(5.0f, 0.0f),
    e(41f, 0.0f),
    ei(40f, 4.0f),
    en(42f, 0.5f),
    eng(42f, 1.0f),
    er(41f, 1f),
    i(60f, 1.0f),
    ia(0.0f, 0.0f),
    ian(0.0f, 1.0f),
    iang(0.0f, 1.5f),
    iao(5.0f, 1.5f),
    ie(40f, 4.5f),
    in(60f, 2.5f),
    ing(60f, 3.0f),
    iong(20, 9.5f),
    iu(20f, 4f),
    o(20f, 0.0f),
    ong(20f, 8.0f),
    ou(20f, 5.5f),
    u(80f, 0.0f),
    ua(2.0f, 0.0f),
    uai(8.0f, 1.5f),
    uan(2.0f, 1.0f),
    uang(2.0f, 1.5f),
    ue(40f, 5.0f),
    ui(40, 3.0f),
    un(43, 0.5f),
    uo(20f, 6.0f),
    v(80, 0.0f),
    ve(40, 5.0f),
    /**
     * 仅用于null类型的拼音
     */
    none(9999f, 9999f);

    Yunmu(float twoDCode1, float twoDCode2) {
        this.twoDCode1 = twoDCode1;
        this.twoDCode2 = twoDCode2;
//        this.code = code;
    }

    private float twoDCode1;

    private float twoDCode2;

    private String code;

    public float getTwoDCode1() {
        return twoDCode1;
    }

    public float getTwoDCode2() {
        return twoDCode2;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

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
 * <create-date>2014/11/2 8:08</create-date>
 *
 * <copyright file="Shengmu.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.mayabot.nlp.module.pinyin.model;

/**
 * 声母
 * <p>
 * twoDCode from
 * https://github.com/System-T/DimSim/blob/master/dimsim/utils/maps.py
 *
 * @author hankcs
 */
public enum Shengmu {
    b(1.0f, 0.5f),
    c(31f, 1.5f),
    ch(31f, 1.7f),
    d(12.0f, 0.5f),
    f(7.0f, 4.0f),
    g(7.0f, 0.5f),
    h(7.0f, 3.0f),
    j(30.0f, 0.5f),
    k(7.0f, 1.5f),
    l(22.5f, 1.5f),
    m(50.0f, 3.5f),
    n(22.5f, 0.5f),
    p(1.0f, 1.5f),
    q(31.0f, 0.5f),
    r(22.5f, 2.5f),
    s(33f, 3.5f),
    sh(33f, 3.7f),
    t(12.0f, 1.5f),
    w(40f, 5.0f),
    x(33f, 2.5f),
    y(40.0f, 0.0f),
    z(30f, 1.5f),
    zh(30f, 1.7f),
    /**
     * 零声母
     */
    none(99999f, 99999f);

    Shengmu(float twoDCode1, float twoDCode2) {
        this.twoDCode1 = twoDCode1;
        this.twoDCode2 = twoDCode2;
    }

    private float twoDCode1;

    private float twoDCode2;

//    public String code(){
//        return codeMap.get(this);
//    }

    public float getTwoDCode1() {
        return twoDCode1;
    }

    public float getTwoDCode2() {
        return twoDCode2;
    }
//
//    static final Map<Shengmu,String> codeMap = buildCodeMap();
//
//    static Map<Shengmu,String> buildCodeMap(){
//        // 基于改进音形码的中文敏感词检测算法.pdf (周 昊,沈庆宏)
//        Map<Shengmu,String> codeMap = new HashMap<>();
//        codeMap.put(Shengmu.b,"00000");
//        codeMap.put(Shengmu.p,"00001");
//        codeMap.put(Shengmu.m,"00011");
//        codeMap.put(Shengmu.f,"00010");
//
//        codeMap.put(Shengmu.d,"00111");
//        codeMap.put(Shengmu.t,"00101");
//        codeMap.put(Shengmu.n,"00100");
//        codeMap.put(Shengmu.l,"01100");
//
//        codeMap.put(Shengmu.g,"01111");
//        codeMap.put(Shengmu.k,"01110");
//        codeMap.put(Shengmu.h,"01010");
//
//        codeMap.put(Shengmu.j,"01001");
//        codeMap.put(Shengmu.q,"01000");
//        codeMap.put(Shengmu.x,"11000");
//
//        codeMap.put(Shengmu.zh,"11011");
//        codeMap.put(Shengmu.ch,"11010");
//        codeMap.put(Shengmu.sh,"11110");
//        codeMap.put(Shengmu.r,"11111");
//
//        codeMap.put(Shengmu.z,"11011");
//        codeMap.put(Shengmu.c,"11010");
//        codeMap.put(Shengmu.s,"11110");
//
//        codeMap.put(Shengmu.y,"11110");
//        codeMap.put(Shengmu.w,"11110");
//
//        codeMap.put(Shengmu.r,"11100");
//        codeMap.put(Shengmu.y,"10100");
//        codeMap.put(Shengmu.none,"00000");
//
//        return Collections.unmodifiableMap(codeMap);
//    }
}

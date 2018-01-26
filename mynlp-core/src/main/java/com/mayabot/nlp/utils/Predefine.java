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
package com.mayabot.nlp.utils;

/**
 * 一些预定义的静态全局变量
 */
public class Predefine {

    //// 现在总词频25146057
    public static final int MAX_FREQUENCY = 25146057;
    /**
     * Smoothing 平滑因子
     */
    public static final double dTemp = (double) 1 / MAX_FREQUENCY + 0.00001;
    /**
     * 平滑参数
     */
    public static final double dSmoothingPara = 0.1;

}

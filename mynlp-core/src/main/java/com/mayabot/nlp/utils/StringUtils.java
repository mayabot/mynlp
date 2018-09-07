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

import java.util.regex.Pattern;

/**
 * @author hankcs
 */
public class StringUtils {

    public static boolean isWhiteSpace(String word) {
        switch (word.charAt(0)) {
            case '\u2002':
            case '\u3000':
            case '\r':
            case '\u0085':
            case '\u200A':
            case '\u2005':
            case '\u2000':
            case '\u2029':
            case '\u000B':
            case '\u2008':
            case '\u2003':
            case '\u205F':
            case '\u1680':
            case '\u0009':
            case '\u0020':
            case '\u2006':
            case '\u2001':
            case '\u202F':
            case '\u00A0':
            case '\u000C':
            case '\u2009':
            case '\u2004':
            case '\u2028':
            case '\u2007':
            case '\n':
                return true;
            default:
                return false;

        }
    }

    /**
     * 匹配&或全角状态字符或标点
     */
    public static final String PATTERN = "&|[\uFE30-\uFFA0]|‘’|“”";

    public static String replaceSpecialtyStr(String str, String pattern, String replace) {
        if (isBlankOrNull(pattern)) {
            pattern = "\\s*|\t|\r|\n";//去除字符串中空格、换行、制表
        }
        if (isBlankOrNull(replace)) {
            replace = "";
        }
        return Pattern.compile(pattern).matcher(str).replaceAll(replace);

    }

    public static boolean isBlankOrNull(String str) {
        if (null == str) {
            return true;
        }
        //return str.length()==0?true:false;
        return str.length() == 0;
    }

    /**
     * 清除数字和空格
     */
    public static String cleanBlankOrDigit(String str) {
        if (isBlankOrNull(str)) {
            return "null";
        }
        return Pattern.compile("\\d|\\s").matcher(str).replaceAll("");
    }


    /**
     * Unicode 编码并不只是为某个字符简单定义了一个编码，而且还将其进行了归类。
     * <p/>
     * /pP 其中的小写 p 是 property 的意思，表示 Unicode 属性，用于 Unicode 正表达式的前缀。
     * <p/>
     * 大写 P 表示 Unicode 字符集七个字符属性之一：标点字符。\\pP‘’“”]",如果在 JDK 5 或以下的环境中，全角单引号对、双引号对
     * <p/>
     * 其他六个是
     * L：字母；
     * M：标记符号（一般不会单独出现）；
     * Z：分隔符（比如空格、换行等）；
     * S：符号（比如数学符号、货币符号等）；
     * N：数字（比如阿拉伯数字、罗马数字等）；
     * C：其他字符
     */
    public static void main(String[] args) {
        System.out.println(replaceSpecialtyStr("中国电信2011年第一批IT设备集中采购-存储备份&（），)(UNIX服务器", PATTERN, ""));
    }
}



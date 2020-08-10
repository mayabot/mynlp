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
package com.mayabot.nlp.common.utils;

/**
 * @author jimichan
 */
public class StringUtils {

    /**
     * 是否全是中文
     *
     * @param str
     * @return isAllChinese
     */
    public static boolean isAllChinese(String str) {
        return str.matches("[\\u4E00-\\u9FA5]+");
    }

    public static boolean isWhiteSpace(char word) {
        switch (word) {
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

    public static boolean isWhiteSpace(String word) {
        int len = word.length();
        if (len == 0) {
            return true;
        }else if (len == 1) {
            return isWhiteSpace(word.charAt(0));
        }else if (len == 2){
            return isWhiteSpace(word.charAt(0)) && isWhiteSpace(word.charAt(1));
        }else {
            return isWhiteSpace(word.charAt(0)) &&
                    isWhiteSpace(word.charAt(len/2) )
                            && isWhiteSpace(word.charAt(len-1));
        }

    }

}



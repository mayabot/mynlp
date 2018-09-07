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
 * 字符集识别辅助工具类
 */
public class CharacterHelper {

    public static boolean isSpaceLetter(char input) {
        return input == 8 || input == 9
                || input == 10 || input == 13
                || input == 32 || input == 160;
    }

    public static boolean isEnglishLetter(char input) {
        return (input >= 'a' && input <= 'z')
                || (input >= 'A' && input <= 'Z');
    }

    public static boolean isArabicNumber(char input) {
        return input >= '0' && input <= '9';
    }

    public static boolean isCJKCharacter(char input) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(input);
        //全角数字字符和日韩字符
//韩文字符集
//日文字符集
//平假名
//片假名
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                //全角数字字符和日韩字符
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                //韩文字符集
                || ub == Character.UnicodeBlock.HANGUL_SYLLABLES
                || ub == Character.UnicodeBlock.HANGUL_JAMO
                || ub == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                //日文字符集
                || ub == Character.UnicodeBlock.HIRAGANA //平假名
                || ub == Character.UnicodeBlock.KATAKANA //片假名
                || ub == Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS;
        //其他的CJK标点符号，可以不做处理
        //|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
        //|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
    }


    /**
     * 进行字符规格化（全角转半角，大写转小写处理）
     *
     * @param input
     * @return char
     */
    public static char regularize(char input) {
        if (input == 12288) {
            input = (char) 32;

        } else if (input > 65280 && input < 65375) {
            input = (char) (input - 65248);

        } else if (input >= 'A' && input <= 'Z') {
            input += 32;
        }

        return input;
    }

}

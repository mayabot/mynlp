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

import com.google.common.base.CharMatcher;

/**
 * @author jimichan
 */
public class Characters {

    /**
     * 把全角字符转成
     * System.out.println(((char)('\u0020'+i)+"   "+(char)('\uFF00'+i)));
     * Range: FF00–FF61
     * Range: 0020-u007E
     * 全角空格为12288，半角空格为32
     * 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
     * 全角字符转换为半角字符
     *
     * @return
     */
    private static final int cha = 'Ａ' - 'A';

    public static String fullWidth2halfWidth(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        int found = -1;
        for (int i = 0, len = input.length(); i < len; i++) {
            char ch = input.charAt(i);
            if (ch == 12288 || ('\uFF00' <= ch && ch <= '\uFF5E')) {
                found = i;
                break;
            }
        }

        if (found > -1) {
            char[] charlist = input.toCharArray();

            for (int i = found, len = charlist.length; i < len; i++) {
                char ch = charlist[i];
                if (ch == 12288) {
                    charlist[i] = ' ';
                }
                if ('\uFF00' < ch && ch <= '\uFF5E') {
                    charlist[i] = (char) (ch - cha);
                }
            }

            return new String(charlist);
        } else {
            return input;
        }
    }

    public static void fullWidth2halfWidth(char[] charlist) {
        for (int i = 0, len = charlist.length; i < len; i++) {
            char ch = charlist[i];
            if (ch == 12288) {
                charlist[i] = ' ';
            }
            if ('\uFF00' < ch && ch <= '\uFF5E') {
                charlist[i] = (char) (ch - cha);
            }
        }
    }


    /**
     * 把半角字符转成全角字符
     * System.out.println(((char)('\u0020'+i)+"   "+(char)('\uFF00'+i)));
     * Range: FF00–FF5E
     * Range: 0020-007E
     * 全角空格为12288，半角空格为32
     * 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
     * 为半角字符=>全角字符转换
     *
     * @return String
     */
    public static String halfWidth2fullWidth(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        int found = -1;
        for (int i = 0, len = input.length(); i < len; i++) {
            char ch = input.charAt(i);
            if (ch == 32 || ('\u0020' <= ch && ch <= '\u007E')) {
                found = i;
                break;
            }
        }

        if (found > -1) {
            char[] charlist = input.toCharArray();

            for (int i = found, len = charlist.length; i < len; i++) {
                char ch = charlist[i];
                if (ch == 32) {
                    charlist[i] = (char) 12288;
                }
                if ('\u0022' < ch && ch <= '\u007E') {
                    charlist[i] = (char) (ch + cha);
                }
            }

            return new String(charlist);
        } else {
            return input;
        }
    }

    /**
     * 是不是标点符号。包括ASCII中的标点，对应的全角标点。和中文扩展标点符号的判断
     *
     * @param c
     * @return boolean
     */
    public static boolean isPunctuation(char c) {
        if (c <= 127) {
            if (c <= 47) {
                return true;
            } else if (58 <= c && c <= 64) {
                return true;
            } else if (91 <= c && c <= 96) {
                return true;
            } else if (123 <= c && c <= 127) {
                return true;
            }

            if (c == 160) {
                return true;
            }
        }
        if ('\u00A1' <= c && c <= '\u00BF') {
            return true;
        }


        if ('\uFF00' <= c && c <= '\uFFEF') {
//			if (c <= '\uFF20') {
            if ('\uFE10' <= c && c <= '\uFE1F') {
                return true;
            }
            if ('\uFF00' <= c && c <= '\uFF0F') {
                return true;
            }

            if ('\uFF1A' <= c && c <= '\uFF20') {
                return true;
            }
//			}

            if ('\uFF3B' <= c && c <= '\uFF40') {
                return true;
            }
            if ('\uFF5B' <= c && c <= '\uFFEF') {
                return true;
            }
        }

        if (c <= '\u303F') {
            if ('\u2010' <= c && c <= '\u2027') {
                return true;
            }
            if ('\u2030' <= c && c <= '\u205E') {
                return true;
            }

            if ('\u2E00' <= c && c <= '\u2E30') {
                return true;
            }

            return '\u3000' <= c && c <= '\u303F';
        }

        return false;
    }

    public static final CharMatcher PUNCTUATION = new CharMatcher() {
        @Override
        public boolean matches(char c) {
            return isPunctuation(c);
        }

        @Override
        public String toString() {
            return "CharMatcher.PUNCTUATION";
        }
    };

    public static boolean isASCII(char c) {
        return c <= 127;
    }

    static final String hex = "0123456789ABCDEF";

    public static String showCharacter(char c) {
        char[] tmp = {'\\', 'u', '\0', '\0', '\0', '\0'};
        for (int i = 0; i < 4; i++) {
            tmp[5 - i] = hex.charAt(c & 0xF);
            c >>= 4;
        }
        return String.copyValueOf(tmp);
    }

    public static int digit(char ch) {
        return Character.digit(ch, 10);
    }

}

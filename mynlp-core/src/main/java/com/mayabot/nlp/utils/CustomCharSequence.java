/*
 *  Copyright 2017 mayabot.com authors. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mayabot.nlp.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 不需要复制char数组
 *
 * @author jimichan
 */
public class CustomCharSequence implements CharSequence {

    private char[] text;
    private int offset;
    private int length;

    public CustomCharSequence(char[] text) {
        this(text, 0, text.length);
    }

    public CustomCharSequence(char[] text, int offset, int length) {
        this.text = text;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        if ((index < 0) || (index >= length)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return text[offset + index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new CustomCharSequence(text, start + offset, end - start);
    }

    public String toString() {
        return new String(text, offset, length);
    }

    public static void main(String[] args) {
        CustomCharSequence s = new CustomCharSequence("abcdefabg".toCharArray());

        System.out.println(s);

        Pattern p = Pattern.compile("ab");
        Matcher m = p.matcher(s);
        while (m.find()) {
            System.out.println(m.start() + " : " + m.group());
        }
    }
}
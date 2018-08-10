/*
 * Copyright 2012 Takao Nakaguchi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trie4j.util;

import java.io.UnsupportedEncodingException;

public class StringUtil {
    public static byte[] toUTF8(String str) {
        try {
            return str.getBytes(utf8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String fromUTF8(byte[] bytes) {
        try {
            return new String(bytes, utf8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String fromUTF8(byte[] bytes, int offset, int length) {
        try {
            return new String(bytes, offset, length, utf8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String repeted(String str, int count) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < count; i++) b.append(str);
        return b.toString();
    }

    private static final String utf8 = "UTF8";
}

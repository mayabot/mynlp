/*
 * Copyright 2013 Takao Nakaguchi
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class StreamUtil {
    public static String readAsString(InputStream is, String encoding)
            throws UnsupportedEncodingException, IOException {
        InputStreamReader reader = new InputStreamReader(is, encoding);
        char[] buff = new char[65536];
        StringBuilder ret = new StringBuilder();
        int len;
        while ((len = reader.read(buff)) != -1) {
            ret.append(buff, 0, len);
        }
        return ret.toString();
    }
}

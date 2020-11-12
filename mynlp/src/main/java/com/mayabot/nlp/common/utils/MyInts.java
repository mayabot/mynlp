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
public class MyInts {


    public static byte[] toByteArray(int[] value, int fromIndex, int toIndex) {
        toIndex = Math.min(value.length, toIndex);
        byte[] bytes = new byte[(toIndex - fromIndex) * 4];
        int point = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            int v = value[i];
            bytes[point++] = (byte) (v >> 24);
            bytes[point++] = (byte) (v >> 16);
            bytes[point++] = (byte) (v >> 8);
            bytes[point++] = (byte) v;
        }
        return bytes;
    }


    public static int[] fromByteArrayToArray(byte[] bytes) {
        return fromByteArrayToArray(bytes, new int[bytes.length / 4], bytes.length);
    }

    public static int[] fromByteArrayToArray(byte[] bytes, int[] result, int bytesLen) {
        int intCount = bytesLen / 4;
        for (int i = 0, len = intCount; i < len; i++) {
            int from = i * 4;
            byte b1 = bytes[from++];
            byte b2 = bytes[from++];
            byte b3 = bytes[from++];
            byte b4 = bytes[from++];
            result[i] = b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
        }
        return result;
    }
}

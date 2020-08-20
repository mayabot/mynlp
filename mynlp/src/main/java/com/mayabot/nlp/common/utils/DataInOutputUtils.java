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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DataInOutputUtils {

    public static final int ic1 = 1123992342;
    public static final int ic2 = 832718121;
    public static final int ic3 = 957462342;

    static int[] nullMagic = new int[]{ic1, ic2, ic3};


    /**
     * 计算了分页数量
     *
     * @param total
     * @param itemPerPage
     * @return
     */
    private static int pages(int total, int itemPerPage) {
        return (total + itemPerPage - 1) / itemPerPage;
    }

    final static int perPage = 1024 * 4;

    public static void writeIntArray(int[] array, DataOutput output) throws IOException {
        if (array == null) {
            array = nullMagic;
        }

        //每个int对应4个字节
        int totalByte = array.length * 4;

        output.writeInt(totalByte);


        // 用分页的思想
        int pages = pages(array.length, perPage);

        for (int page = 0; page < pages; page++) {
            int from = page * perPage;
            int to = from + perPage;
            byte[] bytes = MyInts.toByteArray(array, from, to);
            output.write(bytes);
        }
    }


    public static int[] readIntArray(DataInput input) throws IOException {
        int len = input.readInt();

        if (len == 12) {
            byte[] result = new byte[len];
            input.readFully(result);
            int[] ints = MyInts.fromByteArrayToArray(result);
            if (ints.length == 3 && ints[0] == ic1 && ints[1] == ic2 && ints[2] == ic3) {
                return null;
            } else {
                return ints;
            }
        } else {
            int[] array = new int[len / 4];
            final int pageSize = perPage * 4;
            int pages = pages(len, pageSize);
            byte[] buffer = new byte[pageSize];
            int[] intBuffer = new int[perPage];
            int point = 0;

            for (int page = 0; page < pages; page++) {
                int from = page * pageSize;
                int to = Math.min(from + pageSize, len);
                int length = to - from;

                int intCount = length / 4;

                input.readFully(buffer, 0, length);

                MyInts.fromByteArrayToArray(buffer, intBuffer, length);

                System.arraycopy(intBuffer, 0, array, point, intCount);

                point += intCount;
            }
            return array;
        }
    }


//   public static int[] readIntArray(ByteBuffer buffer) {
//        int size = buffer.getInt() / 4;
//        int[] ints = new int[size];
//        buffer.asIntBuffer().get(ints);
//        buffer.position(buffer.position() + size * 4);
//        if (ints.length == 3 && ints[0] == 1123992342 && ints[1] == ic2 && ints[2] == ic3) {
//            return null;
//        }
//        return ints;
//    }


    public static void writeIntArray(int[][] array, DataOutput output) throws IOException {

        int line = array.length;
        output.writeInt(line);

        for (int i = 0; i < line; i++) {
            writeIntArray(array[i], output);
        }
    }

    public static int[][] readDoubleIntArray(DataInput input) throws IOException {
        int line = input.readInt();
        int[][] result = new int[line][];

        for (int i = 0; i < line; i++) {
            result[i] = readIntArray(input);
        }
        return result;
    }

    public static <T> ArrayList<T> readArrayList(DataInput input, Function<DataInput, T> reader) throws IOException {

        int size = input.readInt();
        ArrayList<T> result = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            result.add(reader.apply(input));
        }

        return result;
    }


    public static <T> void writeArrayList(
            ArrayList<T> list, BiConsumer<T, DataOutput> consumer, DataOutput output) throws IOException {

        output.writeInt(list.size());

        for (T val : list) {
            consumer.accept(val, output);
        }
    }

}

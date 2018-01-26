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

import com.mayabot.nlp.collection.utils.MyInts;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface DataInOutputUtils {


    static void writeIntArray(int[][] array, DataOutput output) throws IOException {

        int line = array.length;
        output.writeInt(line);

        for (int i = 0; i < line; i++) {
            writeIntArray(array[i], output);
        }
    }

    static int[][] readDoubleIntArray(DataInput input) throws IOException {
        int line = input.readInt();
        int[][] result = new int[line][];

        for (int i = 0; i < line; i++) {
            result[i] = readIntArray(input);
        }
        return result;
    }


    static int[] readIntArray(DataInput input) throws IOException {
        int len = input.readInt();
        byte[] result = new byte[len];
        input.readFully(result);
        return MyInts.fromByteArrayToArray(result);
    }

    static void writeIntArray(int[] array, DataOutput output) throws IOException {
        byte[] bytes = MyInts.toByteArray(array);
        output.writeInt(bytes.length);
        output.write(bytes);
    }


    static ArrayList<String> readStringArrayList(DataInput input) throws IOException {
        int size = input.readInt();
        ArrayList<String> result = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            result.add(input.readUTF());
        }
        return result;
    }

    static void writeStringArrayList(ArrayList<String> list, DataOutput output) throws IOException {

        output.writeInt(list.size());

        for (int i = 0; i < list.size(); i++) {
            output.writeUTF(list.get(i));
        }
    }


    static <T> ArrayList<T> readArrayList(DataInput input, Function<DataInput, T> reader) throws IOException {

        int size = input.readInt();
        ArrayList<T> result = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            result.add(reader.apply(input));
        }

        return result;
    }

    static <T> void writeArrayList(
            ArrayList<T> list, BiConsumer<T, DataOutput> consumer, DataOutput output) throws IOException {
        output.writeInt(list.size());

        for (T val : list) {
            consumer.accept(val, output);
        }
    }

    //
//    public static void main(String[] args) throws IOException {
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        DataOutputStream dataOutputStream = new DataOutputStream(out);
//
//        int[] abc =new int[]{1,2,3,4,5};
//
//        writeIntArray(abc,dataOutputStream);
//
//        dataOutputStream.flush();
//        out.flush();
//
//        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(out.toByteArray()));
//
//        int[] re = readIntArray(dataInputStream);
//
//        for (int i : re) {
//            System.out.println(i);
//        }
//
//    }

}

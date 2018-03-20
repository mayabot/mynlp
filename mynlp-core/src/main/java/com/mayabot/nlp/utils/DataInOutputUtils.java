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
import java.nio.ByteBuffer;
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

        int[] ints = MyInts.fromByteArrayToArray(result);
        if(ints.length == 3 && ints[0] == 1123992342 && ints[1] == 832718121 && ints[2] == 957462342 ){
            return null;
        }
        return ints;
    }


    static int[] readIntArray(ByteBuffer buffer) {
        int size = buffer.getInt()/4;
        int[] ints = new int[size];
        buffer.asIntBuffer().get(ints);
        buffer.position(buffer.position()+size*4);
        if(ints.length == 3 && ints[0] == 1123992342 && ints[1] == 832718121 && ints[2] == 957462342 ){
            return null;
        }
        return ints;
    }

    int[] nullMagic = new int[]{1123992342,832718121,957462342};

    static void writeIntArray(int[] array, DataOutput output) throws IOException {
        if(array == null){
            array = nullMagic;
        }
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

}

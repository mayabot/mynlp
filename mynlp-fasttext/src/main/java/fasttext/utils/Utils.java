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

package fasttext.utils;

import com.carrotsearch.hppc.IntArrayList;

import java.util.Random;

public class Utils {

   static float [] sqrt = new float[200000];
   static {
       for (int i = 0; i < 200000; i++) {
           sqrt[i] = (float) Math.pow(i,0.5);
       }
   }
    public static final float sqrt(long d) {
        if (d < 200000) {
            return sqrt[(int)d];
        }
        return (float) Math.pow(d,0.5);
    }

    public static final void iota(int[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = i;
        }
    }


    public static final void swap(int[] array, int i, int j) {
        int x = array[i];
        array[i] = array[j];
        array[j] = x;
    }
    public static final void swap(IntArrayList array, int i, int j) {
        int x = array.get(i);
        array.set(i,array.get(j));
        array.set(j, x);
    }



    public final static void shuffle(int[] array,Random random) {
        int size = array.length;
        for (int i = size - 1; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }
    }
    public final static void shuffle(IntArrayList array, Random random) {
        int size = array.size();
        for (int i = size - 1; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }
    }

}

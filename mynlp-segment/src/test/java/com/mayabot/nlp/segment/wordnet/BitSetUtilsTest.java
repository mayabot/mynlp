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

package com.mayabot.nlp.segment.wordnet;

import org.junit.Test;

import java.util.BitSet;

public class BitSetUtilsTest {

    @Test
    public void accessBlank() throws Exception {

        BitSet bitSet = new BitSet();

        bitSet.set(0, 5);
        bitSet.set(6, 10);

        BitSetUtils.accessBlank(10, bitSet, (from, len) -> {
            System.out.println(String.format("from %d len  %d", from, len));
        });

    }

    @Test
    public void accessBlank2() throws Exception {

        BitSet bitSet = new BitSet();

        bitSet.set(0, 5);
//        bitSet.set(6,10);


        BitSetUtils.accessBlank(10, bitSet, (from, len) -> {
            System.out.println(String.format("from %d len  %d", from, len));
        });

    }

    @Test
    public void accessBlank3() throws Exception {

        BitSet bitSet = new BitSet();

        bitSet.set(0, 5);
        bitSet.set(9, 10);


        BitSetUtils.accessBlank(10, bitSet, (from, len) -> {
            System.out.println(String.format("from %d len  %d", from, len));
        });

    }

    @Test
    public void accessBlank4() throws Exception {

        BitSet bitSet = new BitSet();

        bitSet.set(0, 5);
        bitSet.set(6, 7);
        bitSet.set(9, 10);


        BitSetUtils.accessBlank(10, bitSet, (from, len) -> {
            System.out.println(String.format("from %d len  %d", from, len));
        });

        System.out.println(bitSet);

    }

}
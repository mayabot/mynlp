/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.mayabot.mynlp.common.countbloom;

import java.util.List;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;

/**
 * Collections of strategies of generating the k * log(M) bits required for an
 * element to be mapped to a BloomFilter of M bits and k hash functions. These
 * strategies are part of the serialized form of the Bloom filters that use
 * them, thus they must be preserved as is (no updates allowed, only
 * introduction of new versions).
 *
 * Important: the order of the constants cannot change, and they cannot be
 * deleted - we depend on their ordinal for BloomFilter serialization.
 *
 * @author Dimitris Andreou
 */
enum CountBloomFilterStrategies implements CountBloomFilter.Strategy {

    /**
     * See "Less Hashing, Same Performance: Building a Better Bloom Filter" by
     * Adam Kirsch and Michael Mitzenmacher. The paper argues that this trick
     * doesn't significantly deteriorate the performance of a Bloom filter (yet
     * only needs two 32bit hash functions).
     */
    MURMUR128_MITZ_32() {
        @Override
        public <T> boolean put(T object, Funnel<? super T> funnel,
                               int numHashFunctions, short[] bits,List<int[]> history) {
            long hash64 = Hashing.murmur3_128().hashObject(object, funnel)
                    .asLong();
            int hash1 = (int) hash64;
            int hash2 = (int) (hash64 >>> 32);
            int[] indexs = new int[numHashFunctions];
            boolean hasZero = false;//是不是第一次置入,true，第一次put. 如果false，多次put

            for (int i = 1; i <= numHashFunctions; i++) {
                int nextHash = hash1 + i * hash2;
                if (nextHash < 0) {
                    nextHash = ~nextHash;
                }
                int index = nextHash % bits.length;
                indexs[i-1] = index;
                short data = bits[index];
                if(data==Short.MAX_VALUE){
                    //防止溢出
                }else{
                    if(data==0){hasZero=true;}
                    bits[index]++;
                }
            }
            if(hasZero){//bit changes 100%第一次置入
                //	System.out.println("first" + object);
                history.add(indexs);
            }else{
                //System.out.println("repeat" + object);
            }
            return true;
        }

        /**
         * 返回Hash对应的几个数字索引下标
         *
         * @param object
         * @param funnel
         * @param numHashFunctions
         * @param bits
         * @return
         */
        @Override
        public <T> int[] dataIndex(T object, Funnel<? super T> funnel,
                                   int numHashFunctions,int bitLength) {
            int[] index = new int[numHashFunctions];
            long hash64 = Hashing.murmur3_128().hashObject(object, funnel)
                    .asLong();
            int hash1 = (int) hash64;
            int hash2 = (int) (hash64 >>> 32);
            for (int i = 1; i <= numHashFunctions; i++) {
                int nextHash = hash1 + i * hash2;
                if (nextHash < 0) {
                    nextHash = ~nextHash;
                }
                index[i - 1] = nextHash % bitLength;
            }
            return index;
        }

        @Override
        public <T> int mayCount(T object, Funnel<? super T> funnel,
                                int numHashFunctions, short[] bits) {
            int count = Integer.MAX_VALUE;
            long hash64 = Hashing.murmur3_128().hashObject(object, funnel)
                    .asLong();
            int hash1 = (int) hash64;
            int hash2 = (int) (hash64 >>> 32);
            for (int i = 1; i <= numHashFunctions; i++) {
                int nextHash = hash1 + i * hash2;
                if (nextHash < 0) {
                    nextHash = ~nextHash;
                }
                int d = bits[nextHash % bits.length];
                if (d < count) {
                    count = d;
                }// 挑选最小的
            }
            return count;
        }
    };

}

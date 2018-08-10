/*
 * Copyright 2014 Takao Nakaguchi
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
package org.trie4j.bv;

import org.trie4j.util.IntArray;

import java.io.Serializable;
import java.util.Arrays;

public class LongsSuccinctBitVector
        implements Serializable, SuccinctBitVector {
    public LongsSuccinctBitVector() {
        this(16);
    }

    public LongsSuccinctBitVector(int initialCapacity) {
        this.longs = new long[longsSize(initialCapacity)];
        this.countCache0 = new int[countCache0Size(initialCapacity)];
        this.indexCache0 = new IntArray(initialIndexCache0Size(initialCapacity));
    }

    public LongsSuccinctBitVector(byte[] bytes, int bitsSize) {
        this.size = bitsSize;
        this.longs = new long[longsSize(bitsSize)];
        this.countCache0 = new int[countCache0Size(bitsSize)];
        this.indexCache0 = new IntArray(initialIndexCache0Size(bitsSize));

        int n = bytes.length;
        for (int i = 0; i < n; i++) {
            int b = bytes[i] & 0xff;
            longs[i / 8] |= (long) b << ((7 - (i % 8)) * 8);
            byte[] zeroPosInB = BITPOS0[b];
            int rest = bitsSize - i * 8;
            if (rest < 8) {
                // 残りより後の0の位置は扱わない
                int nz = zeroPosInB.length;
                for (int j = 0; j < nz; j++) {
                    if (zeroPosInB[j] >= rest) {
                        zeroPosInB = Arrays.copyOf(zeroPosInB, j);
                        break;
                    }
                }
            }
            int zeroCount = zeroPosInB.length;
            if (size0 < 3 && zeroCount > 0) {
                if (size0 == 0) {
                    node1pos = zeroPosInB[0] + 8 * i;
                    if (zeroPosInB.length > 1) node2pos = zeroPosInB[1] + 8 * i;
                    if (zeroPosInB.length > 2) node3pos = zeroPosInB[2] + 8 * i;
                } else if (size0 == 1) {
                    node2pos = zeroPosInB[0] + 8 * i;
                    if (zeroPosInB.length > 1) node3pos = zeroPosInB[1] + 8 * i;
                } else {
                    node3pos = zeroPosInB[0] + 8 * i;
                }
            }

            int prevSize0 = size0;
            size0 += zeroCount;
            if ((i + 1) % (BITS_IN_COUNTCACHE0 / 8) == 0) {
                countCache0[i / (BITS_IN_COUNTCACHE0 / 8)] = size0;
            }
            int indexOfIndexBlock = size0 / ZEROBITS_IN_EACH_INDEX;
            if (zeroCount > 0 && (indexOfIndexBlock != (prevSize0 / ZEROBITS_IN_EACH_INDEX))) {
                indexCache0.set(
                        indexOfIndexBlock,
                        i * 8 +
                                zeroPosInB[zeroPosInB.length - (size0 % ZEROBITS_IN_EACH_INDEX) - 1]);
            }

            if (rest < 8) break;
        }
        countCache0[(size - 1) / BITS_IN_COUNTCACHE0] = size0;
    }

    public LongsSuccinctBitVector(
            long[] longs, int size, int size0,
            int node1pos, int node2pos, int node3pos,
            int[] countCache0, IntArray indexCache0) {
        this.longs = longs;
        this.size = size;
        this.size0 = size0;
        this.node1pos = node1pos;
        this.node2pos = node2pos;
        this.node3pos = node3pos;
        this.countCache0 = countCache0;
        this.indexCache0 = indexCache0;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        int n = Math.min(size, 64);
        for (int i = 0; i < n; i++) {
            long m = 0x8000000000000000L >>> (i % BITS_IN_BLOCK);
            long bi = longs[(i / BITS_IN_BLOCK)] & m;
            b.append(bi != 0 ? "1" : "0");
        }
        return b.toString();
    }

    public long[] getLongs() {
        return longs;
    }

    public int[] getCountCache0() {
        return countCache0;
    }

    public IntArray getIndexCache0() {
        return indexCache0;
    }

    @Override
    public boolean get(int pos) {
        return isOne(pos);
    }

    public boolean isZero(int pos) {
        return (longs[pos / BITS_IN_BLOCK] & (0x8000000000000000L >>> pos % BITS_IN_BLOCK)) == 0;
    }

    public boolean isOne(int pos) {
        return (longs[pos / BITS_IN_BLOCK] & (0x8000000000000000L >>> pos % BITS_IN_BLOCK)) != 0;
    }

    public int size() {
        return size;
    }

    public int getSize0() {
        return size0;
    }

    public int getNode1pos() {
        return node1pos;
    }

    public int getNode2pos() {
        return node2pos;
    }

    public int getNode3pos() {
        return node3pos;
    }

    public void trimToSize() {
        longs = Arrays.copyOf(longs, longsSize(size));
        countCache0 = Arrays.copyOf(countCache0, countCache0Size(size));
        indexCache0.trimToSize();
    }

    public void append0() {
        int longsi = size / BITS_IN_BLOCK;
        int countCachei = size / BITS_IN_COUNTCACHE0;
        if (longsi >= longs.length) {
            extendLongsAndCountCache0();
        }
        if (size % BITS_IN_COUNTCACHE0 == 0 && countCachei > 0) {
            countCache0[countCachei] = countCache0[countCachei - 1];
        }
        countCache0[countCachei]++;

        size0++;
        switch (size0) {
            case 1:
                node1pos = size;
                break;
            case 2:
                node2pos = size;
                break;
            case 3:
                node3pos = size;
                break;
        }
        if (size0 % ZEROBITS_IN_EACH_INDEX == 0) {
            indexCache0.set(size0 / ZEROBITS_IN_EACH_INDEX, size);
        }

        size++;
    }

    public void append1() {
        int longsi = size / BITS_IN_BLOCK;
        int countCachei = size / BITS_IN_COUNTCACHE0;
        if (longsi >= longs.length) {
            extendLongsAndCountCache0();
        }
        if (size % BITS_IN_COUNTCACHE0 == 0 && countCachei > 0) {
            countCache0[countCachei] = countCache0[countCachei - 1];
        }
        longs[longsi] |= 0x8000000000000000L >>> (size % BITS_IN_BLOCK);
        size++;
    }

    public void append(boolean bit) {
        if (bit) append1();
        else append0();
    }

    public int rank1(int pos) {
        int cn = pos / BITS_IN_COUNTCACHE0;
        int ret = (cn > 0) ? cn * BITS_IN_COUNTCACHE0 - countCache0[cn - 1] : 0;
        int n = pos / BITS_IN_BLOCK;
        for (int i = (cn * BITS_IN_COUNTCACHE0 / BITS_IN_BLOCK); i < n; i++) {
            ret += Long.bitCount(longs[i]);
        }
        return ret + Long.bitCount(longs[n] & (0x8000000000000000L >> (pos % BITS_IN_BLOCK)));
    }

    public int rank0(int pos) {
        int cn = pos / BITS_IN_COUNTCACHE0;
        if ((pos + 1) % BITS_IN_COUNTCACHE0 == 0) return countCache0[cn];
        int ret = (cn > 0) ? countCache0[cn - 1] : 0;
        int n = pos / BITS_IN_BLOCK;
        for (int i = (cn * BITS_IN_COUNTCACHE0 / BITS_IN_BLOCK); i < n; i++) {
            ret += Long.bitCount(~longs[i]);
        }
        return ret + Long.bitCount(~longs[n] & (0x8000000000000000L >> (pos % BITS_IN_BLOCK)));
    }

    public int rank(int pos, boolean b) {
        if (b) return rank1(pos);
        else return rank0(pos);
    }

    @Override
    public int select0(int count) {
        if (count > size0) return -1;
        if (count <= 3) {
            if (count == 1) return node1pos;
            else if (count == 2) return node2pos;
            else if (count == 3) return node3pos;
            else return -1;
        }

        int idx = count / ZEROBITS_IN_EACH_INDEX;
        int start = 0;
        int end = 0;
        if (idx < indexCache0.size()) {
            start = indexCache0.get(idx);
            if (count % BITS_IN_COUNTCACHE0 == 0) return start;
            start /= BITS_IN_COUNTCACHE0;
            if (idx + 1 < indexCache0.size()) {
                end = indexCache0.get(idx + 1) / BITS_IN_COUNTCACHE0 + 1;
            } else {
                end = countCache0Size(size);
            }
        } else if (idx > 0) {
            start = indexCache0.get(idx - 1) / BITS_IN_COUNTCACHE0;
            end = Math.min(start + BITS_IN_COUNTCACHE0, countCache0Size(size));
        }

        int m = -1;
        int d = 0;
        if (start != end) {
            do {
                m = (start + end) / 2;
                d = count - countCache0[m];
                if (d < 0) {
                    end = m;
                    continue;
                } else if (d > 0) {
                    if (start != m) start = m;
                    else break;
                } else {
                    break;
                }
            } while (start != end);
            if (d > 0) {
                count = d;
            } else {
                while (m >= 0 && count <= countCache0[m]) m--;
                if (m >= 0) count -= countCache0[m];
            }
        }

        int n = longs.length;
        for (int i = (m + 1) * BITS_IN_COUNTCACHE0 / BITS_IN_BLOCK; i < n; i++) {
            long v = longs[i];
            int c = Long.bitCount(~v);
            int cd = count - c;
            if (cd <= 0) {
                int b = 0, bc = 0;
                if ((v & 0xffff000000000000L) != 0xffff000000000000L) {
                    b = (int) (v >>> 56);
                    bc = BITCOUNTS0[b];
                    count -= bc;
                    if (count <= 0) return i * BITS_IN_BLOCK + BITPOS0[b][count + bc - 1];
                    b = (int) ((v >>> 48) & 0xff);
                    bc = BITCOUNTS0[b];
                    count -= bc;
                    if (count <= 0) return i * BITS_IN_BLOCK + 8 + BITPOS0[b][count + bc - 1];
                }
                if ((v & 0x0000ffff00000000L) != 0x0000ffff00000000L) {
                    b = (int) ((v >>> 40) & 0xff);
                    bc = BITCOUNTS0[b];
                    count -= bc;
                    if (count <= 0) return i * BITS_IN_BLOCK + 16 + BITPOS0[b][count + bc - 1];
                    b = (int) ((v >>> 32) & 0xff);
                    bc = BITCOUNTS0[b];
                    count -= bc;
                    if (count <= 0) return i * BITS_IN_BLOCK + 24 + BITPOS0[b][count + bc - 1];
                }
                if ((v & 0x00000000ffff0000L) != 0x00000000ffff0000L) {
                    b = (int) ((v >>> 24) & 0xff);
                    bc = BITCOUNTS0[b];
                    count -= bc;
                    if (count <= 0) return i * BITS_IN_BLOCK + 32 + BITPOS0[b][count + bc - 1];
                    b = (int) ((v >>> 16) & 0xff);
                    bc = BITCOUNTS0[b];
                    count -= bc;
                    if (count <= 0) return i * BITS_IN_BLOCK + 40 + BITPOS0[b][count + bc - 1];
                }
                b = (int) ((v >>> 8) & 0xff);
                bc = BITCOUNTS0[b];
                count -= bc;
                if (count <= 0) return i * BITS_IN_BLOCK + 48 + BITPOS0[b][count + bc - 1];
                b = (int) (v & 0xff);
                bc = BITCOUNTS0[b];
                count -= bc;
                if (count <= 0) return i * BITS_IN_BLOCK + 56 + BITPOS0[b][count + bc - 1];

/*				if((v & 0xffffffff00000000L) == 0xffffffff00000000L){
					v <<= 32;
					for(int j = 32; j < 64; j++){
						if(v >= 0){
							count--;
							if(count == 0) return i * BITS_IN_BLOCK + j;
						}
						v <<= 1;
					}
				} else{
					for(int j = 0; j < 64; j++){
						if(v >= 0){
							count--;
							if(count == 0) return i * BITS_IN_BLOCK + j;
						}
						v <<= 1;
					}
				}
*/
                return -1;
            }
            count = cd;
        }
        return -1;
    }

    @Override
    public int select1(int count) {
        for (int i = 0; i < longs.length; i++) {
            if (i * BITS_IN_BLOCK >= size) return -1;
            long v = longs[i];
            int c = Long.bitCount(v);
            if (count <= c) {
                for (int j = 0; j < BITS_IN_BLOCK; j++) {
                    if (i * BITS_IN_BLOCK + j >= size) return -1;
                    if ((v & 0x8000000000000000L) != 0) {
                        count--;
                        if (count == 0) {
                            return i * BITS_IN_BLOCK + j;
                        }
                    }
                    v <<= 1;
                }
                return -1;
            }
            count -= c;
        }
        return -1;
    }

    public int select(int count, boolean b) {
        if (b) return select1(count);
        else return select0(count);
    }

    public int next0(int pos) {
        if (pos >= size) return -1;
        if (pos <= node3pos) {
            if (pos <= node1pos) return node1pos;
            else if (pos <= node2pos) return node2pos;
            else return node3pos;
        }
        int longsi = pos / BITS_IN_BLOCK;
        int s = pos % BITS_IN_BLOCK;
        int n = longs.length - 1;
        if (longsi < n) {
            long v = longs[longsi] << s;
            for (int i = s; i < BITS_IN_BLOCK; i++) {
                if (v >= 0) return longsi * BITS_IN_BLOCK + i;
                v <<= 1;
            }
            longsi++;
            s = 0;
            for (; longsi < n; longsi++) {
                v = longs[longsi];
                if (Long.bitCount(v) == BITS_IN_BLOCK) continue;
                for (int i = 0; i < BITS_IN_BLOCK; i++) {
                    if (v >= 0) return longsi * BITS_IN_BLOCK + i;
                    v <<= 1;
                }
            }
        }
        long v = longs[longsi] << s;
        if (Long.bitCount(v) != (BITS_IN_BLOCK - s)) {
            int in = size % BITS_IN_BLOCK;
            for (int i = s; i < in; i++) {
                if (v >= 0) return longsi * BITS_IN_BLOCK + i;
                v <<= 1;
            }
        }
        return -1;
    }

    private void extendLongsAndCountCache0() {
        int longsSize = (int) (longs.length * 1.2) + 1;
        longs = Arrays.copyOf(longs, longsSize);
        countCache0 = Arrays.copyOf(countCache0, countCache0Size(longsSize * BITS_IN_BLOCK));
    }

    private static int longsSize(int bitSize) {
        if (bitSize == 0) return 0;
        return (bitSize - 1) / BITS_IN_BLOCK + 1;
    }

    private static int countCache0Size(int bitSize) {
        if (bitSize == 0) return 0;
        return (bitSize - 1) / BITS_IN_COUNTCACHE0 + 1;
    }

    private static int initialIndexCache0Size(int bitSize) {
        if (bitSize == 0) return 0;
        return bitSize / 2 / ZEROBITS_IN_EACH_INDEX;
    }

    static final int BITS_IN_BLOCK = 64;
    static final int BITS_IN_COUNTCACHE0 = 1 * 64;
    static final int ZEROBITS_IN_EACH_INDEX = 1 * 64;
    private long[] longs;
    private int size;
    private int size0;
    private int node1pos = -1;
    private int node2pos = -1;
    private int node3pos = -1;
    private int[] countCache0;
    private IntArray indexCache0;

    private static final byte[] BITCOUNTS0 = {
            8, 7, 7, 6, 7, 6, 6, 5, 7, 6, 6, 5, 6, 5, 5, 4,
            7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3,
            7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3,
            6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2,
            7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3,
            6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2,
            6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2,
            5, 4, 4, 3, 4, 3, 3, 2, 4, 3, 3, 2, 3, 2, 2, 1,
            7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3,
            6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2,
            6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2,
            5, 4, 4, 3, 4, 3, 3, 2, 4, 3, 3, 2, 3, 2, 2, 1,
            6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2,
            5, 4, 4, 3, 4, 3, 3, 2, 4, 3, 3, 2, 3, 2, 2, 1,
            5, 4, 4, 3, 4, 3, 3, 2, 4, 3, 3, 2, 3, 2, 2, 1,
            4, 3, 3, 2, 3, 2, 2, 1, 3, 2, 2, 1, 2, 1, 1, 0,
    };
    private static final byte[][] BITPOS0 = {
            {0, 1, 2, 3, 4, 5, 6, 7,}, // 0(0)
            {0, 1, 2, 3, 4, 5, 6,}, // 1(1)
            {0, 1, 2, 3, 4, 5, 7,}, // 2(2)
            {0, 1, 2, 3, 4, 5,}, // 3(3)
            {0, 1, 2, 3, 4, 6, 7,}, // 4(4)
            {0, 1, 2, 3, 4, 6,}, // 5(5)
            {0, 1, 2, 3, 4, 7,}, // 6(6)
            {0, 1, 2, 3, 4,}, // 7(7)
            {0, 1, 2, 3, 5, 6, 7,}, // 8(8)
            {0, 1, 2, 3, 5, 6,}, // 9(9)
            {0, 1, 2, 3, 5, 7,}, // 10(a)
            {0, 1, 2, 3, 5,}, // 11(b)
            {0, 1, 2, 3, 6, 7,}, // 12(c)
            {0, 1, 2, 3, 6,}, // 13(d)
            {0, 1, 2, 3, 7,}, // 14(e)
            {0, 1, 2, 3,}, // 15(f)
            {0, 1, 2, 4, 5, 6, 7,}, // 16(10)
            {0, 1, 2, 4, 5, 6,}, // 17(11)
            {0, 1, 2, 4, 5, 7,}, // 18(12)
            {0, 1, 2, 4, 5,}, // 19(13)
            {0, 1, 2, 4, 6, 7,}, // 20(14)
            {0, 1, 2, 4, 6,}, // 21(15)
            {0, 1, 2, 4, 7,}, // 22(16)
            {0, 1, 2, 4,}, // 23(17)
            {0, 1, 2, 5, 6, 7,}, // 24(18)
            {0, 1, 2, 5, 6,}, // 25(19)
            {0, 1, 2, 5, 7,}, // 26(1a)
            {0, 1, 2, 5,}, // 27(1b)
            {0, 1, 2, 6, 7,}, // 28(1c)
            {0, 1, 2, 6,}, // 29(1d)
            {0, 1, 2, 7,}, // 30(1e)
            {0, 1, 2,}, // 31(1f)
            {0, 1, 3, 4, 5, 6, 7,}, // 32(20)
            {0, 1, 3, 4, 5, 6,}, // 33(21)
            {0, 1, 3, 4, 5, 7,}, // 34(22)
            {0, 1, 3, 4, 5,}, // 35(23)
            {0, 1, 3, 4, 6, 7,}, // 36(24)
            {0, 1, 3, 4, 6,}, // 37(25)
            {0, 1, 3, 4, 7,}, // 38(26)
            {0, 1, 3, 4,}, // 39(27)
            {0, 1, 3, 5, 6, 7,}, // 40(28)
            {0, 1, 3, 5, 6,}, // 41(29)
            {0, 1, 3, 5, 7,}, // 42(2a)
            {0, 1, 3, 5,}, // 43(2b)
            {0, 1, 3, 6, 7,}, // 44(2c)
            {0, 1, 3, 6,}, // 45(2d)
            {0, 1, 3, 7,}, // 46(2e)
            {0, 1, 3,}, // 47(2f)
            {0, 1, 4, 5, 6, 7,}, // 48(30)
            {0, 1, 4, 5, 6,}, // 49(31)
            {0, 1, 4, 5, 7,}, // 50(32)
            {0, 1, 4, 5,}, // 51(33)
            {0, 1, 4, 6, 7,}, // 52(34)
            {0, 1, 4, 6,}, // 53(35)
            {0, 1, 4, 7,}, // 54(36)
            {0, 1, 4,}, // 55(37)
            {0, 1, 5, 6, 7,}, // 56(38)
            {0, 1, 5, 6,}, // 57(39)
            {0, 1, 5, 7,}, // 58(3a)
            {0, 1, 5,}, // 59(3b)
            {0, 1, 6, 7,}, // 60(3c)
            {0, 1, 6,}, // 61(3d)
            {0, 1, 7,}, // 62(3e)
            {0, 1,}, // 63(3f)
            {0, 2, 3, 4, 5, 6, 7,}, // 64(40)
            {0, 2, 3, 4, 5, 6,}, // 65(41)
            {0, 2, 3, 4, 5, 7,}, // 66(42)
            {0, 2, 3, 4, 5,}, // 67(43)
            {0, 2, 3, 4, 6, 7,}, // 68(44)
            {0, 2, 3, 4, 6,}, // 69(45)
            {0, 2, 3, 4, 7,}, // 70(46)
            {0, 2, 3, 4,}, // 71(47)
            {0, 2, 3, 5, 6, 7,}, // 72(48)
            {0, 2, 3, 5, 6,}, // 73(49)
            {0, 2, 3, 5, 7,}, // 74(4a)
            {0, 2, 3, 5,}, // 75(4b)
            {0, 2, 3, 6, 7,}, // 76(4c)
            {0, 2, 3, 6,}, // 77(4d)
            {0, 2, 3, 7,}, // 78(4e)
            {0, 2, 3,}, // 79(4f)
            {0, 2, 4, 5, 6, 7,}, // 80(50)
            {0, 2, 4, 5, 6,}, // 81(51)
            {0, 2, 4, 5, 7,}, // 82(52)
            {0, 2, 4, 5,}, // 83(53)
            {0, 2, 4, 6, 7,}, // 84(54)
            {0, 2, 4, 6,}, // 85(55)
            {0, 2, 4, 7,}, // 86(56)
            {0, 2, 4,}, // 87(57)
            {0, 2, 5, 6, 7,}, // 88(58)
            {0, 2, 5, 6,}, // 89(59)
            {0, 2, 5, 7,}, // 90(5a)
            {0, 2, 5,}, // 91(5b)
            {0, 2, 6, 7,}, // 92(5c)
            {0, 2, 6,}, // 93(5d)
            {0, 2, 7,}, // 94(5e)
            {0, 2,}, // 95(5f)
            {0, 3, 4, 5, 6, 7,}, // 96(60)
            {0, 3, 4, 5, 6,}, // 97(61)
            {0, 3, 4, 5, 7,}, // 98(62)
            {0, 3, 4, 5,}, // 99(63)
            {0, 3, 4, 6, 7,}, // 100(64)
            {0, 3, 4, 6,}, // 101(65)
            {0, 3, 4, 7,}, // 102(66)
            {0, 3, 4,}, // 103(67)
            {0, 3, 5, 6, 7,}, // 104(68)
            {0, 3, 5, 6,}, // 105(69)
            {0, 3, 5, 7,}, // 106(6a)
            {0, 3, 5,}, // 107(6b)
            {0, 3, 6, 7,}, // 108(6c)
            {0, 3, 6,}, // 109(6d)
            {0, 3, 7,}, // 110(6e)
            {0, 3,}, // 111(6f)
            {0, 4, 5, 6, 7,}, // 112(70)
            {0, 4, 5, 6,}, // 113(71)
            {0, 4, 5, 7,}, // 114(72)
            {0, 4, 5,}, // 115(73)
            {0, 4, 6, 7,}, // 116(74)
            {0, 4, 6,}, // 117(75)
            {0, 4, 7,}, // 118(76)
            {0, 4,}, // 119(77)
            {0, 5, 6, 7,}, // 120(78)
            {0, 5, 6,}, // 121(79)
            {0, 5, 7,}, // 122(7a)
            {0, 5,}, // 123(7b)
            {0, 6, 7,}, // 124(7c)
            {0, 6,}, // 125(7d)
            {0, 7,}, // 126(7e)
            {0,}, // 127(7f)
            {1, 2, 3, 4, 5, 6, 7,}, // 128(80)
            {1, 2, 3, 4, 5, 6,}, // 129(81)
            {1, 2, 3, 4, 5, 7,}, // 130(82)
            {1, 2, 3, 4, 5,}, // 131(83)
            {1, 2, 3, 4, 6, 7,}, // 132(84)
            {1, 2, 3, 4, 6,}, // 133(85)
            {1, 2, 3, 4, 7,}, // 134(86)
            {1, 2, 3, 4,}, // 135(87)
            {1, 2, 3, 5, 6, 7,}, // 136(88)
            {1, 2, 3, 5, 6,}, // 137(89)
            {1, 2, 3, 5, 7,}, // 138(8a)
            {1, 2, 3, 5,}, // 139(8b)
            {1, 2, 3, 6, 7,}, // 140(8c)
            {1, 2, 3, 6,}, // 141(8d)
            {1, 2, 3, 7,}, // 142(8e)
            {1, 2, 3,}, // 143(8f)
            {1, 2, 4, 5, 6, 7,}, // 144(90)
            {1, 2, 4, 5, 6,}, // 145(91)
            {1, 2, 4, 5, 7,}, // 146(92)
            {1, 2, 4, 5,}, // 147(93)
            {1, 2, 4, 6, 7,}, // 148(94)
            {1, 2, 4, 6,}, // 149(95)
            {1, 2, 4, 7,}, // 150(96)
            {1, 2, 4,}, // 151(97)
            {1, 2, 5, 6, 7,}, // 152(98)
            {1, 2, 5, 6,}, // 153(99)
            {1, 2, 5, 7,}, // 154(9a)
            {1, 2, 5,}, // 155(9b)
            {1, 2, 6, 7,}, // 156(9c)
            {1, 2, 6,}, // 157(9d)
            {1, 2, 7,}, // 158(9e)
            {1, 2,}, // 159(9f)
            {1, 3, 4, 5, 6, 7,}, // 160(a0)
            {1, 3, 4, 5, 6,}, // 161(a1)
            {1, 3, 4, 5, 7,}, // 162(a2)
            {1, 3, 4, 5,}, // 163(a3)
            {1, 3, 4, 6, 7,}, // 164(a4)
            {1, 3, 4, 6,}, // 165(a5)
            {1, 3, 4, 7,}, // 166(a6)
            {1, 3, 4,}, // 167(a7)
            {1, 3, 5, 6, 7,}, // 168(a8)
            {1, 3, 5, 6,}, // 169(a9)
            {1, 3, 5, 7,}, // 170(aa)
            {1, 3, 5,}, // 171(ab)
            {1, 3, 6, 7,}, // 172(ac)
            {1, 3, 6,}, // 173(ad)
            {1, 3, 7,}, // 174(ae)
            {1, 3,}, // 175(af)
            {1, 4, 5, 6, 7,}, // 176(b0)
            {1, 4, 5, 6,}, // 177(b1)
            {1, 4, 5, 7,}, // 178(b2)
            {1, 4, 5,}, // 179(b3)
            {1, 4, 6, 7,}, // 180(b4)
            {1, 4, 6,}, // 181(b5)
            {1, 4, 7,}, // 182(b6)
            {1, 4,}, // 183(b7)
            {1, 5, 6, 7,}, // 184(b8)
            {1, 5, 6,}, // 185(b9)
            {1, 5, 7,}, // 186(ba)
            {1, 5,}, // 187(bb)
            {1, 6, 7,}, // 188(bc)
            {1, 6,}, // 189(bd)
            {1, 7,}, // 190(be)
            {1,}, // 191(bf)
            {2, 3, 4, 5, 6, 7,}, // 192(c0)
            {2, 3, 4, 5, 6,}, // 193(c1)
            {2, 3, 4, 5, 7,}, // 194(c2)
            {2, 3, 4, 5,}, // 195(c3)
            {2, 3, 4, 6, 7,}, // 196(c4)
            {2, 3, 4, 6,}, // 197(c5)
            {2, 3, 4, 7,}, // 198(c6)
            {2, 3, 4,}, // 199(c7)
            {2, 3, 5, 6, 7,}, // 200(c8)
            {2, 3, 5, 6,}, // 201(c9)
            {2, 3, 5, 7,}, // 202(ca)
            {2, 3, 5,}, // 203(cb)
            {2, 3, 6, 7,}, // 204(cc)
            {2, 3, 6,}, // 205(cd)
            {2, 3, 7,}, // 206(ce)
            {2, 3,}, // 207(cf)
            {2, 4, 5, 6, 7,}, // 208(d0)
            {2, 4, 5, 6,}, // 209(d1)
            {2, 4, 5, 7,}, // 210(d2)
            {2, 4, 5,}, // 211(d3)
            {2, 4, 6, 7,}, // 212(d4)
            {2, 4, 6,}, // 213(d5)
            {2, 4, 7,}, // 214(d6)
            {2, 4,}, // 215(d7)
            {2, 5, 6, 7,}, // 216(d8)
            {2, 5, 6,}, // 217(d9)
            {2, 5, 7,}, // 218(da)
            {2, 5,}, // 219(db)
            {2, 6, 7,}, // 220(dc)
            {2, 6,}, // 221(dd)
            {2, 7,}, // 222(de)
            {2,}, // 223(df)
            {3, 4, 5, 6, 7,}, // 224(e0)
            {3, 4, 5, 6,}, // 225(e1)
            {3, 4, 5, 7,}, // 226(e2)
            {3, 4, 5,}, // 227(e3)
            {3, 4, 6, 7,}, // 228(e4)
            {3, 4, 6,}, // 229(e5)
            {3, 4, 7,}, // 230(e6)
            {3, 4,}, // 231(e7)
            {3, 5, 6, 7,}, // 232(e8)
            {3, 5, 6,}, // 233(e9)
            {3, 5, 7,}, // 234(ea)
            {3, 5,}, // 235(eb)
            {3, 6, 7,}, // 236(ec)
            {3, 6,}, // 237(ed)
            {3, 7,}, // 238(ee)
            {3,}, // 239(ef)
            {4, 5, 6, 7,}, // 240(f0)
            {4, 5, 6,}, // 241(f1)
            {4, 5, 7,}, // 242(f2)
            {4, 5,}, // 243(f3)
            {4, 6, 7,}, // 244(f4)
            {4, 6,}, // 245(f5)
            {4, 7,}, // 246(f6)
            {4,}, // 247(f7)
            {5, 6, 7,}, // 248(f8)
            {5, 6,}, // 249(f9)
            {5, 7,}, // 250(fa)
            {5,}, // 251(fb)
            {6, 7,}, // 252(fc)
            {6,}, // 253(fd)
            {7,}, // 254(fe)
            {}, // 255(ff)
    };

    private static final long serialVersionUID = -7658605229245494623L;
}

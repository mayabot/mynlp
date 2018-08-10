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

import java.io.Serializable;
import java.util.Arrays;

public class LongsRank1OnlySuccinctBitVector
        implements Serializable, SuccinctBitVector {
    public LongsRank1OnlySuccinctBitVector() {
        this(16);
    }

    public LongsRank1OnlySuccinctBitVector(int initialCapacity) {
        if (initialCapacity == 0) {
            this.longs = new long[]{};
            this.countCache1 = new int[]{};
        } else {
            this.longs = new long[longsSize(initialCapacity)];
            this.countCache1 = new int[countCacheSize(initialCapacity)];
        }
    }

    public LongsRank1OnlySuccinctBitVector(byte[] bytes, int bitsSize) {
        this.size = bitsSize;
        this.longs = new long[longsSize(bitsSize)];
        this.countCache1 = new int[countCacheSize(bitsSize)];

        int n = bytes.length;
        for (int i = 0; i < n; i++) {
            int b = bytes[i] & 0xff;
            longs[i / 8] |= (long) b << ((7 - (i % 8)) * 8);
            byte[] onePosInB = BITPOS1[b];
            int rest = bitsSize - i * 8;
            if (rest < 8) {
                // 残りより後の0の位置は扱わない
                int nz = onePosInB.length;
                for (int j = 0; j < nz; j++) {
                    if (onePosInB[j] >= rest) {
                        onePosInB = Arrays.copyOf(onePosInB, j);
                        break;
                    }
                }
            }
            size1 += onePosInB.length;
            if ((i + 1) % (BITS_IN_COUNTCACHE / 8) == 0) {
                countCache1[i / (BITS_IN_COUNTCACHE / 8)] = size1;
            }

            if (rest < 8) break;
        }
        countCache1[(size - 1) / BITS_IN_COUNTCACHE] = size1;
    }

    public LongsRank1OnlySuccinctBitVector(
            long[] longs, int size, int size1,
            int[] countCache1) {
        this.longs = longs;
        this.size = size;
        this.size1 = size1;
        this.countCache1 = countCache1;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        int n = Math.min(size, 64);
        for (int i = 0; i < n; i++) {
            long m = 0x8000000000000000L >>> (i % BITS_IN_BLOCK);
            long bi = longs[(i / BITS_IN_BLOCK)] & m;
            b.append((bi) != 0 ? "1" : "0");
        }
        return b.toString();
    }

    public long[] getLongs() {
        return longs;
    }

    public int[] getCountCache1() {
        return countCache1;
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

    public int getSize1() {
        return size1;
    }

    public void trimToSize() {
        longs = Arrays.copyOf(longs, longsSize(size));
        countCache1 = Arrays.copyOf(countCache1, countCacheSize(size));
    }

    public void append1() {
        int longsi = size / BITS_IN_BLOCK;
        int countCachei = size / BITS_IN_COUNTCACHE;
        if (longsi >= longs.length) {
            extendLongsAndCountCache();
        }
        if (size % BITS_IN_COUNTCACHE == 0 && countCachei > 0) {
            countCache1[countCachei] = countCache1[countCachei - 1];
        }
        longs[longsi] |= 0x8000000000000000L >>> (size % BITS_IN_BLOCK);
        size++;

        countCache1[countCachei]++;
        size1++;
    }

    public void append0() {
        int longsi = size / BITS_IN_BLOCK;
        int countCachei = size / BITS_IN_COUNTCACHE;
        if (longsi >= longs.length) {
            extendLongsAndCountCache();
        }
        if (size % BITS_IN_COUNTCACHE == 0 && countCachei > 0) {
            countCache1[countCachei] = countCache1[countCachei - 1];
        }
        size++;
    }

    public void append(boolean bit) {
        if (bit) append1();
        else append0();
    }

    public int rank0(int pos) {
        int cn = pos / BITS_IN_COUNTCACHE;
        if ((pos + 1) % BITS_IN_COUNTCACHE == 0) return (cn + 1) * BITS_IN_COUNTCACHE - countCache1[cn];
        int ret = (cn > 0) ? cn * BITS_IN_COUNTCACHE - countCache1[cn - 1] : 0;
        int n = pos / BITS_IN_BLOCK;
        for (int i = (cn * BITS_IN_COUNTCACHE / BITS_IN_BLOCK); i < n; i++) {
            ret += Long.bitCount(~longs[i]);
        }
        return ret + Long.bitCount(~longs[n] & (0x8000000000000000L >> (pos % BITS_IN_BLOCK)));
    }

    public int rank1(int pos) {
        int cn = pos / BITS_IN_COUNTCACHE;
        if ((pos + 1) % BITS_IN_COUNTCACHE == 0) return countCache1[cn];
        int ret = (cn > 0) ? countCache1[cn - 1] : 0;
        int n = pos / BITS_IN_BLOCK;
        for (int i = (cn * BITS_IN_COUNTCACHE / BITS_IN_BLOCK); i < n; i++) {
            ret += Long.bitCount(longs[i]);
        }
        return ret + Long.bitCount(longs[n] & (0x8000000000000000L >> (pos % BITS_IN_BLOCK)));
    }

    public int rank(int pos, boolean b) {
        if (b) return rank1(pos);
        else return rank0(pos);
    }

    @Override
    public int select0(int count) {
        for (int i = 0; i < longs.length; i++) {
            if (i * BITS_IN_BLOCK >= size) return -1;
            long v = longs[i];
            int c = BITS_IN_BLOCK - Long.bitCount(v);
            if (count <= c) {
                for (int j = 0; j < BITS_IN_BLOCK; j++) {
                    if (i * BITS_IN_BLOCK + j >= size) return -1;
                    if ((v & 0x8000000000000000L) != 1) {
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
        int longsi = pos / BITS_IN_BLOCK;
        int s = pos % BITS_IN_BLOCK;
        for (; longsi < longs.length; longsi++) {
            long v = longs[longsi];
            for (int i = s; i < BITS_IN_BLOCK; i++) {
                int p = longsi * BITS_IN_BLOCK + i;
                if (p >= size) return -1;
                if ((v & (0x8000000000000000L >>> i)) == 0) {
                    return p;
                }
            }
            s = 0;
        }
        return -1;
    }

    private void extendLongsAndCountCache() {
        int longsSize = (int) (longs.length * 1.2) + 1;
        longs = Arrays.copyOf(longs, longsSize);
        int cacheSize = longsSize * BITS_IN_BLOCK / BITS_IN_COUNTCACHE + 1;
        countCache1 = Arrays.copyOf(countCache1, cacheSize);
    }

    private static int longsSize(int bitSize) {
        return (bitSize - 1) / BITS_IN_BLOCK + 1;
    }

    private static int countCacheSize(int bitSize) {
        return (bitSize - 1) / BITS_IN_COUNTCACHE + 1;
    }

    static final int BITS_IN_BLOCK = 64;
    static final int BITS_IN_COUNTCACHE = 1 * 64;
    private long[] longs;
    private int size;
    private int size1;
    private int[] countCache1;

    private static final byte[][] BITPOS1 = {
            {}, // 0(0)
            {7,}, // 1(1)
            {6,}, // 2(2)
            {6, 7,}, // 3(3)
            {5,}, // 4(4)
            {5, 7,}, // 5(5)
            {5, 6,}, // 6(6)
            {5, 6, 7,}, // 7(7)
            {4,}, // 8(8)
            {4, 7,}, // 9(9)
            {4, 6,}, // 10(a)
            {4, 6, 7,}, // 11(b)
            {4, 5,}, // 12(c)
            {4, 5, 7,}, // 13(d)
            {4, 5, 6,}, // 14(e)
            {4, 5, 6, 7,}, // 15(f)
            {3,}, // 16(10)
            {3, 7,}, // 17(11)
            {3, 6,}, // 18(12)
            {3, 6, 7,}, // 19(13)
            {3, 5,}, // 20(14)
            {3, 5, 7,}, // 21(15)
            {3, 5, 6,}, // 22(16)
            {3, 5, 6, 7,}, // 23(17)
            {3, 4,}, // 24(18)
            {3, 4, 7,}, // 25(19)
            {3, 4, 6,}, // 26(1a)
            {3, 4, 6, 7,}, // 27(1b)
            {3, 4, 5,}, // 28(1c)
            {3, 4, 5, 7,}, // 29(1d)
            {3, 4, 5, 6,}, // 30(1e)
            {3, 4, 5, 6, 7,}, // 31(1f)
            {2,}, // 32(20)
            {2, 7,}, // 33(21)
            {2, 6,}, // 34(22)
            {2, 6, 7,}, // 35(23)
            {2, 5,}, // 36(24)
            {2, 5, 7,}, // 37(25)
            {2, 5, 6,}, // 38(26)
            {2, 5, 6, 7,}, // 39(27)
            {2, 4,}, // 40(28)
            {2, 4, 7,}, // 41(29)
            {2, 4, 6,}, // 42(2a)
            {2, 4, 6, 7,}, // 43(2b)
            {2, 4, 5,}, // 44(2c)
            {2, 4, 5, 7,}, // 45(2d)
            {2, 4, 5, 6,}, // 46(2e)
            {2, 4, 5, 6, 7,}, // 47(2f)
            {2, 3,}, // 48(30)
            {2, 3, 7,}, // 49(31)
            {2, 3, 6,}, // 50(32)
            {2, 3, 6, 7,}, // 51(33)
            {2, 3, 5,}, // 52(34)
            {2, 3, 5, 7,}, // 53(35)
            {2, 3, 5, 6,}, // 54(36)
            {2, 3, 5, 6, 7,}, // 55(37)
            {2, 3, 4,}, // 56(38)
            {2, 3, 4, 7,}, // 57(39)
            {2, 3, 4, 6,}, // 58(3a)
            {2, 3, 4, 6, 7,}, // 59(3b)
            {2, 3, 4, 5,}, // 60(3c)
            {2, 3, 4, 5, 7,}, // 61(3d)
            {2, 3, 4, 5, 6,}, // 62(3e)
            {2, 3, 4, 5, 6, 7,}, // 63(3f)
            {1,}, // 64(40)
            {1, 7,}, // 65(41)
            {1, 6,}, // 66(42)
            {1, 6, 7,}, // 67(43)
            {1, 5,}, // 68(44)
            {1, 5, 7,}, // 69(45)
            {1, 5, 6,}, // 70(46)
            {1, 5, 6, 7,}, // 71(47)
            {1, 4,}, // 72(48)
            {1, 4, 7,}, // 73(49)
            {1, 4, 6,}, // 74(4a)
            {1, 4, 6, 7,}, // 75(4b)
            {1, 4, 5,}, // 76(4c)
            {1, 4, 5, 7,}, // 77(4d)
            {1, 4, 5, 6,}, // 78(4e)
            {1, 4, 5, 6, 7,}, // 79(4f)
            {1, 3,}, // 80(50)
            {1, 3, 7,}, // 81(51)
            {1, 3, 6,}, // 82(52)
            {1, 3, 6, 7,}, // 83(53)
            {1, 3, 5,}, // 84(54)
            {1, 3, 5, 7,}, // 85(55)
            {1, 3, 5, 6,}, // 86(56)
            {1, 3, 5, 6, 7,}, // 87(57)
            {1, 3, 4,}, // 88(58)
            {1, 3, 4, 7,}, // 89(59)
            {1, 3, 4, 6,}, // 90(5a)
            {1, 3, 4, 6, 7,}, // 91(5b)
            {1, 3, 4, 5,}, // 92(5c)
            {1, 3, 4, 5, 7,}, // 93(5d)
            {1, 3, 4, 5, 6,}, // 94(5e)
            {1, 3, 4, 5, 6, 7,}, // 95(5f)
            {1, 2,}, // 96(60)
            {1, 2, 7,}, // 97(61)
            {1, 2, 6,}, // 98(62)
            {1, 2, 6, 7,}, // 99(63)
            {1, 2, 5,}, // 100(64)
            {1, 2, 5, 7,}, // 101(65)
            {1, 2, 5, 6,}, // 102(66)
            {1, 2, 5, 6, 7,}, // 103(67)
            {1, 2, 4,}, // 104(68)
            {1, 2, 4, 7,}, // 105(69)
            {1, 2, 4, 6,}, // 106(6a)
            {1, 2, 4, 6, 7,}, // 107(6b)
            {1, 2, 4, 5,}, // 108(6c)
            {1, 2, 4, 5, 7,}, // 109(6d)
            {1, 2, 4, 5, 6,}, // 110(6e)
            {1, 2, 4, 5, 6, 7,}, // 111(6f)
            {1, 2, 3,}, // 112(70)
            {1, 2, 3, 7,}, // 113(71)
            {1, 2, 3, 6,}, // 114(72)
            {1, 2, 3, 6, 7,}, // 115(73)
            {1, 2, 3, 5,}, // 116(74)
            {1, 2, 3, 5, 7,}, // 117(75)
            {1, 2, 3, 5, 6,}, // 118(76)
            {1, 2, 3, 5, 6, 7,}, // 119(77)
            {1, 2, 3, 4,}, // 120(78)
            {1, 2, 3, 4, 7,}, // 121(79)
            {1, 2, 3, 4, 6,}, // 122(7a)
            {1, 2, 3, 4, 6, 7,}, // 123(7b)
            {1, 2, 3, 4, 5,}, // 124(7c)
            {1, 2, 3, 4, 5, 7,}, // 125(7d)
            {1, 2, 3, 4, 5, 6,}, // 126(7e)
            {1, 2, 3, 4, 5, 6, 7,}, // 127(7f)
            {0,}, // 128(80)
            {0, 7,}, // 129(81)
            {0, 6,}, // 130(82)
            {0, 6, 7,}, // 131(83)
            {0, 5,}, // 132(84)
            {0, 5, 7,}, // 133(85)
            {0, 5, 6,}, // 134(86)
            {0, 5, 6, 7,}, // 135(87)
            {0, 4,}, // 136(88)
            {0, 4, 7,}, // 137(89)
            {0, 4, 6,}, // 138(8a)
            {0, 4, 6, 7,}, // 139(8b)
            {0, 4, 5,}, // 140(8c)
            {0, 4, 5, 7,}, // 141(8d)
            {0, 4, 5, 6,}, // 142(8e)
            {0, 4, 5, 6, 7,}, // 143(8f)
            {0, 3,}, // 144(90)
            {0, 3, 7,}, // 145(91)
            {0, 3, 6,}, // 146(92)
            {0, 3, 6, 7,}, // 147(93)
            {0, 3, 5,}, // 148(94)
            {0, 3, 5, 7,}, // 149(95)
            {0, 3, 5, 6,}, // 150(96)
            {0, 3, 5, 6, 7,}, // 151(97)
            {0, 3, 4,}, // 152(98)
            {0, 3, 4, 7,}, // 153(99)
            {0, 3, 4, 6,}, // 154(9a)
            {0, 3, 4, 6, 7,}, // 155(9b)
            {0, 3, 4, 5,}, // 156(9c)
            {0, 3, 4, 5, 7,}, // 157(9d)
            {0, 3, 4, 5, 6,}, // 158(9e)
            {0, 3, 4, 5, 6, 7,}, // 159(9f)
            {0, 2,}, // 160(a0)
            {0, 2, 7,}, // 161(a1)
            {0, 2, 6,}, // 162(a2)
            {0, 2, 6, 7,}, // 163(a3)
            {0, 2, 5,}, // 164(a4)
            {0, 2, 5, 7,}, // 165(a5)
            {0, 2, 5, 6,}, // 166(a6)
            {0, 2, 5, 6, 7,}, // 167(a7)
            {0, 2, 4,}, // 168(a8)
            {0, 2, 4, 7,}, // 169(a9)
            {0, 2, 4, 6,}, // 170(aa)
            {0, 2, 4, 6, 7,}, // 171(ab)
            {0, 2, 4, 5,}, // 172(ac)
            {0, 2, 4, 5, 7,}, // 173(ad)
            {0, 2, 4, 5, 6,}, // 174(ae)
            {0, 2, 4, 5, 6, 7,}, // 175(af)
            {0, 2, 3,}, // 176(b0)
            {0, 2, 3, 7,}, // 177(b1)
            {0, 2, 3, 6,}, // 178(b2)
            {0, 2, 3, 6, 7,}, // 179(b3)
            {0, 2, 3, 5,}, // 180(b4)
            {0, 2, 3, 5, 7,}, // 181(b5)
            {0, 2, 3, 5, 6,}, // 182(b6)
            {0, 2, 3, 5, 6, 7,}, // 183(b7)
            {0, 2, 3, 4,}, // 184(b8)
            {0, 2, 3, 4, 7,}, // 185(b9)
            {0, 2, 3, 4, 6,}, // 186(ba)
            {0, 2, 3, 4, 6, 7,}, // 187(bb)
            {0, 2, 3, 4, 5,}, // 188(bc)
            {0, 2, 3, 4, 5, 7,}, // 189(bd)
            {0, 2, 3, 4, 5, 6,}, // 190(be)
            {0, 2, 3, 4, 5, 6, 7,}, // 191(bf)
            {0, 1,}, // 192(c0)
            {0, 1, 7,}, // 193(c1)
            {0, 1, 6,}, // 194(c2)
            {0, 1, 6, 7,}, // 195(c3)
            {0, 1, 5,}, // 196(c4)
            {0, 1, 5, 7,}, // 197(c5)
            {0, 1, 5, 6,}, // 198(c6)
            {0, 1, 5, 6, 7,}, // 199(c7)
            {0, 1, 4,}, // 200(c8)
            {0, 1, 4, 7,}, // 201(c9)
            {0, 1, 4, 6,}, // 202(ca)
            {0, 1, 4, 6, 7,}, // 203(cb)
            {0, 1, 4, 5,}, // 204(cc)
            {0, 1, 4, 5, 7,}, // 205(cd)
            {0, 1, 4, 5, 6,}, // 206(ce)
            {0, 1, 4, 5, 6, 7,}, // 207(cf)
            {0, 1, 3,}, // 208(d0)
            {0, 1, 3, 7,}, // 209(d1)
            {0, 1, 3, 6,}, // 210(d2)
            {0, 1, 3, 6, 7,}, // 211(d3)
            {0, 1, 3, 5,}, // 212(d4)
            {0, 1, 3, 5, 7,}, // 213(d5)
            {0, 1, 3, 5, 6,}, // 214(d6)
            {0, 1, 3, 5, 6, 7,}, // 215(d7)
            {0, 1, 3, 4,}, // 216(d8)
            {0, 1, 3, 4, 7,}, // 217(d9)
            {0, 1, 3, 4, 6,}, // 218(da)
            {0, 1, 3, 4, 6, 7,}, // 219(db)
            {0, 1, 3, 4, 5,}, // 220(dc)
            {0, 1, 3, 4, 5, 7,}, // 221(dd)
            {0, 1, 3, 4, 5, 6,}, // 222(de)
            {0, 1, 3, 4, 5, 6, 7,}, // 223(df)
            {0, 1, 2,}, // 224(e0)
            {0, 1, 2, 7,}, // 225(e1)
            {0, 1, 2, 6,}, // 226(e2)
            {0, 1, 2, 6, 7,}, // 227(e3)
            {0, 1, 2, 5,}, // 228(e4)
            {0, 1, 2, 5, 7,}, // 229(e5)
            {0, 1, 2, 5, 6,}, // 230(e6)
            {0, 1, 2, 5, 6, 7,}, // 231(e7)
            {0, 1, 2, 4,}, // 232(e8)
            {0, 1, 2, 4, 7,}, // 233(e9)
            {0, 1, 2, 4, 6,}, // 234(ea)
            {0, 1, 2, 4, 6, 7,}, // 235(eb)
            {0, 1, 2, 4, 5,}, // 236(ec)
            {0, 1, 2, 4, 5, 7,}, // 237(ed)
            {0, 1, 2, 4, 5, 6,}, // 238(ee)
            {0, 1, 2, 4, 5, 6, 7,}, // 239(ef)
            {0, 1, 2, 3,}, // 240(f0)
            {0, 1, 2, 3, 7,}, // 241(f1)
            {0, 1, 2, 3, 6,}, // 242(f2)
            {0, 1, 2, 3, 6, 7,}, // 243(f3)
            {0, 1, 2, 3, 5,}, // 244(f4)
            {0, 1, 2, 3, 5, 7,}, // 245(f5)
            {0, 1, 2, 3, 5, 6,}, // 246(f6)
            {0, 1, 2, 3, 5, 6, 7,}, // 247(f7)
            {0, 1, 2, 3, 4,}, // 248(f8)
            {0, 1, 2, 3, 4, 7,}, // 249(f9)
            {0, 1, 2, 3, 4, 6,}, // 250(fa)
            {0, 1, 2, 3, 4, 6, 7,}, // 251(fb)
            {0, 1, 2, 3, 4, 5,}, // 252(fc)
            {0, 1, 2, 3, 4, 5, 7,}, // 253(fd)
            {0, 1, 2, 3, 4, 5, 6,}, // 254(fe)
            {0, 1, 2, 3, 4, 5, 6, 7,}, // 255(ff)
    };

    private static final long serialVersionUID = -7658605229245494623L;
}

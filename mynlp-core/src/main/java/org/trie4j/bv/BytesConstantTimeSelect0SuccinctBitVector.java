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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

public class BytesConstantTimeSelect0SuccinctBitVector
        implements Externalizable, SuccinctBitVector {
    public BytesConstantTimeSelect0SuccinctBitVector() {
        this(16);
    }

    public BytesConstantTimeSelect0SuccinctBitVector(int initialCapacity) {
        this.bytes = new byte[bytesSize(initialCapacity)];
        this.countCache0 = new int[countCache0Size(initialCapacity)];
        this.bvD = new BytesRank1OnlySuccinctBitVector();
        this.bvR = new BytesRank1OnlySuccinctBitVector();
        this.arS = new int[]{0};
        this.arSSize = 1;
    }

    public BytesConstantTimeSelect0SuccinctBitVector(byte[] bytes, int bitsSize) {
        this.size = bitsSize;
        this.bytes = Arrays.copyOf(bytes, bytesSize(bitsSize));
        this.countCache0 = new int[countCache0Size(bitsSize)];
        this.bvD = new BytesRank1OnlySuccinctBitVector();
        this.bvR = new BytesRank1OnlySuccinctBitVector();
        this.arS = new int[]{0};
        this.arSSize = 1;
        // cache, indexCache(0のCACHE_WIDTH個毎に位置を記憶), node1/2/3pos(0)

        int n = bytes.length;
        for (int i = 0; i < n; i++) {
            // 8bit毎に処理を行う
            int b = bytes[i] & 0xff;
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

            // D, C, Rを構築
            // Dはbytesの0bitに対応。8bitブロック内で最初に現れるものに1、それ以外は0。
            // Cはbytesの8bitブロックに対応。0を含むものに1、含まないものに0。
            // RはCの1bitに対応。最初に現れるものに1、続いて現れるものに0。0は無視。
            prevBsC = currentBsC;
            if (zeroPosInB.length > 0) {
                bvD.append1();
                for (int j = 1; j < zeroPosInB.length; j++) bvD.append0();
                currentBsC = true;
                if (prevBsC) {
                    bvR.append0();
                } else {
                    bvR.append1();
                    addArS();
                }
            } else {
                currentBsC = false;
                arS[arSSize - 1]++;
            }

            size0 += zeroCount;
            if ((i + 1) % (CACHE_WIDTH / 8) == 0) {
                countCache0[i / (CACHE_WIDTH / 8)] = size0;
            }

            if (rest < 8) break;
        }
        countCache0[(size - 1) / CACHE_WIDTH] = size0;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        int n = Math.min(size, 32);
        for (int i = 0; i < n; i++) {
            b.append((bytes[(i / 8)] & (0x80 >> (i % 8))) != 0 ? "1" : "0");
        }
        return b.toString();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int[] getCountCache0() {
        return countCache0;
    }

    @Override
    public boolean get(int pos) {
        return isOne(pos);
    }

    public boolean isZero(int pos) {
        return (bytes[pos / 8] & BITS[pos % 8]) == 0;
    }

    public boolean isOne(int pos) {
        return (bytes[pos / 8] & BITS[pos % 8]) != 0;
    }

    public int size() {
        return this.size;
    }

    public int getSize0() {
        return size0;
    }

    public BytesConstantTimeSelect0SuccinctBitVector(
            byte[] bytes, int size, int size0,
            int node1pos, int node2pos, int node3pos,
            int[] countCache0, SuccinctBitVector bvD,
            SuccinctBitVector bvR, boolean first0bitInBlock,
            boolean prevBsC, boolean currentBsC,
            int[] arS, int arSSize) {
        this.bytes = bytes;
        this.size = size;
        this.size0 = size0;
        this.node1pos = node1pos;
        this.node2pos = node2pos;
        this.node3pos = node3pos;
        this.countCache0 = countCache0;
        this.bvD = bvD;
        this.bvR = bvR;
        this.first0bitInBlock = first0bitInBlock;
        this.prevBsC = prevBsC;
        this.currentBsC = currentBsC;
        this.arS = arS;
        this.arSSize = arSSize;
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

    public SuccinctBitVector getBvD() {
        return bvD;
    }

    public SuccinctBitVector getBvR() {
        return bvR;
    }

    public int[] getArS() {
        return arS;
    }

    public int getArSSize() {
        return arSSize;
    }

    public void trimToSize() {
        bytes = Arrays.copyOf(bytes, bytesSize(size));
        countCache0 = Arrays.copyOf(countCache0, countCache0Size(size));
        bvD.trimToSize();
        bvR.trimToSize();
        arS = Arrays.copyOf(arS, arSSize);
    }

    public void append1() {
        int blockIndex = size / 8;
        int indexInBlock = size % 8;
        int cacheBlockIndex = size / CACHE_WIDTH;
        if (blockIndex >= bytes.length) {
            extend();
        }
        if (size % CACHE_WIDTH == 0 && cacheBlockIndex > 0) {
            countCache0[cacheBlockIndex] = countCache0[cacheBlockIndex - 1];
        }
        bytes[blockIndex] |= BITS[indexInBlock];

        if (indexInBlock == 0) {
            // first bit in block
            prevBsC = currentBsC;
            currentBsC = false;
            arS[arSSize - 1]++;
        }

        size++;
        if (size % 8 == 0) {
            first0bitInBlock = true;
        }
    }

    public void append0() {
        int blockIndex = size / 8;
        int indexInBlock = size % 8;
        int cacheBlockIndex = size / CACHE_WIDTH;
        int indexInCacheBlock = size % CACHE_WIDTH;
        if (blockIndex >= bytes.length) {
            extend();
        }
        if (indexInCacheBlock == 0 && cacheBlockIndex > 0) {
            countCache0[cacheBlockIndex] = countCache0[cacheBlockIndex - 1];
        }
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
        countCache0[cacheBlockIndex]++;

        // D, C, Rを構築
        // Dはbytesの0bitに対応。8bitブロック内で最初に現れるものに1、連続する場合は0。
        // Cはbytesの8bitブロックに対応。0を含むものに1、含まないものに0。
        // RはCの1bitに対応。最初に現れるものに1、続いて現れるものに0。0は無視。
        if (first0bitInBlock) {
            bvD.append1();
            first0bitInBlock = false;
        } else {
            bvD.append0();
        }

        if (indexInBlock == 0) {
            //first bit
            if (bvR.size() == 0 || !currentBsC) {
                bvR.append1();
                addArS();
            } else {
                bvR.append0();
            }
            prevBsC = currentBsC;
            currentBsC = true;
        } else if (!currentBsC) {
            // turn from 0 to 1
            if (bvR.size() == 0 || !prevBsC) {
                bvR.append1();
            } else {
                bvR.append0();
            }
            arS[arSSize - 1]--;
            if (!prevBsC) {
                addArS();
            }
            currentBsC = true;
        }

        size++;
        if (size % 8 == 0) {
            first0bitInBlock = true;
        }
    }

    public void append(boolean bit) {
        if (bit) append1();
        else append0();
    }

    public int rank1(int pos) {
        int cn = pos / CACHE_WIDTH;
        if ((pos + 1) % CACHE_WIDTH == 0) return (cn + 1) * CACHE_WIDTH - countCache0[cn];
        int ret = (cn > 0) ? ret = cn * CACHE_WIDTH - countCache0[cn - 1] : 0;
        int n = pos / 8;
        for (int i = (cn * (CACHE_WIDTH / 8)); i < n; i++) {
            ret += BITCOUNTS1[bytes[i] & 0xff];
        }
        return ret + BITCOUNTS1[bytes[n] & MASKS[pos % 8]];
    }

    public int rank0(int pos) {
        int cn = pos / CACHE_WIDTH;
        if ((pos + 1) % CACHE_WIDTH == 0) return countCache0[cn];
        int ret = (cn > 0) ? ret = countCache0[cn - 1] : 0;
        int n = pos / 8;
        for (int i = (cn * (CACHE_WIDTH / 8)); i < n; i++) {
            ret += BITCOUNTS0[bytes[i] & 0xff];
        }
        return ret + BITCOUNTS0[(bytes[n] | ~MASKS[pos % 8]) & 0xff];
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
        int c = count - 1;
        int ci = bvD.rank1(c) - 1;
        int u = ci + arS[bvR.rank1(ci) - 1];
        if (u != 0) {
            int ui = u * 8;
            int r = rank0(ui - 1);
            return ui + BITPOS0[bytes[u] & 0xff][c - r];
        } else {
            return BITPOS0[bytes[0] & 0xff][c];
        }
    }

    public int select1(int count) {
        for (int i = 0; i < bytes.length; i++) {
            if (i * 8 >= size) return -1;
            int c = BITCOUNTS1[bytes[i] & 0xff];
            if (count <= c) {
                int v = bytes[i] & 0xff;
                for (int j = 0; j < 8; j++) {
                    if (i * 8 + j >= size) return -1;
                    if ((v & 0x80) != 0) {
                        count--;
                        if (count == 0) {
                            return i * 8 + j;
                        }
                    }
                    v <<= 1;
                }
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
        int i = pos / 8;
        int s = pos % 8;
        if (s != 0) {
            for (byte b : BITPOS0[bytes[i] & 0xff]) {
                if (s <= b) return i * 8 + b;
            }
            i++;
        }
        int n = size / 8 + 1;
        for (; i < n; i++) {
            byte[] poss = BITPOS0[bytes[i] & 0xff];
            if (poss.length > 0) {
                return poss[0] + i * 8;
            }
        }
        return -1;
    }

    @Override
    public void readExternal(ObjectInput in)
            throws ClassNotFoundException, IOException {
        size = in.readInt();
        size0 = in.readInt();
        node1pos = in.readInt();
        node2pos = in.readInt();
        node3pos = in.readInt();
        int vectorSize = in.readInt();
        bytes = new byte[vectorSize];
        in.readFully(bytes, 0, vectorSize);
        countCache0 = (int[]) in.readObject();
        bvD = (SuccinctBitVector) in.readObject();
        first0bitInBlock = in.readBoolean();
        bvR = (SuccinctBitVector) in.readObject();
        arS = (int[]) in.readObject();
        arSSize = in.readInt();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(size);
        out.writeInt(size0);
        out.writeInt(node1pos);
        out.writeInt(node2pos);
        out.writeInt(node3pos);
        trimToSize();
        out.writeInt(bytes.length);
        out.write(bytes);
        out.writeObject(countCache0);
        out.writeObject(bvD);
        out.writeBoolean(first0bitInBlock);
        out.writeObject(bvR);
        out.writeObject(arS);
        out.writeInt(arSSize);
    }

    private void extend() {
        int vectorSize = (int) (bytes.length * 1.2) + 1;
        bytes = Arrays.copyOf(bytes, vectorSize);
        int blockSize = CACHE_WIDTH / 8;
        int size = vectorSize / blockSize + (((vectorSize % blockSize) != 0) ? 1 : 0);
        countCache0 = Arrays.copyOf(countCache0, size);
    }

    private void addArS() {
        if (arSSize == arS.length) {
            arS = Arrays.copyOf(arS, (int) (arSSize * 1.2) + 1);
        }
        if (arSSize > 0) {
            arS[arSSize] = arS[arSSize - 1];
        }
        arSSize++;
    }

    private static int bytesSize(int bitSize) {
        return (bitSize - 1) / BITS_IN_BYTE + 1;
    }

    private static int countCache0Size(int bitSize) {
        return (bitSize - 1) / CACHE_WIDTH + 1;
    }

    private static final int BITS_IN_BYTE = 8;
    private static final int CACHE_WIDTH = 64;
    private byte[] bytes;
    private int size;
    private int size0;
    private int node1pos = -1;
    private int node2pos = -1;
    private int node3pos = -1;
    private int[] countCache0;
    private SuccinctBitVector bvD;
    private boolean first0bitInBlock = true;
    private boolean prevBsC;
    private boolean currentBsC;
    private SuccinctBitVector bvR;
    private int[] arS;
    private int arSSize;

    private static final int[] MASKS = {
            0x80, 0xc0, 0xe0, 0xf0
            , 0xf8, 0xfc, 0xfe, 0xff
    };
    private static final byte[] BITS = {
            (byte) 0x80, (byte) 0x40, (byte) 0x20, (byte) 0x10
            , (byte) 0x08, (byte) 0x04, (byte) 0x02, (byte) 0x01
    };
    private static final byte[] BITCOUNTS1 = {
            0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4,
            1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
            1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
            1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
            3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
            4, 5, 5, 6, 5, 6, 6, 7, 5, 6, 6, 7, 6, 7, 7, 8
    };
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

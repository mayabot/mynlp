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

package fasttext.productquantizer;

import com.google.common.base.Preconditions;

import java.util.Random;

import static fasttext.Utils.iota;
import static fasttext.Utils.shuffle;

public class CentroidTable {
    public static final int niter_ = 25;

    private final Random random = new Random(1234L);

    final float eps_ = 1e-7f;

    float[] centroidData;

    /**
     * dim/dsub 有几个子空间
     */
    int nsubq;

    /**
     * dsub 子空间的维度
     */
    int dsub;

    /**
     * 最后一个子空间的维度
     */
    int lastdsub;

    int ksub;

    /**
     * 质心
     *
     * @param dim  原始数据的维度
     * @param ksub 每个区，质心的数量，一般为2的次方。比如256
     */
    public CentroidTable(int dim, int ksub, int dsub) {
        centroidData = new float[dim * ksub];

        this.dsub = dsub;
        nsubq = dim / dsub;
        this.ksub = ksub;

        lastdsub = dim % dsub;
        if (lastdsub == 0) {
            lastdsub = dsub;
        } else {
            nsubq++;
        }
    }

    public MCentroid get(int m) {
        return new MCentroid(m);
    }

    public void kmeans(int m, float[] xslice, int np, int d) {
        MCentroid mcen = this.get(m);
        Preconditions.checkArgument(d == mcen.d);

        mcen.kmeans(xslice, np);
    }


//    private void kmeans(int c, int n, int d) {
//        int[] perm = new int[n];
//        iota(perm);
//        shuffle(perm);
//
//        for (int i = 0; i < ksub_; i++) {
//            // memcpy (&c[i * d], x + perm[i] * d, d * sizeof(real));
//            System.arraycopy(centroids_,i*d,data,perm[i]*d,d);
//        }
//        short[] codes = new short[n];
//        for (int i = 0; i < niter_; i++) {
//            //Estep(x, c, codes.data(), d, n);
//
//        }
//    }


    /**
     * 第M个区间的质心视图
     */
    class MCentroid {
        int m = 0;
        private int start;
        int d;

        public MCentroid(int m) {
            this.m = m;
            this.start = m * ksub * dsub;
            if (m == nsubq - 1) {
                d = lastdsub;
            } else {
                d = dsub;
            }
        }

        public void kmeans(float[] xslice, int n) {
            int[] perm = new int[n];
            iota(perm);
            shuffle(perm,random);

            //随机选取256个点，作为初始化质心
            for (int i = 0; i < ksub; i++) {
                // memcpy (&c[i * d], x + perm[i] * d, d * sizeof(real));
                System.arraycopy(xslice, perm[i] * d, centroidData, start + i * d, d);
            }

            short[] codes = new short[n];
            for (int q = 0; q < niter_; q++) {

                //记住每个向量和哪些之心最近
                for (int i = 0; i < n; i++) {
                    codes[i] = assignCentroid(xslice, i * d);
                }

                //每个质心,坐标为和之有关的均值
                int[] nelts = new int[ksub];

                //质心=0
                for (int i = start; i < ksub * d; i++) {
                    centroidData[i] = 0;
                }

                //每个质心求和
                for (int i = 0; i < n; i++) {
                    int k = codes[i];

                    int t = 0;
                    for (int j = start + k * d, max = start + k * d + d; j < max; j++) {
                        centroidData[j] += xslice[t++];
                    }
                    //求和
                    nelts[k]++;
                }

                //平均数
                int c = start;
                for (int k = 0; k < ksub; k++) {
                    int z = nelts[k];
                    if (z != 0) {
                        for (int j = 0; j < d; j++) {
                            centroidData[c++] /= z;
                        }
                    } else {
                        c += d;
                    }
                }

                //如果质心没有绑定到最近的,随机分配一个
                for (int k = 0; k < ksub; k++) {
                    if (nelts[k] == 0) {
                        int m = 0;
                        while (random.nextFloat() * (n - ksub) >= nelts[m] - 1) {
                            m = (m + 1) % ksub;
                        }
                        System.arraycopy(centroidData, start + m * d, centroidData, start + k * d, d);
                        for (int j = 0; j < d; j++) {
                            int sign = (j % 2) * 2 - 1;
                            centroidData[start + k * d + j] += sign * eps_;
                            centroidData[start + m * d + j] -= sign * eps_;
                        }
                        nelts[k] = nelts[m] / 2;
                        nelts[m] -= nelts[k];
                    }
                }
            }
        }

        /**
         * 返回地i个质心在数据data中的位置
         *
         * @param i
         * @return
         */
        public final int getCentroid(int i) {
            return start + i * d;
        }

        /**
         * 计算出给定的向量和这些质心之间，那个最近
         *
         * @param data
         * @param offset
         * @return
         */
        public short assignCentroid(float[] data, int offset) {
            float dis = distL2(data, offset, 0);
            short code = 0;
            for (int j = 1; j < ksub; j++) {
                float disij = distL2(data, offset, j);
                if (disij < dis) {
                    code = (short) j;
                    dis = disij;
                }
            }
            return code;
        }


        public final float distL2(float[] x, int xoffset, int row) {
            float dist = 0;
            for (int i = xoffset, j = getCentroid(row); i < d + xoffset; i++, j++) {
                float tmp = x[i] - centroidData[j];
                dist += tmp * tmp;
            }
            return dist;
        }

    }



}

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

package fasttext.pq;

import fasttext.matrix.Vector;

import java.util.Random;

import static fasttext.utils.Utils.iota;
import static fasttext.utils.Utils.shuffle;

public class ProductQuantizer {

    public static final int nbits_ = 8;
    public static final int ksub_ = 1 << nbits_;
    public static final int max_points_per_cluster_ = 256;
    public static final int max_points_ = max_points_per_cluster_ * ksub_;

    /**
     * 原始向量维度
     */
    int dim_;

    /**
     * dim/dsub 有几个子空间
     */
    int nsubq_;

    /**
     * dsub 子空间的维度
     */
    int dsub_;

    /**
     * 最后一个子空间的维度
     */
    int lastdsub_;

    private final Random random = new Random(1234L);

    CentroidTable centroidTable;

    /**
     * @param dim
     * @param dsub 子空间维度
     */
    public ProductQuantizer(int dim, int dsub) {
        dim_ = dim;
        dsub_ = dsub;
        nsubq_ = dim / dsub;
        //centroids_ = new float[dim * ksub_];

        lastdsub_ = dim_ % dsub;
        if (lastdsub_ == 0) {
            lastdsub_ = dsub_;
        } else {
            nsubq_++;
        }
    }

    public void train(int n, float[] data) {
        int np = Math.min(n, max_points_);
        centroidTable = new CentroidTable(dim_, ksub_, dsub_);


        int[] perm = new int[n];
        iota(perm);

        int d = dsub_;
        float[] xslice = new float[np * dsub_];
        for (int m = 0; m < nsubq_; m++) {
            if (m == nsubq_ - 1) {
                d = lastdsub_;
            }
            if (np != n) {
                shuffle(perm, random);
            }
            for (int j = 0; j < np; j++) {
                int from = perm[j] * dim_ + m * dsub_;
                System.arraycopy(data, from, xslice, j * d, d);
            }
            centroidTable.kmeans(m, xslice, np, d);
        }
    }

    /**
     * @param data
     * @param codes
     * @param n     这个n是原始数据的行数
     */
    public void compute_codes(float[] data, short[] codes, int n) {
        for (int i = 0; i < n; i++) {
            int x = i * dim_;
            int c = i * nsubq_;

            for (int m = 0; m < nsubq_; m++) {
                CentroidTable.MCentroid mCentroid = centroidTable.get(m);
                 int k = mCentroid.assignCentroid(data,x + m*dsub_);
                 codes[c+m] = (short) k;
            }
        }

    }

    public int get_centroids(int m, short i) {
        if (m == nsubq_ - 1) {
            return  m * ksub_ * dsub_ + i * lastdsub_;}
        return (m * ksub_ + i) * dsub_;
    }

    /**
     *
     * @param x
     * @param codes_
     * @param t 原始数据的行数
     * @param alpha
     */
    // TODO check
    public void addCode(Vector x, short[] codes_, int t, float alpha) {
        int d = dsub_;
        int codeOffset = nsubq_ * t;
        float[] centroidData = centroidTable.centroidData;
        for (int m = 0; m < nsubq_; m++) {
            int c = get_centroids(m,codes_[codeOffset+m]);
            if(m == nsubq_ - 1){
                d = lastdsub_;}
            for (int n = 0; n < d; n++) {
                x.add(m * dsub_ + n, alpha * centroidData[c + n]);
            }
        }
    }


    //    void ProductQuantizer::addcode(Vector& x, const uint8_t* codes,
//                                   int32_t t, real alpha) const {
//        auto d = dsub_;
//  const uint8_t* code = codes + nsubq_ * t;
//        for (auto m = 0; m < nsubq_; m++) {
//    const real* c = get_centroids(m, code[m]);
//            if (m == nsubq_ - 1) {d = lastdsub_;}
//            for(auto n = 0; n < d; n++) {
//                x[m * dsub_ + n] += alpha * c[n];
//            }
//        }
//    }

    // TODO check
    public float mulCode(Vector x, short[] codes_, int t, float alpha) {
        float res = 0f;
        int d = dsub_;
        int codeOffset = nsubq_ * t;
        float[] centroidData = centroidTable.centroidData;
        for (int m = 0; m < nsubq_; m++) {
            int c = get_centroids(m,codes_[codeOffset+m]);
            if(m == nsubq_ - 1){
                d = lastdsub_;}
            for (int n = 0; n < d; n++) {
                res += x.get(m * dsub_ + n) * centroidData[c + n];
            }
        }
        return res * alpha;
    }
//    real ProductQuantizer::mulcode(const Vector& x, const uint8_t* codes,
//                                   int32_t t, real alpha) const {
//        real res = 0.0;
//        auto d = dsub_;
//  const uint8_t* code = codes + nsubq_ * t;
//        for (auto m = 0; m < nsubq_; m++) {
//    const real* c = get_centroids(m, code[m]);
//            if (m == nsubq_ - 1) {d = lastdsub_;}
//            for(auto n = 0; n < d; n++) {
//                res += x[m * dsub_ + n] * c[n];
//            }
//        }
//        return res * alpha;
//    }
//

//

}

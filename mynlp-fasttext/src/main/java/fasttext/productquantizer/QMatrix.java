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
import fasttext.matrix.Matrix;
import fasttext.matrix.Vector;

import java.io.InputStream;
import java.io.OutputStream;

public class QMatrix {
    ProductQuantizer pq_;
    ProductQuantizer npq_;

    short[] codes_;
    short[] norm_codes_;

    boolean qnorm_;

    int m_;
    int n_;

    int codesize_;

    public QMatrix() {
        qnorm_ = false;
        m_ = 0;
        n_ = 0;
        codesize_ = 0;
    }

    public QMatrix(Matrix mat, int dsub, boolean qnorm) {
        qnorm_ = qnorm;
        m_ = mat.rows();
        n_ = mat.cols();
        codesize_ = m_ * ((n_ + dsub - 1) / dsub);

        codes_ = new short[codesize_];
        pq_ = new ProductQuantizer(n_, dsub);

        if (qnorm) {
            norm_codes_ = new short[m_];
            npq_ = new ProductQuantizer(1, 1);
        }
    }

    private void quantize(Matrix matrix) {
        Preconditions.checkArgument(m_ == matrix.rows());
        Preconditions.checkArgument(n_ == matrix.cols());

        Matrix temp = matrix;
        if (qnorm_) {
            float[] norms = new float[m_];
            temp.l2NormRow(norms);
            temp.divideRow(norms);
            quantizeNorm(norms);
        }

        pq_.train(m_, temp.getData());
        pq_.compute_codes(temp.getData(), codes_, m_);
    }

    public void addToVector(Vector x, int t) {
        float norm = 1.0f;
        if (qnorm_) {
            norm = npq_.centroidTable.centroidData[
                    npq_.get_centroids(0, norm_codes_[t])];
        }
        pq_.addCode(x, codes_, t, norm);
    }

    public float dotRow(Vector vec, int i) {
        Preconditions.checkArgument(i >= 0 && i < m_ && vec.size() == n_);

        float norm = 1f;
        if (qnorm_) {
            norm = npq_.centroidTable.centroidData[
                    npq_.get_centroids(0, norm_codes_[i])];
        }

        return pq_.mulCode(vec, codes_, i, norm);
    }

    public int getM() {
        return m_;
    }

    public int getN() {
        return n_;
    }

    void quantizeNorm(float[] norms) {
        assert (qnorm_);
        assert (norms.length == m_);

        npq_.train(m_, norms);
        npq_.compute_codes(norms, norm_codes_, m_);
    }

    public void save(OutputStream out) {

    }

    public void load(InputStream in) {

    }

}

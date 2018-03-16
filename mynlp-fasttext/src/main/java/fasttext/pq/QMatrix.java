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

import com.google.common.base.Preconditions;
import fasttext.matrix.Matrix;
import fasttext.matrix.Vector;
import fasttext.utils.CLangDataInputStream;
import fasttext.utils.CLangDataOutputStream;

import java.io.IOException;

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

        if (qnorm_) {
            float[] norms = new float[m_];
            matrix.l2NormRow(norms);
            matrix.divideRow(norms);
            quantizeNorm(norms);
        }

        pq_.train(m_, matrix.getData());
        pq_.compute_codes(matrix.getData(), codes_, m_);
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

    public void save(CLangDataOutputStream out) throws IOException{
        out.writeBoolean(qnorm_);
        out.writeLong(m_);
        out.writeLong(n_);
        out.writeInt(codesize_);

        out.writeShortArray(codes_);

        pq_.save(out);
        if (qnorm_) {
            out.writeShortArray(norm_codes_);

            npq_.save(out);
        }

    }

    public void load(CLangDataInputStream in) throws IOException{
        qnorm_ = in.readBoolean();
        m_ = (int) in.readLong();
        n_ = (int) in.readLong();
        codesize_ = in.readInt();

        codes_ = new short[codesize_];
        in.readShortArray(codes_);

        pq_ = new ProductQuantizer();
        pq_.load(in);

        if (qnorm_) {
            norm_codes_ = new short[m_];
            in.readShortArray(norm_codes_);

            npq_ = new ProductQuantizer();
            npq_.load(in);
        }
    }

}

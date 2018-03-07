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

package fasttext.matrix;


import com.google.common.base.Preconditions;
import fasttext.pq.QMatrix;

import static com.google.common.base.Preconditions.checkArgument;

public class Vector {

    private float[] data;

    private int start = 0;
    private int end = 0;
    private int length = 0;

    public Vector() {
        data = new float[0];
    }

    public Vector(float[] data, int start, int end, int length) {
        this.data = data;
        this.start = start;
        this.end = end;
        this.length = length;
    }

    public Vector(int dim) {
        data = new float[dim];
        start = 0;
        end = start + dim;
        length = data.length;
        zero();
    }

    public float get(int i) {
        return data[i];
    }

    public void set(int i, float value) {
        data[i] = value;
    }

    public void add(int i, float v) {
        data[i] += v;
    }
    public Vector copy(){
        Vector r = new Vector(length);
        int p = 0;
        for (int i = start ; i < end; i++) {
            r.data[p++] = data[i];
        }
        return r;
    }

    public void zero() {
        fill(0);
    }

    public void fill(float v) {
        for (int i = start; i < end; i++) {
            data[i] = v;
        }
    }

    public void check(){
        for (int i = 0; i < data.length; i++) {
            checkArgument(!Float.isNaN(data[i]));
            checkArgument(!Float.isInfinite(data[i]));
        }
    }


    /**
     * 向量点乘。内积
     * @param other
     * @return
     */
    public float prod(Vector other) {
        checkArgument(length() == other.length());

        float result = 0f;

        int p = start;
        int q = other.start;

        for (int i = 0; i < length; i++) {
            result += data[p++] * other.data[q++];
        }

        return result;
    }

    public void addRow(Matrix m, int r, float s) {
        add(m.rowView(r), s);
    }

    public void addRow(Matrix m, int r) {
        add(m.rowView(r));
    }

    public void addRow(QMatrix m, int r) {
        Preconditions.checkArgument(r >= 0);
        m.addToVector(this, r);
    }


    public void add(Vector other) {
        add(other, 1);
    }

    public void add(Vector other, float s) {
        checkArgument(length() == other.length());

        int p = start;
        int q = other.start;

            for (int i = 0; i < length; i++) {
                data[p++] += other.data[q++] * s;
            }
    }

    public Vector addTo(Vector other) {
        checkArgument(length() == other.length());
        Vector r = new Vector(length());

        int p = start;
        int q = other.start;
        for (int i = 0; i < length; i++) {
            r.data[i] = data[p++] + other.data[q++];
        }

        return r;
    }


    public void sub(Vector other, float s) {
        checkArgument(length() == other.length());

        int p = start;
        int q = other.start;

        for (int i = 0; i < length; i++) {
            data[p++] -= other.data[q++] * s;
        }
    }


    public void mul(float s){
        for (int i = start; i < end; i++) {
            data[i] *= s;
        }
    }


    public void mul(final Matrix a, final Vector vec) {

        checkArgument(a.rows() == length);
        checkArgument(a.cols() == vec.length);

        int m_ = a.rows();
        for (int i = 0; i < m_; i++) {
            float x = 0f;
            for (int j = 0; j < a.cols(); j++) {
                x += a.get(i, j) * vec.data[j];
            }
            data[i] = x;
        }
    }

    public void div(float s) {
        //TODO yao jiancha
        for (int i = start; i < end; i++) {
            data[i] /= s;
        }
    }

    public final int length() {
        return length;
    }


    /**
     * sqrt (sum (v [i] * v [i]))
     *
     * @return
     */
    public float norm() {
        double sum = 0;
        for (int i = start; i < end; i++) {
            float x = data[i];
            sum += x * x;
        }
        return (float) Math.sqrt(sum);
    }

    public int size() {
        return length;
    }

    public final static float dot(Vector a, Vector b) {
        return a.prod(b);
    }

    public final static float cosine(Vector a, Vector b) {
        float normA = dot(a, a), normB = dot(b, b);
        if (normA == 0.0 || normB == 0.0) {
            return 0.0f;
        }
        return (float) (dot(a, b) / Math.sqrt(normA * normB));
    }

    public String toString() {

        if (length == 0)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        int iMax = length -1;
        for (int i = start; ; i++) {
            b.append(data[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

}

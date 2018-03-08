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
import com.google.common.base.Strings;
import fasttext.utils.CLangDataInputStream;
import fasttext.utils.CLangDataOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;

public class Matrix {


    private int rows;
    private int cols;

    //按行存储
    private float[] data;
    private int length;

    public Matrix() {
        this(0, 0);
    }

    public Matrix(int row, int col) {
        this.rows = row;
        this.cols = col;
        length = row * col;
        data = new float[length];
    }

    /**
     * 均值为0
     *
     * @param sd 标准差
     */
    public void gaussRandomInit(double sd) {
        for (int i = 0; i < length; i++) {
            data[i] = (float) (rnd.nextGaussian() * sd);
        }
    }

    public void uniform(float a) {
        float upper = a;
        float lower = -a;
        for (int i = 0; i < length; i++) {
            data[i] = (rnd.nextFloat() * (upper - lower)) + lower;
        }
    }

    public float get(int i, int j) {
        return data[index(i, j)];
    }

    public void set(int i, int j, float v) {
        data[index(i, j)] = v;
    }

    private final int index(int i, int j) {
        return i * cols + j;
    }

    public void fill(float v) {
        for (int i = 0; i < length; i++) {
            data[i] = v;
        }
    }

    public FloatRowView rowView(int r) {
        Preconditions.checkArgument(r < rows);
        return new FloatRowView(this, r);
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }


    public void addRow(final Vector vec, int i, float a) {
        rowView(i).add(vec, a);
    }

    public float dotRow(final Vector vec, int i) {
        return rowView(i).prod(vec);
    }

    public void divideRow(float[] denoms) {
        divideRow(denoms, 0, -1);
    }

    public void divideRow(float[] denoms, int ib, int ie) {
        if (ie == -1) {
            ie = rows;
        }
        assert (ie <= denoms.length);

        for (int i = ib; i < ie; i++) {
            float n = denoms[i - ib];
            if (n != 0) {
                int s = i * cols;
                for (int j = 0; j < cols; j++) {
                    //i* cols + j
                    data[s + j] /= n;
                }
            }
        }
    }

    public float l2NormRow(int i) {
        float norm = 0.0f;
        for (int j = 0; j < cols; j++) {
            float v = get(i, j);
            norm += v * v;
        }
        if (Float.isNaN(norm)) {
            throw new RuntimeException("Encountered NaN.");
        }
        return (float) Math.sqrt(norm);
    }

    public void l2NormRow(float[] norms) {
        assert (norms.length == rows);
        for (int i = 0; i < rows; i++) {
            norms[i] = l2NormRow(i);
        }
    }


    @Override
    public String toString() {

        int iMax = data.length - 1;
        if (iMax == -1) {
            return "[]";
        }

        StringBuilder b = new StringBuilder();
        b.append(Strings.repeat("-", cols * 12));
        b.append("\n");
        int count = 0;
        for (int i = 0; ; i++) {
            count++;
            b.append(data[i]);

            if (i == iMax) {
                return b.append('\n').append(Strings.repeat("-", cols * 12)).append('\n').toString();
            }
            if (count % cols == 0) {
                b.append('\n');
            } else {
                b.append("\t");
            }
        }
    }


    final private Random rnd = new Random();

    public float[] getData() {
        return data;
    }

    public void zero() {
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
    }

    public void load(CLangDataInputStream in) throws IOException{
        rows = (int) in.readLong();
        cols = (int) in.readLong();
        length = rows*cols;
        data = new float[length];

        in.readFloatArray(data);
    }

    public void save(CLangDataOutputStream out) throws IOException{
        out.writeLong(rows);
        out.writeLong(cols);

        out.writeFloatArray(data);
    }

}




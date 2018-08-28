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
package com.mayabot.nlp.segment.crf;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * 特征函数，其实是tag.size个特征函数的集合
 *
 * @author hankcs
 */
public class FeatureFunction implements Externalizable {

    public static int serialVersionUID = 1;

    /**
     * 环境参数
     */
    char[] o;


    /**
     * 权值，按照index对应于tag的id
     */
    double[] w;

    public FeatureFunction(char[] o, int tagSize) {
        this.o = o;
        w = new double[tagSize];
    }

    public FeatureFunction() {
    }


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(new String(o));

        out.writeInt(w.length);
        for (int i = 0; i < w.length; i++) {
            out.writeDouble(w[i]);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        o = in.readUTF().toCharArray();

        w = new double[in.readInt()];

        for (int i = 0; i < w.length; i++) {
            w[i] = in.readDouble();
        }
    }
}

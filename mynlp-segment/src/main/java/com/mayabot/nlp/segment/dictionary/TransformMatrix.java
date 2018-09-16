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

package com.mayabot.nlp.segment.dictionary;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.ByteSource;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.segment.common.QuickStringDoubleTable;
import com.mayabot.nlp.segment.common.QuickStringIntTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 概率转移矩阵
 * 这是一个通用的数据结构
 * <p>
 * <p>
 * 放弃了HanLp中使用Enum的做法，直接使用了string。
 * <p>
 * @author jimichan
 */
public class TransformMatrix {

    /**
     * 储存转移矩阵
     */
    private QuickStringIntTable matrix;

    /**
     * 储存每个标签出现的次数
     */
    private ImmutableMap<String, Long> total;

    /**
     * 所有标签出现的总次数
     */
    private long totalFrequency;

    // HMM的五元组
    /**
     * 隐状态
     */
    public ImmutableList<String> states;
    /**
     * 初始概率
     */
    public ImmutableMap<String, Double> start_probability;
    /**
     * 转移概率
     */
    public QuickStringDoubleTable transititon_probability;


    public double getTP(String a, String b) {
        double d = transititon_probability.get(a, b);
        if (d == Double.MIN_VALUE) {
            return 0;
        }

        return d;
    }

    /**
     * 获取转移频次
     *
     * @param from
     * @param to
     * @return
     */

    public int getFrequency(String from, String to) {
        int v = matrix.get(from, to);
        if (v == Integer.MIN_VALUE) {
            return 0;
        }
        return v;
    }



    public boolean load(ByteSource source) throws IOException {
        try (InputStream inputStream = source.openBufferedStream()) {
            return load(inputStream);
        }
    }

    public boolean load(NlpResource resource) throws IOException {

        try (InputStream inputStream = resource.openInputStream()) {
            return load(inputStream);
        }

    }

    public boolean load(InputStream in) throws IOException {

        Splitter splitter = Splitter.on(',').trimResults();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in,
                StandardCharsets.UTF_8))) {
            String firstLine = br.readLine();
            List<String> lablist = splitter.splitToList(firstLine);


            // 为了制表方便，第一个label是空白，所以要抹掉它
            // //之后就描述了矩阵
            ArrayTable<String, String, Integer> matrix = ArrayTable.create(lablist.subList(1, lablist.size()),
                    lablist.subList(1, lablist.size()));
            String line;
            while ((line = br.readLine()) != null) {
                List<String> paramArray = splitter.splitToList(line);
                String row_lable = paramArray.get(0);
                Map<String, Integer> row = matrix.row(row_lable);
                for (int i = 1; i < paramArray.size(); i++) {
                    row.put(lablist.get(i), Integer.parseInt(paramArray.get(i)));
                }
            }

            this.matrix = new QuickStringIntTable(matrix);

            tongji(matrix);


        }




        return true;
    }

    private void tongji(ArrayTable<String, String, Integer> matrix) {

        // 需要统计一下每个标签出现的次数
        HashMap<String, Long> _total = Maps.newHashMap();
        for (String label : matrix.rowKeyList()) {
            long v1 = 0, v2 = 0;
            for (int x : matrix.row(label).values()) {
                v1 += x;
            }
            for (int x : matrix.column(label).values()) {
                v2 += x;
            }
            _total.put(label, v1 + v2 - matrix.get(label, label));
        }
        this.total = ImmutableMap.copyOf(_total);


        // 总计频率
        long _tf = 0;
        for (long x : this.total.values()) {
            _tf += x;
        }
        totalFrequency = _tf;

        // 下面计算HMM四元组

        // 状态标签数组
        states = matrix.rowKeyList();
        // 初始概率
        HashMap<String, Double> _start_probability = Maps.newHashMap();
        for (String label : states) {
            double frequency = total.get(label) + 1e-8;
            _start_probability
                    .put(label, -Math.log(frequency / totalFrequency));
        }
        this.start_probability = ImmutableMap.copyOf(_start_probability);

        ArrayTable<String, String, Double> transititon_probability = ArrayTable.create(matrix.rowKeyList(),
                matrix.columnKeyList());


        for (String from : states) {
            for (String to : states) {
                double frequency = matrix.get(from, to) + 1e-8;
                transititon_probability.put(from, to,
                        -Math.log(frequency / total.get(from)));
            }
        }

        this.transititon_probability = new QuickStringDoubleTable(transititon_probability);
    }

    /**
     * 获取e的总频次
     *
     * @param from
     * @return
     */
    public long getTotalFrequency(String from) {
        Long v = total.get(from);
        if (v == null) {
            // FIXME 这里会不会有问题
            return 0;
        }
        return v.longValue();
    }

    /**
     * 获取所有标签的总频次
     *
     * @return
     */
    public long getTotalFrequency() {
        return totalFrequency;
    }
}

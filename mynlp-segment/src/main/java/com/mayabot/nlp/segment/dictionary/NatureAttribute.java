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

import com.google.common.collect.ImmutableMap;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 核心词典中的词属性.
 * 只读的类，里面的数据不可变
 * 表达了词的词性特征
 *
 * @author jimichan
 */
public final class NatureAttribute {

    public static final String version = "v1";

    private ImmutableMap<Nature, Integer> map = ImmutableMap.of();

    private int totalFrequency;

    private Map.Entry<Nature, Integer> one;

    private NatureAttribute() {

    }


    public int size() {
        return map.size();
    }

    public Map.Entry<Nature, Integer> one() {
        if (one == null) {
            one = map.entrySet().iterator().next();
        }
        return one;
    }

    public ImmutableMap<Nature, Integer> getMap() {
        return map;
    }


    public boolean hasNature(Nature xnature) {
        return map.containsKey(xnature);
    }

    /**
     * 使用单个词性，默认词频1000构造
     *
     * @param nature
     */
    public static NatureAttribute create1000(Nature nature) {
        return create(nature, 1000);
    }

    /**
     * 获取词性的词频
     *
     * @param nature 词性
     * @return 词频
     */
    public int getNatureFrequency(final Nature nature) {
        return map.getOrDefault(nature, 0);
    }


    private void computeTotal() {
        int c = 0;
        for (Integer i : map.values()) {
            c += i;
        }
        this.totalFrequency = c;
    }


    @Override
    public String toString() {
        return map.toString();
    }

    public int getTotalFrequency() {
        return totalFrequency;
    }


    public static NatureAttribute create() {
        return new NatureAttribute();
    }

    public static NatureAttribute create(Nature nature, int frequency) {
        NatureAttribute natureAttribute = new NatureAttribute();
        natureAttribute.map = ImmutableMap.of(nature, frequency);
        natureAttribute.totalFrequency = frequency;
        return natureAttribute;
    }

    public static NatureAttribute create(String... param) {
        NatureAttribute natureAttribute = new NatureAttribute();
        int natureCount = param.length / 2;
        Map<Nature, Integer> map = new HashMap<Nature, Integer>();
//        ImmutableMap.Builder<Nature, Integer> builder = ImmutableMap.builder();
        for (int i = 0; i < natureCount; ++i) {
            map.put(Nature.parse(param[1 + 2 * i]), Integer.parseInt(param[2 + 2 * i]));
        }
        natureAttribute.map = ImmutableMap.copyOf(map);
        natureAttribute.computeTotal();
        return natureAttribute;
    }


    private static int[] quickJson(String json) {
        if ("[]".equals(json)) {
            return empty;
        }
        String[] split = json.split(",");
        int[] result = new int[split.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Integer.parseInt(split[i]);
        }
        return result;
    }

    static Nature[] natures = Nature.values();
    public static NatureAttribute read(DataInput in) {
        try {
            NatureAttribute attribute = new NatureAttribute();

            attribute.totalFrequency = in.readInt();

            String json = in.readUTF();

            int[] array = quickJson(json);

            if (array.length == 0) {
                attribute.map = ImmutableMap.of();
            } else if (array.length == 2) {
                attribute.map = ImmutableMap.of(natures[array[0]], Integer.valueOf(array[1]));
            } else {
                ImmutableMap.Builder<Nature, Integer> builder = ImmutableMap.builder();
                for (int i = 0; i < array.length; i += 2) {
                    int ord = array[i];
                    int freq = array[i + 1];
                    builder.put(natures[ord], Integer.valueOf(freq));
                }

                attribute.map = builder.build();
            }
            return attribute;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public static void write(NatureAttribute a, DataOutput out) {
        try {
            out.writeInt(a.totalFrequency);

            int size = a.map.size();

            if (size == 0) {
                out.writeUTF("[]");
            } else if (size == 1) {
                Map.Entry<Nature, Integer> x = a.map.entrySet().iterator().next();
                out.writeUTF(x.getKey().ordinal() + "," + x.getValue());
            } else {
                StringBuilder line = new StringBuilder();
                for (Map.Entry<Nature, Integer> x : a.map.entrySet()) {
                    if (line.length() > 0) {
                        line.append(",");
                    }
                    line.append(x.getKey().ordinal() + "," + x.getValue());
                }
                out.writeUTF(line.toString());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    //19,17,1,2
    static int[] empty = new int[0];


}

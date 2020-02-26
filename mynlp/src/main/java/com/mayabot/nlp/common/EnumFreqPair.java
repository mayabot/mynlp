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
package com.mayabot.nlp.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 对标签-频次的封装
 * 参考HanLP https://github.com/hankcs/HanLP 中的实现
 * 做了一些改动，labelMap使用ImmutableMap。序列化采用FastJson对信息打包
 *
 * @author hankcs
 * @author jimichan
 */
public class EnumFreqPair<E extends Enum> {

    private ImmutableMap<E, Integer> labelMap = ImmutableMap.of();

    public EnumFreqPair() {
    }

    /**
     * 优化
     * <pre>
     * null -> "NULL"
     * a:1  -> a:1
     * {a:1,b:2}  -> a:1,b:2
     * </pre>
     *
     * @param out
     */
    public void writeItem(DataOutput out) {
        try {
            int size = labelMap.size();

            switch (size) {
                case 0:
                    out.writeUTF("NULL");
                    break;
                case 1:
                    Map.Entry<E, Integer> next = labelMap.entrySet().iterator().next();
                    String name = next.getKey().name();
                    Integer f = next.getValue();
                    out.writeUTF(name + "," + f);
                    break;
                default:
                    StringBuilder sb = new StringBuilder();

                    int count = 0;
                    for (Map.Entry<E, Integer> e : labelMap.entrySet()) {
                        count++;

                        sb.append(e.getKey().name())
                                .append(",").append(e.getValue());

                        if (count != size) {
                            sb.append(",");
                        }
                    }
                    out.writeUTF(sb.toString());
                    break;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void readItem(DataInput in, Function<String, E> function) {
        try {
            String json = in.readUTF();

            if ("NULL".equals(json)) {
                this.labelMap = ImmutableMap.of();
            } else {
                ImmutableMap.Builder<E, Integer> builder = ImmutableMap.builder();

                String[] split = json.split(",");

                for (int i = 0; i < split.length; i += 2) {
                    builder.put(function.apply(split[i]), Integer.parseInt(split[i + 1]));
                }
                this.labelMap = builder.build();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public EnumFreqPair(E... labels) {
        this();
        ImmutableMap.Builder<E, Integer> builder = ImmutableMap.builder();
        for (E label : labels) {
            builder.put(label, 1);
        }
        this.labelMap = builder.build();
    }


    /**
     * 创建只有一个标签的条目
     *
     * @param label
     * @param frequency
     */
    public EnumFreqPair(E label, int frequency) {
        this();
        labelMap = ImmutableMap.of(label, frequency);

    }

    public EnumFreqPair(E label, int frequency, E label2, int freq) {
        this();
        labelMap = ImmutableMap.of(label, frequency, label2, freq);
    }

    public EnumFreqPair(E label, long frequency) {
        this();
        labelMap = ImmutableMap.of(label, (int) frequency);
    }

    /**
     * 创建一个条目，其标签频次都是1，各标签由参数指定
     *
     * @param x
     */
    @SafeVarargs
    public static <E extends Enum<E>> EnumFreqPair<E> create(E... x) {
        return new EnumFreqPair<>(x);
    }

    public static <E extends Enum> EnumFreqPair<E> create(List<String> params, Function<String, E> f) {
        EnumFreqPair<E> x = new EnumFreqPair<>();
        ImmutableMap.Builder<E, Integer> builder = ImmutableMap.builder();
        Iterator<String> ite = params.iterator();
        while (ite.hasNext()) {
            String key = ite.next();
            String value = ite.next();
            builder.put(f.apply(key), Ints.tryParse(value));
        }
        x.labelMap = builder.build();

        return x;
    }

    public Map<E, Integer> getMap() {
        return labelMap;
    }

    /**
     * 只有一个值的，key
     *
     * @return E
     */
    public E oneKey() {
        return labelMap.entrySet().iterator().next().getKey();
    }

    public Set<E> keySet() {
        return labelMap.keySet();
    }

    public int size() {
        return labelMap.size();
    }

    public boolean containsLabel(E label) {
        return labelMap.containsKey(label);
    }

    public int getFrequency(E label) {
        return labelMap.get(label);
    }

    @Override
    public String toString() {
        return labelMap.toString();
    }
}

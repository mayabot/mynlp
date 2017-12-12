/*
 *  Copyright 2017 mayabot.com authors. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mayabot.nlp.segment.dictionary;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import com.mayabot.nlp.collection.ValueSerializer;
import com.mayabot.nlp.segment.corpus.tag.Nature;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 核心词典中的词属性.
 * 想办法变成只读的类，里面的数据不可变
 * 表达了词的词性特征
 *
 * @author jimichan
 */
public final class NatureAttribute implements Serializable {

    private static final long serialVersionUID = 1L;

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
        ImmutableMap.Builder<Nature, Integer> builder = ImmutableMap.builder();
        for (int i = 0; i < natureCount; ++i) {
            builder.put(Nature.valueOf(param[1 + 2 * i]), Integer.parseInt(param[2 + 2 * i]));
        }
        natureAttribute.map = builder.build();
        natureAttribute.computeTotal();
        return natureAttribute;
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
//        final StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < nature.length; ++i) {
//            sb.append(nature[i]).append(' ').append(frequency[i])
//                    .append(' ');
//        }
        return map.toString();
    }


    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {

    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {

    }

    private void readObjectNoData()
            throws ObjectStreamException {

    }


    /**
     * 当NatureAttribute被持久化或读取的时候，提供一个统一的序列化或反序列化的工具
     */
    public final static ValueSerializer<NatureAttribute> valueSerializer = new ValueSerializer<NatureAttribute>() {

        @Override
        public byte[] serializer(List<NatureAttribute> sublist) throws IOException {
            ByteArrayOutputStream bbs = new ByteArrayOutputStream();

            bbs.write(Ints.toByteArray(sublist.size()));
            for (NatureAttribute att : sublist) {

                bbs.write(Ints.toByteArray(att.totalFrequency));
                bbs.write(Ints.toByteArray(att.map.size()));

                for (Map.Entry<Nature, Integer> e : att.map.entrySet()) {
                    bbs.write(Ints.toByteArray(e.getKey().ord)); // 应该是ord编号
                    bbs.write(Ints.toByteArray(e.getValue()));
                }
            }
            bbs.flush();
            return bbs.toByteArray();
        }

        @Override
        public List<NatureAttribute> unserializer(byte[] data) throws IOException, ClassNotFoundException {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            byte[] buffer = new byte[4];
            in.read(buffer);
            int size = Ints.fromByteArray(buffer);
            List<NatureAttribute> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                in.read(buffer);
                int totalFrequency = Ints.fromByteArray(buffer);
                in.read(buffer);
                int natureLen = Ints.fromByteArray(buffer);

                ImmutableMap.Builder<Nature, Integer> builder = ImmutableMap.builder();
                NatureAttribute att = new NatureAttribute();
                for (int j = 0; j < natureLen; j++) {
                    in.read(buffer);
                    Nature nature = Nature.valueOf(Ints.fromByteArray(buffer));
                    in.read(buffer);
                    int frequency = Ints.fromByteArray(buffer);
                    builder.put(nature, frequency);
                }

                att.map = builder.build();
                att.totalFrequency = totalFrequency;

                list.add(att);
            }
            return list;
        }
    };


    public int getTotalFrequency() {
        return totalFrequency;
    }


}

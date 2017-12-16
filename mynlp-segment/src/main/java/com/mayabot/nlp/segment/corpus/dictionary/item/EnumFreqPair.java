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
package com.mayabot.nlp.segment.corpus.dictionary.item;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import com.mayabot.nlp.segment.corpus.tag.NTTag;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 对标签-频次的封装
 *
 * @author hankcs
 */
public class EnumFreqPair<E extends Enum> {

    private ImmutableMap<E, Integer> labelMap = ImmutableMap.of();

    public EnumFreqPair() {
    }

    public void writeItem( DataOutput out) {
        try {
            int size = labelMap.size();

            if(size ==0 ){
                out.writeUTF("{}");
            }else if(size ==1){
                Map.Entry<E, Integer> next = labelMap.entrySet().iterator().next();
                String name = next.getKey().name();
                Integer f = next.getValue();
                out.writeUTF(String.format("{\""+name+"\":"+f+"}"));
            }else{
                out.writeUTF(JSON.toJSONString(labelMap));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void readItem(DataInput in,TypeReference<Map<E, Integer>> typeReference){
        try {
            String json = in.readUTF();
            Map<E, Integer> map = JSON.parseObject(json, typeReference);

            this.labelMap = ImmutableMap.copyOf(map);
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
        EnumFreqPair<E> ei = new EnumFreqPair<>(x);
        return ei;
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
     * @return
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

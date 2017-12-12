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

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author hankcs
 */
public class SimpleItem {
    /**
     * 该条目的标签
     */
    public Map<String, Integer> labelMap;

    public SimpleItem() {
        labelMap = new TreeMap<>();
    }

    public void addLabel(String label) {
        Integer frequency = labelMap.get(label);
        if (frequency == null) {
            frequency = 1;
        } else {
            ++frequency;
        }

        labelMap.put(label, frequency);
    }

    /**
     * 添加一个标签和频次
     *
     * @param label
     * @param frequency
     */
    public void addLabel(String label, Integer frequency) {
        Integer innerFrequency = labelMap.get(label);
        if (innerFrequency == null) {
            innerFrequency = frequency;
        } else {
            innerFrequency += frequency;
        }

        labelMap.put(label, innerFrequency);
    }

    /**
     * 删除一个标签
     *
     * @param label 标签
     */
    public void removeLabel(String label) {
        labelMap.remove(label);
    }

    public boolean containsLabel(String label) {
        return labelMap.containsKey(label);
    }

    public int getFrequency(String label) {
        Integer frequency = labelMap.get(label);
        if (frequency == null) return 0;
        return frequency;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        ArrayList<Map.Entry<String, Integer>> entries = new ArrayList<>(labelMap.entrySet());
        entries.sort((o1, o2) -> -o1.getValue().compareTo(o2.getValue()));
        for (Map.Entry<String, Integer> entry : entries) {
            sb.append(entry.getKey());
            sb.append(' ');
            sb.append(entry.getValue());
            sb.append(' ');
        }
        return sb.toString();
    }

    public static SimpleItem create(String param) {
        if (param == null) return null;
        String[] array = param.split(" ");
        return create(array);
    }

    public static SimpleItem create(String param[]) {
        if (param.length % 2 == 1) return null;
        SimpleItem item = new SimpleItem();
        int natureCount = (param.length) / 2;
        for (int i = 0; i < natureCount; ++i) {
            item.labelMap.put(param[2 * i], Integer.parseInt(param[1 + 2 * i]));
        }
        return item;
    }

    /**
     * 合并两个条目，两者的标签map会合并
     *
     * @param other
     */
    public void combine(SimpleItem other) {
        for (Map.Entry<String, Integer> entry : other.labelMap.entrySet()) {
            addLabel(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 获取全部频次
     *
     * @return
     */
    public int getTotalFrequency() {
        int frequency = 0;
        for (Integer f : labelMap.values()) {
            frequency += f;
        }
        return frequency;
    }

    public String getMostLikelyLabel() {
        return labelMap.entrySet().iterator().next().getKey();
    }
}

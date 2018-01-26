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
package com.mayabot.nlp.segment.corpus.dictionary.item;

import java.util.ArrayList;
import java.util.Map;

/**
 * 词典中的一个条目，比如“希望 v 7685 vn 616”
 *
 * @author hankcs
 */
public class Item extends SimpleItem {
    /**
     * 该条目的索引，比如“啊”
     */
    public String key;

    public Item(String key, String label) {
        this(key);
        labelMap.put(label, 1);
    }

    public Item(String key) {
        super();
        this.key = key;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(key);
        ArrayList<Map.Entry<String, Integer>> entries = new ArrayList<>(labelMap.entrySet());
        entries.sort((o1, o2) -> -o1.getValue().compareTo(o2.getValue()));
        for (Map.Entry<String, Integer> entry : entries) {
            sb.append(' ');             // 现阶段词典分隔符统一使用空格
            sb.append(entry.getKey());
            sb.append(' ');
            sb.append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * 获取首个label
     *
     * @return
     */
    public String firstLabel() {
        return labelMap.keySet().iterator().next();
    }

    /**
     * @param param 类似 “希望 v 7685 vn 616” 的字串
     * @return
     */
    public static Item create(String param) {
        if (param == null) {
            return null;
        }
        String mark = "\\s";    // 分隔符，历史格式用空格，但是现在觉得用制表符比较好
        if (param.indexOf('\t') > 0) {
            mark = "\t";
        }
        String[] array = param.split(mark);
        return create(array);
    }

    public static Item create(String[] param) {
        if (param.length % 2 == 0) {
            return null;
        }
        Item item = new Item(param[0]);
        int natureCount = (param.length - 1) / 2;
        for (int i = 0; i < natureCount; ++i) {
            item.labelMap.put(param[1 + 2 * i], Integer.parseInt(param[2 + 2 * i]));
        }
        return item;
    }
}

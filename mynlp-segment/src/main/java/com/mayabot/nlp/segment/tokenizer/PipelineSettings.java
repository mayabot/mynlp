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

package com.mayabot.nlp.segment.tokenizer;

import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

import java.util.Map;

public class PipelineSettings {

    private Map<String,String> map = Maps.newHashMap();

    public PipelineSettings() {

    }

    public String put(String key, String value) {
        return map.put(key, value);
    }

    public void putAll(Map<String,String> map) {
        this.map.putAll(map);
    }

    public String get(String key) {
        return map.get(key);
    }

    public String get(String key, String defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    public boolean getBool(String key, boolean defaultValue) {
        String s = map.get(key);
        if (s == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(s);
    }

    public int getInt(String key, int defaultValue) {
        String s = map.get(key);
        if (s == null) {
            return defaultValue;
        }
        return Ints.tryParse(s);
    }

    public static final PipelineSettings parseProperties(String string) {
        return null;
    }

    public static final PipelineSettings EMTPY = new PipelineSettings();
}

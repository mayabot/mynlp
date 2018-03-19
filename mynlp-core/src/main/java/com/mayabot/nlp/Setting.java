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

package com.mayabot.nlp;

import com.google.common.primitives.Ints;

import java.util.function.Function;

/**
 * @author jimichan
 * @param <T>
 */
public class Setting<T> {

    private String key;

    private String defaultValue;

    private Function<String, T> parse;

    private Setting() {

    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Function<String, T> getParse() {
        return parse;
    }

    public static Setting<Integer> intSetting(String key, int value) {
        Setting<Integer> setting = new Setting<>();
        setting.key = key;
        setting.defaultValue = value + "";
        setting.parse = Ints::tryParse;
        return setting;
    }

    public static Setting<String> stringSetting(String key, String value) {
        Setting<String> setting = new Setting<>();
        setting.key = key;
        setting.defaultValue = value;
        setting.parse = Function.identity();
        return setting;
    }

    public static Setting<Boolean> boolSetting(String key, boolean value) {
        Setting<Boolean> setting = new Setting<>();
        setting.key = key;
        setting.defaultValue = value + "";
        setting.parse = v -> v.equalsIgnoreCase("true");
        return setting;
    }
}

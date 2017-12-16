package com.mayabot.nlp;

import com.google.common.primitives.Booleans;
import com.google.common.primitives.Ints;

import java.util.function.Function;

public class Setting<T> {

    private String key;

    private String defaultValue;

    private Function<String,T> parse;

    private Setting(){

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

    public static Setting<String> stringSetting(String key, String  value) {
        Setting<String> setting = new Setting<>();
        setting.key = key;
        setting.defaultValue = value ;
        setting.parse = Function.identity();
        return setting;
    }

    public static Setting<Boolean> newBoolSetting(String key,String  value) {
        Setting<Boolean> setting = new Setting<>();
        setting.key = key;
        setting.defaultValue = value+"" ;
        setting.parse = v -> v.equalsIgnoreCase("true");
        return setting;
    }
}

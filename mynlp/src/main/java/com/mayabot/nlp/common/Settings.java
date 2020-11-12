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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * <p>
 *
 * @author jimichan
 */
public class Settings {

    private Map<String, String> settings;

    Settings(Map<String, String> settings) {
        this.settings = new HashMap(settings);
    }


    public Settings put(SettingItem setting, String value) {
        settings.put(setting.getKey(), value);
        return this;
    }

    public Settings put(String key, String value) {
        settings.put(key, value);
        return this;
    }

    public static Settings createEmpty() {
        return new Settings(new HashMap());
    }

    public static Settings create(Map<String, String> settings) {
        return new Settings(settings);
    }

    public static Settings create(Properties properties) {
        Map<String, String> map = new HashMap<>(16);
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            map.put(key, value);
        }
        return new Settings(map);
    }

    public <T> T get(SettingItem<T> setting) {
        String value = get(setting.getKey(), setting.getDefaultValue());
        return setting.getParse().apply(value);
    }

    /**
     * Returns the setting value associated with the setting key.
     *
     * @param key The setting key
     * @return The setting value, <tt>null</tt> if it does not exists.
     */
    private String get(String key) {
        return settings.get(key);
    }

    /**
     * Returns the setting value associated with the setting key. If it does not exists,
     * returns the default value provided.
     */
    private String get(String setting, String defaultValue) {
        return settings.getOrDefault(setting, defaultValue);
    }

    public List<String> getAsList(SettingItem<String> setting) {
        return getAsList(setting.getKey(), setting.getDefaultValue());
    }

    private List<String> getAsList(String key, String default_) {
        String obj = get(key, default_);

        if (obj == null) {
            return null;
        }
        return Guava.split(obj, ",");
    }


//    /**
//     * Returns the setting value (as float) associated with the setting key. If it does not exists,
//     * returns the default value provided.
//     */
//    public Float getAsFloat(String setting, Float defaultValue) {
//        String sValue = get(setting);
//        if (sValue == null) {
//            return defaultValue;
//        }
//        try {
//            return Float.parseFloat(sValue);
//        } catch (NumberFormatException e) {
//            throw new SettingsException("Failed to parse float setting [" + setting + "] with value [" + sValue + "]", e);
//        }
//    }
//
//    /**
//     * Returns the setting value (as double) associated with the setting key. If it does not exists,
//     * returns the default value provided.
//     */
//    public Double getAsDouble(String setting, Double defaultValue) {
//        String sValue = get(setting);
//        if (sValue == null) {
//            return defaultValue;
//        }
//        try {
//            return Double.parseDouble(sValue);
//        } catch (NumberFormatException e) {
//            throw new SettingsException("Failed to parse double setting [" + setting + "] with value [" + sValue + "]", e);
//        }
//    }
//
//    /**
//     * Returns the setting value (as int) associated with the setting key. If it does not exists,
//     * returns the default value provided.
//     */
//    public Integer getAsInt(String setting, Integer defaultValue) {
//        String sValue = get(setting);
//        if (sValue == null) {
//            return defaultValue;
//        }
//        try {
//            return Integer.parseInt(sValue);
//        } catch (NumberFormatException e) {
//            throw new SettingsException("Failed to parse int setting [" + setting + "] with value [" + sValue + "]", e);
//        }
//    }
//
//    /**
//     * Returns the setting value (as long) associated with the setting key. If it does not exists,
//     * returns the default value provided.
//     */
//    public Long getAsLong(String setting, Long defaultValue) {
//        String sValue = get(setting);
//        if (sValue == null) {
//            return defaultValue;
//        }
//        try {
//            return Long.parseLong(sValue);
//        } catch (NumberFormatException e) {
//            throw new SettingsException("Failed to parse long setting [" + setting + "] with value [" + sValue + "]", e);
//        }
//    }
//
//    /**
//     * Returns the setting value (as boolean) associated with the setting key. If it does not exists,
//     * returns the default value provided.
//     */
//    public Boolean getAsBoolean(String setting, Boolean defaultValue) {
//        String value = get(setting, defaultValue.toString());
//        return Boolean.valueOf(value);
//    }

//    /**
//     * A generic failure to handle settings.
//     */
//    public static class SettingsException extends RuntimeException {
//
//        public SettingsException(String message) {
//            super(message);
//        }
//
//        public SettingsException(String message, Throwable cause) {
//            super(message, cause);
//        }
//    }

}

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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;

/**
 * 系统属性 mynlp.conf 可以配置配置文件加载的目录,默认当前工作目录的conf
 *
 * @author jimichan
 */
public class Settings {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Settings.class);

    private Map<String, String> settings = ImmutableMap.of();

    public final static Settings EMPTY = new Settings(Maps.newHashMap());

    Settings(Map<String, String> settings) {
        this.settings = Maps.newHashMap(settings);
    }

    Settings(Settings settings) {
        this.settings = Maps.newHashMap(settings.settings);
    }

    public static final String KEY_CONF_DIR = "mynlp.conf.dir";
    public static final String KEY_DATA_DIR = "mynlp.data.dir";
    public static final String KEY_WORK_DIR = "mynlp.work.dir";
    public static final String KEY_WORK_DIR_NAME = "mynlp.work.name";


    public <T> T get(Setting<T> setting) {
        String value = get(setting.getKey(), setting.getDefaultValue());
        return setting.getParse().apply(value);
    }

    public void put(Setting setting, String value) {
        settings.put(setting.getKey(), value);
    }

    public void put(String key, String value) {
        settings.put(key, value);
    }

    public void put(String prefix,Setting setting, String value) {
        settings.put(prefix+"."+setting.getKey(), value);
    }

    /**
     * 后面的覆盖前面的设置
     * @param settings
     * @return
     */
    public static Settings merge(Settings ... settings) {
        Map<String,String> all = Maps.newHashMap();
        for (Settings setting : settings) {
            if (setting == null) {
                continue;
            }
            all.putAll(setting.settings);
        }
        return new Settings(ImmutableMap.copyOf(all));
    }

    public static Settings createEmpty() {
        return new Settings(ImmutableMap.of());
    }

    public static Settings createFrom(Settings settings) {
        return new Settings(settings);
    }

    public static Settings create(Properties properties) {
        Map<String, String> map = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            map.put(key, value);
        }
        return new Settings(map);
    }

    /**
     * 从遇到的第一个文件开始。
     * 2. classpath://maya_nlp.properties
     * 2. classpath://maya_nlp_default.properties
     *
     * @return
     */
    public static Settings build() {

        String configPath = System.getProperty(KEY_CONF_DIR, "conf");
        Path configDir = Paths.get(configPath);

        logger.info("Mynlp config path {}", configDir.toAbsolutePath().toString());

        @SuppressWarnings("unchecked") List<Supplier<InputStream>> list = Lists.newArrayList(

                () -> {
                    try {
                        return Resources.getResource("mynlp.default.properties").openStream();
                    } catch (Exception e) {
                        return null;
                    }
                },
                () -> {
                    try {
                        return Resources.getResource("mynlp.properties").openStream();
                    } catch (Exception e) {
                        return null;
                    }
                },

                () -> {
                    try {

                        File file = configDir.resolve("mynlp.properties").toFile();
                        if (file.exists()) {
                            return new FileInputStream(file);
                        }
                        return null;
                    } catch (Exception e) {
                        return null;
                    }
                },
                () -> {
                    try {
                        File file = configDir.resolve("mynlp.product.properties").toFile();
                        if (file.exists()) {
                            return new FileInputStream(file);
                        }
                        return null;
                    } catch (Exception e) {
                        return null;
                    }
                }
        );

        Map<String, String> map = Maps.newHashMap();

        for (Supplier<InputStream> supplier : list) {
            InputStream in = supplier.get();
            if (in != null) {
                Properties properties = new Properties();
                try {
                    properties.load(in);
                    for (String key : properties.stringPropertyNames()) {
                        String value = properties.getProperty(key);
                        map.put(key, value);
                    }
                } catch (Exception e) {

                }
                return new Settings(ImmutableMap.copyOf(map));
            }
        }

        return new Settings(Maps.newHashMap());
    }

    /**
     * 填充一个POJO配置对象
     *
     * @param objectToConfig
     */
    public void fillSettingConfigObject(Object objectToConfig) {
        //TODO 待实现
    }


    private final Splitter splitter = Splitter.on('.').omitEmptyStrings().trimResults();

    /**
     * Returns the setting value associated with the setting key.
     *
     * @param setting The setting key
     * @return The setting value, <tt>null</tt> if it does not exists.
     */
    public String get(String setting) {
        return settings.get(setting);
    }

    /**
     * Returns the setting value associated with the setting key. If it does not exists,
     * returns the default value provided.
     */
    public String get(String setting, String defaultValue) {
        return settings.getOrDefault(setting, defaultValue);
    }

    public List<String> getAsList(String setting) {
        return getAsList(setting, null);
    }

    public List<String> getAsList(String setting, String default_) {
        String obj = get(setting, default_);

        if (obj == null) {
            return null;
        }

        return Splitter.on(',').omitEmptyStrings().trimResults().splitToList(obj);
    }


    /**
     * A settings that are filtered (and key is removed) with the specified prefix.
     */
    public Settings getByPrefix(String prefix) {
        if (!prefix.endsWith(".")) {
            prefix += ".";
        }
        String _prefix = prefix;
        Map<String, String> sub = Maps.filterKeys(settings, key -> key.startsWith(_prefix));

        HashMap<String, String> result = Maps.newHashMap();

        for (Map.Entry<String, String> entry : sub.entrySet()) {
            result.put(entry.getKey().substring(_prefix.length()), entry.getValue());
        }

        return new Settings(ImmutableMap.copyOf(result));
    }


    /**
     * Returns the setting value (as float) associated with the setting key. If it does not exists,
     * returns the default value provided.
     */
    public Float getAsFloat(String setting, Float defaultValue) {
        String sValue = get(setting);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(sValue);
        } catch (NumberFormatException e) {
            throw new SettingsException("Failed to parse float setting [" + setting + "] with value [" + sValue + "]", e);
        }
    }

    /**
     * Returns the setting value (as double) associated with the setting key. If it does not exists,
     * returns the default value provided.
     */
    public Double getAsDouble(String setting, Double defaultValue) {
        String sValue = get(setting);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(sValue);
        } catch (NumberFormatException e) {
            throw new SettingsException("Failed to parse double setting [" + setting + "] with value [" + sValue + "]", e);
        }
    }

    /**
     * Returns the setting value (as int) associated with the setting key. If it does not exists,
     * returns the default value provided.
     */
    public Integer getAsInt(String setting, Integer defaultValue) {
        String sValue = get(setting);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(sValue);
        } catch (NumberFormatException e) {
            throw new SettingsException("Failed to parse int setting [" + setting + "] with value [" + sValue + "]", e);
        }
    }

    /**
     * Returns the setting value (as long) associated with the setting key. If it does not exists,
     * returns the default value provided.
     */
    public Long getAsLong(String setting, Long defaultValue) {
        String sValue = get(setting);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(sValue);
        } catch (NumberFormatException e) {
            throw new SettingsException("Failed to parse long setting [" + setting + "] with value [" + sValue + "]", e);
        }
    }

    /**
     * Returns the setting value (as boolean) associated with the setting key. If it does not exists,
     * returns the default value provided.
     */
    public Boolean getAsBoolean(String setting, Boolean defaultValue) {
        String value = get(setting, defaultValue.toString());
        return Boolean.valueOf(value);
    }

    /**
     * A generic failure to handle settings.
     */
    public static class SettingsException extends RuntimeException {

        public SettingsException(String message) {
            super(message);
        }

        public SettingsException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}

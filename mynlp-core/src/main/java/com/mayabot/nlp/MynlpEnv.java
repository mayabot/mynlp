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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.ClasspathNlpResourceFactory;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.resources.NlpResourceFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Mynlp运行环境。
 * 负责数据目录，缓存、资源加载、Settings等
 * @author jimichan
 */
public class MynlpEnv {

    public static InternalLogger logger = InternalLoggerFactory.getInstance(MynlpEnv.class);

    /**
     * 数据目录
     */
    private File dataDir;

    /**
     * 缓存文件目录
     */
    private File cacheDir;

    private List<NlpResourceFactory> resourceFactory;

    private Settings settings;

    public MynlpEnv(File dataDir, File cacheDir, List<NlpResourceFactory> resourceFactory, Settings settings) {
        this.dataDir = dataDir;
        this.cacheDir = cacheDir;
        this.resourceFactory = ImmutableList.copyOf(resourceFactory);
        this.settings = settings;
    }



    /**
     * 给只从classpath下加载资源的环境
     */
    public MynlpEnv() {
        resourceFactory = ImmutableList.of(new ClasspathNlpResourceFactory(Mynlps.class.getClassLoader()));
        settings = Settings.defaultSystemSettings();
    }


    public Settings getSettings() {
        return settings;
    }

    public void set(String key, String value) {
        settings.put(key, value);
    }

    /**
     * 加载资源
     *
     * @param resourceName 资源路径名称 dict/abc.dict
     * @param charset      字符集
     * @return NlpResource
     */
    public NlpResource loadResource(String resourceName, Charset charset) {
        if (resourceName == null || resourceName.trim().isEmpty()) {
            return null;
        }
        for (NlpResourceFactory factory : resourceFactory) {
            NlpResource resource = factory.load(resourceName, charset);
            if (resource != null) {
                String string = resource.toString();
                if (string.length() >= 60) {
                    string = "../.." + string.substring(string.length() - 60);
                }
                logger.info("load resource {}", string);
                return resource;
            }
        }
        return null;
    }

    /**
     * 加载资源
     *
     * @param resourceName 资源路径名称 dict/abc.dict
     * @return NlpResource
     */
    public NlpResource loadResource(String resourceName) {
        return this.loadResource(resourceName, Charsets.UTF_8);
    }

    public NlpResource loadResource(SettingItem<String> resourceNameSettting) {
        return this.loadResource(settings.get(resourceNameSettting), Charsets.UTF_8);
    }

    public File getDataDir() {
        return dataDir;
    }


    public File getCacheDir() {
        return cacheDir;
    }


}
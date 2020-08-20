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

import com.mayabot.nlp.common.logging.InternalLogger;
import com.mayabot.nlp.common.logging.InternalLoggerFactory;
import com.mayabot.nlp.common.resources.NlpResource;
import com.mayabot.nlp.common.resources.NlpResourceFactory;
import com.mayabot.nlp.common.utils.DictResDesc;
import com.mayabot.nlp.common.utils.DictResources;
import kotlin.text.Charsets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mynlp运行环境。
 * 负责数据目录，缓存、资源加载、Settings等
 *
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

    MynlpEnv(File dataDir, File cacheDir, List<NlpResourceFactory> resourceFactory, Settings settings) {
        this.dataDir = dataDir;
        this.cacheDir = cacheDir;
        this.resourceFactory = Collections.unmodifiableList(new ArrayList<>(resourceFactory));
        this.settings = settings;
    }

    public <T> T get(SettingItem<T> setting) {
        return settings.get(setting);
    }

    public <T> List<String> getAsList(SettingItem<String> setting) {
        return settings.getAsList(setting);
    }


    /**
     * 加载资源
     *
     * @param resourcePath 资源路径名称 dict/abc.dict
     * @return NlpResource
     */
    @Nullable
    public NlpResource loadResource(String resourcePath) {
        return this.loadResource(resourcePath, Charsets.UTF_8);
    }

    /**
     * 加载资源
     *
     * @param resourcePath 资源路径名称 dict/abc.dict
     * @param charset      字符集
     * @return NlpResource
     */
    public @NotNull
    NlpResource loadResource(String resourcePath, Charset charset) {

        if (resourcePath == null || resourcePath.trim().isEmpty()) {
            throw new RuntimeException("resourcePath is null");
        }

        return AccessController.doPrivileged((PrivilegedAction<NlpResource>) () -> {
            NlpResource resource = loadNlpResource(resourcePath, charset);

            if (resource == null) {
                StringBuilder info = new StringBuilder();
                info.append("\n\n没有找到资源 " + resourcePath + " ，");
                if (DictResources.INSTANCE.getMap().containsKey(resourcePath)) {
                    DictResDesc desc = DictResources.INSTANCE.getMap().get(resourcePath);
                    info.append("需要添加依赖 " + desc.getArtifactId() + ".jar\n\n");
                    info.append("Maven:\n\n");
                    info.append("<dependency>\n" +
                            "    <groupId>" + desc.getGroup() + "</groupId>\n" +
                            "    <artifactId>" + desc.getArtifactId() + "</artifactId>\n" +
                            "    <version>" + desc.getVersion() + "</version>\n" +
                            "</dependency>\n\n");
                    info.append("或者 Gradle:\n\n");
                    info.append("compile '" + desc.getGroup() + ":" + desc.getArtifactId() + ":" + desc.getVersion() + "'\n");
                    info.append("\n");
                }
                //logger.error(info.toString());

                throw new RuntimeException(
                        info.toString()
                );
            }

            return resource;
        });

    }


    /**
     * 计算资源的hash值。
     *
     * @param resourceName
     * @return hash
     */
    public @Nullable
    String hashResource(String resourceName) {

        NlpResource r1 = tryLoadResource(resourceName, Charsets.UTF_8);
        if (r1 != null) {
            return r1.hash();
        }
        return null;
    }

    /**
     * 加载资源，如果不存在，返回null，不抛出异常
     *
     * @param resourcePath
     * @param charset
     * @return 如果不存在，返回null，不抛出异常
     */
    public @Nullable
    NlpResource tryLoadResource(String resourcePath, Charset charset) {
        return AccessController.doPrivileged((PrivilegedAction<NlpResource>) () -> {
            if (resourcePath == null || resourcePath.trim().isEmpty()) {
                return null;
            }

            return loadNlpResource(resourcePath, charset);
        });
    }

    /**
     * 加载资源，如果不存在，返回null，不抛出异常
     *
     * @param resourcePath
     * @return 如果不存在，返回null，不抛出异常
     */
    public @Nullable
    NlpResource tryLoadResource(String resourcePath) {
        return this.tryLoadResource(resourcePath, Charsets.UTF_8);
    }

    /**
     * 加载资源，如果不存在，返回null，不抛出异常
     *
     * @param resourceNameSetting
     * @return 如果不存在，返回null，不抛出异常
     */
    public @Nullable
    NlpResource tryLoadResource(SettingItem<String> resourceNameSetting) {
        return this.tryLoadResource(settings.get(resourceNameSetting), Charsets.UTF_8);
    }

    private synchronized NlpResource loadNlpResource(String resourceName, Charset charset) {
        NlpResource resource = null;
        long t1 = System.currentTimeMillis();
        for (NlpResourceFactory factory : resourceFactory) {
            resource = factory.load(resourceName, charset);
            if (resource != null) {
                String string = resource.toString();
                if (string.length() >= 100) {
                    string = "../.." + string.substring(string.length() - 60);
                }
                long t2 = System.currentTimeMillis();

                logger.info("load resource {} ,use time {} ms", string, t2 - t1);
                break;
            }
        }
        return resource;
    }


    public File getDataDir() {
        return dataDir;
    }


    public File getCacheDir() {
        return cacheDir;
    }

}
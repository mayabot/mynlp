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
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.ClasspathNlpResourceFactory;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.resources.NlpResourceFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
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

    private List<ResoucesMissing> missingList = Lists.newArrayList();

    private String downloadBaseUrl = "http://cdn.mayabot.com/mynlp/files/";

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

    public void registeResourceMissing(ResoucesMissing missing) {
        this.missingList.add(missing);
    }


    public Settings getSettings() {
        return settings;
    }

    public void set(String key, String value) {
        settings.put(key, value);
    }


    /**
     * 计算资源的hash值
     *
     * @param resourceName
     * @return hash
     */
    public String hashResource(String resourceName) {

        NlpResource r1 = loadResource(resourceName, Charsets.UTF_8);
        if (r1 != null) {
            return r1.hash();
        }
        return null;
    }

    /**
     * 加载资源
     *
     * @param resourceName 资源路径名称 dict/abc.dict
     * @param charset      字符集
     * @return NlpResource
     */
    public synchronized NlpResource loadResource(String resourceName, Charset charset) {
        return AccessController.doPrivileged((PrivilegedAction<NlpResource>) () -> {
            if (resourceName == null || resourceName.trim().isEmpty()) {
                return null;
            }

            NlpResource resource = getNlpResource(resourceName, charset);

            boolean ps = false;

            if (resource == null) {
                for (ResoucesMissing missing : missingList) {
                    boolean r = missing.process(resourceName, this);
                    if (r) {
                        ps = true;
                        break;
                    }
                }
            }

            if (ps) {
                // load again
                resource = getNlpResource(resourceName, charset);
            }

            return resource;
        });

    }

    private NlpResource getNlpResource(String resourceName, Charset charset) {
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
                logger.info("load resource {} ,use time {} ms", string,t2-t1);
                break;
            }
        }
        return resource;
    }

    /**
     * 加载资源
     *
     * @param resourceName 资源路径名称 dict/abc.dict
     * @return NlpResource
     */
    public synchronized NlpResource loadResource(String resourceName) {
        return this.loadResource(resourceName, Charsets.UTF_8);
    }

    public synchronized NlpResource loadResource(SettingItem<String> resourceNameSettting) {
        return this.loadResource(settings.get(resourceNameSettting), Charsets.UTF_8);
    }

    public File getDataDir() {
        return dataDir;
    }


    public File getCacheDir() {
        return cacheDir;
    }


    /**
     * 从url地址下载jar文件，保存到data目录下
     */
    public synchronized File download(String fileName) {
        return AccessController.doPrivileged((PrivilegedAction<File>) () -> {

            File file = new File(dataDir, fileName);

            if (file.exists()) {
                return file;
            }

            try {
                URL url = new URL(downloadBaseUrl + fileName);

                URLConnection connection = url.openConnection();

                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                System.out.println("Downloading " + url);
                connection.connect();

                try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                     OutputStream out = new BufferedOutputStream(new FileOutputStream(file))
                ) {
                    ByteStreams.copy(inputStream, out);
                }

                System.out.println("Downloaded " + url + " , to " + file);

                if (file.exists()) {
                    return file;
                }

            } catch (Exception e) {
                System.err.println("Download " + (downloadBaseUrl + fileName) + " error!!!\n");
                e.printStackTrace();
                //下载失败 退出系统
                //System.exit(0);
            }

            return null;
        });
        // http://mayaasserts.oss-cn-shanghai.aliyuncs.com/mynlp/files/mynlp-resource-cws-hanlp-1.7.0.jar


    }
}
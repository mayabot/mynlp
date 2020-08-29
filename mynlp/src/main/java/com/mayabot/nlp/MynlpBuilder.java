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

import com.mayabot.nlp.common.GuavaKt;
import com.mayabot.nlp.common.Lists;
import com.mayabot.nlp.common.injector.AbstractModule;
import com.mayabot.nlp.common.injector.Injector;
import com.mayabot.nlp.common.injector.Module;
import com.mayabot.nlp.common.logging.InternalLogger;
import com.mayabot.nlp.common.logging.InternalLoggerFactory;
import com.mayabot.nlp.common.resources.ClasspathNlpResourceFactory;
import com.mayabot.nlp.common.resources.FileNlpResourceFactory;
import com.mayabot.nlp.common.resources.JarNlpResourceFactory;
import com.mayabot.nlp.common.resources.NlpResourceFactory;
import com.mayabot.nlp.common.utils.MynlpFactories;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mynlp构建器
 *
 * @author jimichan
 */
public class MynlpBuilder {

    public static InternalLogger logger = InternalLoggerFactory.getInstance("com.mayabot.nlp.Mynlps");

    /**
     * 数据目录默认在当前工作目录.
     * 注意这个文件夹不一定存在
     */
    private String dataDir;

    /**
     * 各种缓存文件放置的地方。默认在dataDir目录下面的caches
     */
    private String cacheDir;

    private ArrayList<NlpResourceFactory> resourceFactoryList = new ArrayList<>();

    private Settings settings = Settings.createEmpty();

    private Map<Class, Object> injectInstance = new HashMap<>();


    /**
     * 构建Mynlp实例。
     * 一般情况通过Mynlps获取全局唯一实例。不需要调用这个方法构造。
     * 这个保留public是为了test和一些特殊多实例场景使用。
     * Mynlp是一个重量级的对象，是一个容器,包含了Mynlp运行时所有的组件。
     *
     * @return Mynlp
     */
    public Mynlp build() {
        return AccessController.doPrivileged((PrivilegedAction<Mynlp>) () -> {
            try {
                logger.info("Current Working Dir is " + new File(".").getAbsolutePath());

                if (dataDir == null) {
                    //通过JVM系统属性配置 -Dmynlp.data=/path/data
                    if (System.getProperty("mynlp.data.dir") != null) {
                        dataDir = System.getProperty("mynlp.data.dir");
                    }
                }
//                if (dataDir == null) {
//                    // 在全局配置文件中 data.dir 可以指定dir目录，默认是当前工作目录下面的data
//
//                    dataDir = settings.get("data.dir", null);
//                }

                // 默认的位置
                if (dataDir == null) {
                    String userHome = System.getProperty("user.home");

                    if (new File(userHome + "/.mynlp.data").exists()) {
                        dataDir = userHome + "/.mynlp.data";
                    } else {
                        dataDir = userHome + "/mynlp.data";
                    }
                }

                File dataDirFile = new File(dataDir);

                logger.info("Mynlp data dir is " + dataDirFile.getAbsolutePath() + ",exists " + dataDirFile.exists());

//                if (settings.get("cache.dir") != null) {
//                    cacheDir = settings.get("cache.dir");
//                }

                File cacheDirFile;
                if (cacheDir == null) {
                    String userHome = System.getProperty("user.home");

                    if (new File(userHome + "/.mynlp.data").exists()) {
                        cacheDir = userHome + "/.mynlp.data";
                    } else {
                        cacheDir = userHome + "/mynlp.data";
                    }

                    cacheDirFile = ensureDir(new File(cacheDir, "cache"));

                } else {

                    cacheDirFile = ensureDir(new File(cacheDir));

                }

                logger.info("Mynlp cache dir is {}", cacheDirFile.getAbsolutePath());

                resourceFactoryList.add(new FileNlpResourceFactory(dataDirFile));
                resourceFactoryList.add(new JarNlpResourceFactory(dataDirFile));
                resourceFactoryList.add(new ClasspathNlpResourceFactory(Mynlps.class.getClassLoader()));

                MynlpEnv env = new MynlpEnv(dataDirFile, cacheDirFile, resourceFactoryList, settings);

                Injector injector = createInject(env);

                return new Mynlp(env, injector);
            } catch (Exception e) {
                throw new RuntimeException("Mynlp instance build error", e);
            }
        });

    }

    private Injector createInject(MynlpEnv mynlpEnv) {

        ArrayList<Module> modules = Lists.newArrayList();

        modules.add(new AbstractModule() {
            @Override
            public void configure() {
                bind(MynlpEnv.class).toInstance(mynlpEnv);
                injectInstance.forEach((k, v) -> bind(k).toInstance(v));
            }
        });

        //加载模块，在配置文件中声明的
        modules.addAll(loadModules(mynlpEnv));

        return Injector.create(modules);
    }

    private List<Module> loadModules(MynlpEnv mynlp) {
        try {
            Collection<Class> classes = MynlpFactories.load().get(MynlpFactories.GuiceModule);

            return classes.stream().map(clazz -> {
                try {
                    try {
                        Constructor<? extends Module> c1 = ((Class<? extends Module>) clazz).getConstructor(MynlpEnv.class);
                        if (c1 != null) {
                            return c1.newInstance(mynlp);
                        }
                    } catch (NoSuchMethodException e) {
                        //throw new RuntimeException(e);
                    }

                    return (Module) clazz.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 增加自定义的资源工厂。可以自定义从数据库或者其他来源去加载数据.
     *
     * @param resourceFactory
     */
    public void addResourceFactory(NlpResourceFactory resourceFactory) {
        resourceFactoryList.add(resourceFactory);
    }

    public String getDataDir() {
        return dataDir;
    }

    public MynlpBuilder setDataDir(String dataDir) {
        this.dataDir = dataDir;
        return this;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public MynlpBuilder setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
        return this;
    }

//    public Settings getSettings() {
//        return settings;
//    }

    public MynlpBuilder set(String key, String value) {
        settings.put(key, value);
        return this;
    }

    public MynlpBuilder set(SettingItem key, String value) {
        settings.put(key, value);
        return this;
    }


    public <T> MynlpBuilder bind(Class<T> clazz, T object) {
        this.injectInstance.put(clazz, object);
        return this;
    }

    private File ensureDir(File file) throws IOException {
        if (!file.exists()) {
            createParentDirs(file);
            file.mkdir();
        }
        if (!file.isDirectory()) {
            throw new IOException(file + " is not dir");
        }
        return file;
    }

    private void createParentDirs(File file) throws IOException {
        GuavaKt.checkNotNull(file);
        File parent = file.getCanonicalFile().getParentFile();
        if (parent == null) {
            /*
             * The given directory is a filesystem root. All zero of its ancestors
             * exist. This doesn't mean that the root itself exists -- consider x:\ on
             * a Windows machine without such a drive -- or even that the caller can
             * create it, but this method makes no such guarantees even for non-root
             * files.
             */
            return;
        }
        parent.mkdirs();
        if (!parent.isDirectory()) {
            throw new IOException("Unable to create parent directories of " + file);
        }
    }
}
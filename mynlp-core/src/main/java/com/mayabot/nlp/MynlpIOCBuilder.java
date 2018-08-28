package com.mayabot.nlp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.ClasspathNlpResourceFactory;
import com.mayabot.nlp.resources.FileNlpResourceFactory;
import com.mayabot.nlp.resources.NlpResourceFactory;
import com.mayabot.nlp.utils.MynlpFactories;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MynlpIOCBuilder {

    public static InternalLogger logger = InternalLoggerFactory.getInstance("com.mayabot.nlp.Mynlp");

    /**
     * 数据目录默认在当前工作目录
     */
    private String dataDir = "data";


    /**
     * 各种缓存文件放置的地方。默认在dataDir目录下面的caches
     */
    private String cacheDir;

    private ArrayList<NlpResourceFactory> resourceFactoryList =
            Lists.newArrayList();

    private Settings settings;

    private Map<Class, Object> preObj = Maps.newHashMap();

    MynlpIOC build() throws RuntimeException {
        MynlpIOC ioc = new MynlpIOC();
        try {
            logger.info("Current Working Dir is " + new File(".").getAbsolutePath());


            File dataDirFile = ensureDir(new File(dataDir));
            logger.info("Mynlp data dir is " + dataDirFile.getAbsolutePath());
            ioc.dataDir = dataDirFile;

            File cacheDirFile = null;
            if (cacheDir == null) {
                cacheDirFile = ensureDir(new File(dataDirFile, "caches"));
            } else {
                cacheDirFile = ensureDir(new File(cacheDir));
            }
            ioc.cacheDir = cacheDirFile;

            logger.info("Mynlp cache dir is {}", cacheDirFile.getAbsolutePath());

            //
            resourceFactoryList.add(new FileNlpResourceFactory(dataDirFile));
            resourceFactoryList.add(new ClasspathNlpResourceFactory(Mynlp.class.getClassLoader()));

            ioc.resourceFactory = ImmutableList.copyOf(resourceFactoryList);

            if (settings == null) {
                ioc.settings = Settings.defaultSettings();
            } else {
                ioc.settings = Settings.merge(Settings.defaultSettings(), settings);
            }

            ioc.injector = createInject(ioc);


            return ioc;
        } catch (Exception e) {
            throw new RuntimeException((e));
        }
    }


    private Injector createInject(MynlpIOC mynlp) {

        ArrayList<Module> modules = Lists.newArrayList();

        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MynlpIOC.class).toInstance(mynlp);
                bind(Settings.class).toInstance(mynlp.settings);
                preObj.forEach((k, v) -> bind(k).toInstance(v));
            }
        });

        //加载模块，在配置文件中声明的
        modules.addAll(loadModules());

        return Guice.createInjector(modules);
    }

    private List<Module> loadModules() {
        try {
            Collection<Class> classes = MynlpFactories.load().get(MynlpFactories.GuiceModule);

            return classes.stream().map(clazz -> {
                try {
                    try {
                        Constructor<? extends Module> c1 = clazz.getConstructor(Mynlp.class);
                        if (c1 != null) {
                            return c1.newInstance(this);
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

    public MynlpIOCBuilder setDataDir(String dataDir) {
        this.dataDir = dataDir;
        return this;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public MynlpIOCBuilder setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
        return this;
    }

    public Settings getSettings() {
        if (settings == null) {
            settings = Settings.createEmpty();
        }
        return settings;
    }

    public MynlpIOCBuilder set(String key, String value) {
        getSettings().put(key, value);
        return this;
    }

    public MynlpIOCBuilder set(Setting key, String value) {
        getSettings().put(key, value);
        return this;
    }

    public MynlpIOCBuilder setSettings(Settings settings) {
        this.settings = settings;
        return this;
    }

    public <T> MynlpIOCBuilder bind(Class<T> clazz, T object) {
        this.preObj.put(clazz, object);
        return this;
    }

    private File ensureDir(File file) throws IOException {
        if (!file.exists()) {
            Files.createParentDirs(file);
            file.mkdir();
        }
        if (!file.isDirectory()) {
            throw new IOException(file + " is not dir");
        }
        return file;
    }
}
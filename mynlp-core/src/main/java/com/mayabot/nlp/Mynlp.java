package com.mayabot.nlp;

import com.google.common.base.Charsets;
import com.google.common.collect.*;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.ClassPathResourceFactory;
import com.mayabot.nlp.resources.FileResourceFactory;
import com.mayabot.nlp.resources.MynlpResource;
import com.mayabot.nlp.resources.ResourceFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 包含一个IOC容器，管理Mynlp所有的资源。
 * 在项目Mynlp对象应该作为单例，不需要重复创建。
 *
 * @author jimichan
 */
public class Mynlp {

    private static InternalLogger logger = InternalLoggerFactory.getInstance(Mynlp.class);

    public static MynlpBuilder builder() {
        return new MynlpBuilder();
    }

//    private static boolean instanced = false;


    /**
     * 数据目录
     */
    private File dataDir;

    /**
     * 缓存文件目录
     */
    private File cacheDir;

    private Injector injector;

    private List<ResourceFactory> resourceFactory;

    private Settings settings;

    /**
     * 用户有机会向Injector注入实例
     */
    private Map<Class, Object> preObj = Maps.newHashMap();

    private Mynlp() {
    }


    private void start() {
        injector = createInject();
    }


    private Injector createInject() {

        ArrayList<Module> modules = Lists.newArrayList();

        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Mynlp.class).toInstance(Mynlp.this);
                preObj.forEach((k, v) -> bind(k).toInstance(v));
            }
        });

        //加载模块，在配置文件中声明的
        modules.addAll(loadModules());

        return Guice.createInjector(modules);
    }

    private List<Module> loadModules() {
        try {

            Set<String> set = Sets.newHashSet();
            Enumeration<URL> resources = Mynlp.class.getClassLoader().getResources("META-INF/mynlp.factories");

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();

                logger.info("Found mynlp.factories {}", url.toString());

                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));

                String line = reader.readLine();

                while (line != null) {

                    set.add(line);

                    line = reader.readLine();
                }


                reader.close();
            }

            return set.stream().filter(line -> !line.trim().isEmpty()).map(clazzName -> {
                try {
                    Class<? extends Module> clazz = (Class<? extends Module>) Class.forName(clazzName.trim());

                    try {
                        Constructor<? extends Module> c1 = clazz.getConstructor(Mynlp.class);
                        if (c1 != null) {
                            return c1.newInstance(this);
                        }
                    } catch (NoSuchMethodException e) {
                        //throw new RuntimeException(e);
                    }

                    //call default con

                    return clazz.newInstance();
                } catch (ClassNotFoundException e) {
                    System.err.println("Not Found Class " + clazzName);
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    public void injectMembers(Object object) {
        injector.injectMembers(object);
    }


    public Settings getSettings() {
        return settings;
    }

    public <T> T getSetting(Setting<T> key) {
        return settings.get(key);
    }

    /**
     * 加载资源
     *
     * @param resourceName 资源路径名称 dict/abc.dict
     * @param charset      字符集
     * @return
     */
    public MynlpResource loadResource(String resourceName, Charset charset) {
        if (resourceName == null || resourceName.trim().isEmpty()) {
            return null;
        }
        for (ResourceFactory factory : resourceFactory) {
            MynlpResource resource = factory.load(resourceName, charset);
            if (resource != null) {
                logger.info("load resource from {}", resource.toString());
                return resource;
            }
        }
        return null;
    }

    /**
     * 加载资源
     *
     * @param resourceName 资源路径名称 dict/abc.dict
     * @return
     */
    public MynlpResource loadResource(String resourceName) {
        return this.loadResource(resourceName, Charsets.UTF_8);
    }

    public MynlpResource loadResource(Setting<String> resourceNameSettting) {
        return this.loadResource(settings.get(resourceNameSettting), Charsets.UTF_8);
    }

    public File getDataDir() {
        return dataDir;
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public static class MynlpBuilder {

        /**
         * 数据目录默认在当前工作目录
         */
        private String dataDir = "data";


        /**
         * 各种缓存文件放置的地方。默认在dataDir目录下面的caches
         */
        private String cacheDir;

        private ArrayList<ResourceFactory> resourceFactoryList =
                Lists.newArrayList();

        private Settings settings;

        private Map<Class, Object> preObj = Maps.newHashMap();

        public Mynlp build() throws RuntimeException {
            try {
                Mynlp mynlp = new Mynlp();

                logger.info("Current Working Dir is " + new File(".").getAbsolutePath());

                File dataDirFile = ensureDir(new File(dataDir));
                logger.info("Mynlp data dir is " + dataDirFile.getAbsolutePath());
                mynlp.dataDir = dataDirFile;

                File cacheDirFile = null;
                if (cacheDir == null) {
                    cacheDirFile = ensureDir(new File(dataDirFile, "caches"));
                } else {
                    cacheDirFile = ensureDir(new File(cacheDir));
                }
                mynlp.cacheDir = cacheDirFile;

                logger.info("Mynlp cache dir is {}", cacheDirFile.getAbsolutePath());

                //
                resourceFactoryList.add(new FileResourceFactory(dataDirFile));
                resourceFactoryList.add(new ClassPathResourceFactory(Mynlp.class.getClassLoader()));

                mynlp.resourceFactory = ImmutableList.copyOf(resourceFactoryList);

                if (settings == null) {
                    mynlp.settings = Settings.defaultSettings();
                } else {
                    mynlp.settings = Settings.merge(Settings.defaultSettings(), settings);
                }

                mynlp.preObj = ImmutableMap.copyOf(preObj);

                mynlp.start();

                return mynlp;
            } catch (Exception e) {
                throw new RuntimeException((e));
            }
        }

        /**
         * 增加自定义的资源工厂。可以自定义从数据库或者其他来源去加载数据.
         *
         * @param resourceFactory
         */
        public void addResourceFactory(ResourceFactory resourceFactory) {
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

        public Settings getSettings() {
            return settings;
        }

        public MynlpBuilder setSettings(Settings settings) {
            this.settings = settings;
            return this;
        }

        public <T> MynlpBuilder bind(Class<T> clazz, T object) {
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

}

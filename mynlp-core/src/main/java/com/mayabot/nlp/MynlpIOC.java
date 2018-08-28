package com.mayabot.nlp;

import com.google.common.base.Charsets;
import com.google.inject.Injector;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.resources.NlpResourceFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

public class MynlpIOC {

    public static InternalLogger logger = InternalLoggerFactory.getInstance("com.mayabot.nlp.Mynlp");

    /**
     * 数据目录
     */
    File dataDir;

    /**
     * 缓存文件目录
     */
    File cacheDir;

    Injector injector;

    List<NlpResourceFactory> resourceFactory;

    Settings settings;

    MynlpIOC() {

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
    public NlpResource loadResource(String resourceName, Charset charset) {
        if (resourceName == null || resourceName.trim().isEmpty()) {
            return null;
        }
        for (NlpResourceFactory factory : resourceFactory) {
            NlpResource resource = factory.load(resourceName, charset);
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
    public NlpResource loadResource(String resourceName) {
        return this.loadResource(resourceName, Charsets.UTF_8);
    }

    public NlpResource loadResource(Setting<String> resourceNameSettting) {
        return this.loadResource(settings.get(resourceNameSettting), Charsets.UTF_8);
    }

    public File getDataDir() {
        return dataDir;
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public Injector getInjector() {
        return injector;
    }
}
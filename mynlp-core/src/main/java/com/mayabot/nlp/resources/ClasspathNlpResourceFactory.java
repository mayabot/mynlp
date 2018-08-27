package com.mayabot.nlp.resources;

import java.net.URL;
import java.nio.charset.Charset;

/**
 * 从Claspath下面的maya_data路径下加载资源
 *
 * @author jimichan
 */
public class ClasspathNlpResourceFactory implements NlpResourceFactory {

    private ClassLoader classLoader;

    public ClasspathNlpResourceFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public NlpResource load(String resourceName, Charset charset) {

        if (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1);
        }
        String path = "maya_data/" + resourceName;

        URL resource = classLoader.getResource(path);

        if (resource != null) {
            return new URLNlpResource(resource, charset);
        }

        resource = classLoader.getResource(path + ".zip");
        if (resource != null) {
            return new URLNlpResource(resource, charset);
        }


        return null;
    }
}



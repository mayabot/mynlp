package com.mayabot.nlp.resources;

import java.net.URL;
import java.nio.charset.Charset;

public class ClassPathResourceFactory implements ResourceFactory {

    private ClassLoader classLoader;

    public ClassPathResourceFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public MynlpResource load(String resourceName, Charset charset) {

        if (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1);
        }
        String path = "maya_data/" + resourceName;

        URL resource = classLoader.getResource(path);

        if (resource != null) {
            return new URLMynlpResource(resource, charset);
        }

        resource = classLoader.getResource(path + ".zip");
        if (resource != null) {
            return new URLMynlpResource(resource, charset);
        }


        return null;
    }
}



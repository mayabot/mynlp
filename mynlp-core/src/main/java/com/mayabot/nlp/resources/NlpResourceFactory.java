package com.mayabot.nlp.resources;

import com.google.common.base.Charsets;

import java.nio.charset.Charset;

/**
 * 资源文件的来源。比如从文件系统里面的加载，或者从classpath里面去加载
 *
 * @author jimichan
 */
public interface NlpResourceFactory {

    /**
     * 加载资源
     *
     * @param resourceName 格式为 dict/abc.dict
     * @param charset      字符集
     * @return 如果资源不存在那么返回NULL
     */
    NlpResource load(String resourceName, Charset charset);

    /**
     * 加载资源
     *
     * @param resourceName
     * @return
     */
    default NlpResource load(String resourceName) {
        return load(resourceName, Charsets.UTF_8);
    }

}

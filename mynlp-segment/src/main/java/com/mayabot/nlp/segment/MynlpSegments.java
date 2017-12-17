package com.mayabot.nlp.segment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MynlpSegments 是mynlp-segment模块对外门面。此后只可以增加方法
 */
public final class MynlpSegments {

    static ConcurrentHashMap<String, MynlpTokenizer> map = new ConcurrentHashMap<>();
    static Map<String, Object> configMap = null;

    static {

        URL resourceAsStream = MynlpSegments.class.getClassLoader().getResource("META-INF/tokenizers.json");
        try {
            String json = Resources.asCharSource(resourceAsStream, Charsets.UTF_8).read();

            Map<String, Object> map1 = (JSONObject) JSON.parse(json);

            configMap = map1;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MynlpTokenizer getDefault() {
        return get("default");
    }

    public static MynlpTokenizer nlp() {
        return get("default");
    }


    public static MynlpTokenizer crf() {
        return get("crf");
    }


    public static MynlpTokenizer get(String name) {
        return map.computeIfAbsent(name, n -> {
            Map<String, Object> config = (Map) configMap.get(n);
            return WordnetTokenizerFactory.get().build(config);
        });
    }

}

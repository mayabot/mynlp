package com.mayabot.nlp.segment;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PreDefinedTokenizer {

    static ConcurrentHashMap<String, MyTokenizer> map = new ConcurrentHashMap<>();
    static Map<String, Object> configMap = null;

    static {

        URL resourceAsStream = PreDefinedTokenizer.class.getClassLoader().getResource("META-INF/tokenizers.json");
        try {
            String json = Resources.asCharSource(resourceAsStream, Charsets.UTF_8).read();

            Map<String,Object> map1 = new Gson().fromJson(json, Map.class);

            configMap = map1;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MyTokenizer getDefault() {
        return get("default");
    }

    public static MyTokenizer nlp() {
        return get("default");
    }


    public static MyTokenizer crf() {
        return get("crf");
    }


    public static MyTokenizer get(String name) {
        return map.computeIfAbsent(name, n -> {
            Map<String,Object> config = (Map)configMap.get(n);
            return WordnetTokenizerFactory.get().build(config);
        });
    }

}

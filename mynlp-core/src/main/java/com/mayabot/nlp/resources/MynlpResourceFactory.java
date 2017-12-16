package com.mayabot.nlp.resources;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.Environment;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MynlpResourceFactory {

    Map<String, Function<String, ? extends MynlpResource>> map = Maps.newHashMap();

    public MynlpResourceFactory(Path dataDir){
        map.put("inner", path -> new InnerMynlpResource(dataDir, path));
    }


    public void register(String type, Function<String, ? extends MynlpResource> factory) {
        map.put(type, factory);
    }

//    /**
//     * 便捷方法 from dictionary dir
//     *
//     * @param path
//     * @return
//     */
//    public MynlpResource loadDictionary(String path){
//
//        if (path.startsWith("/")) {
//            path = path.substring(1);
//        }
//        return resoloveResourceUrl("dictionary/"+path);
//    }
//
//    /**
//     * 便捷方法 from model dir
//     *
//     * @param path
//     * @return
//     */
//    public MynlpResource loadModel(String path) {
//        if (path.startsWith("/")) {
//            path = path.substring(1);
//        }
//        return resoloveResourceUrl("model/"+path);
//    }


    /**
     * inner://dict/abc/dt.txt
     * redis://words/ww
     * mongo://abc
     * @param url
     * @return
     */
    public MynlpResource load(String url) {

        Pattern pattern = Pattern.compile("^(.+?)://(.+)$");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String type = matcher.group(1);
            String path = matcher.group(2);

            Function<String, ? extends MynlpResource> function = map.get(type);

            Preconditions.checkNotNull(function);

            return function.apply(path);
        }


        return null;
    }
}

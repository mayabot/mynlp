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

package com.mayabot.nlp.resources;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MynlpResourceFactory {

    Map<String, Function<String, ? extends MynlpResource>> map = Maps.newHashMap();

    public MynlpResourceFactory(Path dataDir) {
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
     *
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

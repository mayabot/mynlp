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

package com.mayabot.nlp;

import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 包含一个IOC容器，管理Mynlp所有的资源。
 * 在项目Mynlp对象应该作为单例，不需要重复创建。
 *
 * @author jimichan
 */
public final class Mynlps {


    public static InternalLogger logger = InternalLoggerFactory.getInstance("com.mayabot.nlp.Mynlps");

    private static final ConcurrentHashMap<String, Mynlp> map = new ConcurrentHashMap<>();

    public static void install(Consumer<MynlpBuilder> consumer) {
        if (map.isEmpty()) {
            MynlpBuilder builder = new MynlpBuilder();
            consumer.accept(builder);
            map.put("I", builder.build());
        } else {
            throw new RuntimeException("Do install action before call get()!");
        }
    }

    public static void clear() {
        map.clear();
    }

    public static Mynlp get() {
        return map.computeIfAbsent("I", Mynlps::create);
    }

    private static Mynlp create(String s) {
        return new MynlpBuilder().build();
    }

    private Mynlps() {

    }

    public static <T> T getInstance(Class<T> clazz) {
        return get().getInstance(clazz);
    }

}

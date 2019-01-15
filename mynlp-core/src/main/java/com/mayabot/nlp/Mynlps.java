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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Mynlp包含一个guice实现的IOC容器，管理Mynlp所有的资源。
 * 在项目Mynlp对象应该作为单例，不需要重复创建。
 *
 * @author jimichan
 */
public final class Mynlps {

    public static InternalLogger logger = InternalLoggerFactory.getInstance("com.mayabot.nlp.Mynlps");

    private static final ConcurrentHashMap<String, Mynlp> map = new ConcurrentHashMap<>();

    /**
     * 其他任何Mynlp方式之前调用，通过回调MynlpBuilder进行系统设置。
     * @param consumer 设置MynlpBuilder
     */
    public static void install(Consumer<MynlpBuilder> consumer) {
        if (map.isEmpty()) {
            MynlpBuilder builder = new MynlpBuilder();
            consumer.accept(builder);
            map.put("I", builder.build());
        } else {
            throw new RuntimeException("Do install action before call get()!");
        }
    }

    /**
     * 设置DataDir。
     * 在调用install和其他任何Mynlp方式之前调用
     *
     * @param dataDir 数据目录。默认在当前用户目录下.mynlp.data文件夹
     */
    public static void setDataDir(String dataDir) {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            System.setProperty("mynlp.data.dir", dataDir);
            return null;
        });
    }

    /**
     * 清空Mynlp实例。
     */
    public static void clear() {
        map.clear();
    }

    /**
     * 获取全局唯一的Mynlp实例。
     * @return
     */
    public static Mynlp get() {
        return map.computeIfAbsent("I", Mynlps::create);
    }

    private static Mynlp create(String s) {
        return AccessController.doPrivileged(
                (PrivilegedAction<Mynlp>) () -> new MynlpBuilder().build());
    }

    /**
     * 不可被外界实例化
     */
    private Mynlps() {

    }

    /**
     * 返回Mynlp容器中指定class的Bean。
     *
     * @param clazz class
     * @param <T>   类型参数
     * @return 返回实例Bean
     */
    public static <T> T instanceOf(Class<T> clazz) {
        return AccessController.doPrivileged((PrivilegedAction<T>) () -> get().getInstance(clazz));
    }

}

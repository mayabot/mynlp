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

    public static void install(Consumer<MynlpContainerBuilder> consumer) {
        if (map.isEmpty()) {
            MynlpContainerBuilder builder = new MynlpContainerBuilder();
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
        return new MynlpContainerBuilder().build();
    }


    private Mynlps() {

    }

    public static <T> T getInstance(Class<T> clazz) {
        return get().getInstance(clazz);
    }

}

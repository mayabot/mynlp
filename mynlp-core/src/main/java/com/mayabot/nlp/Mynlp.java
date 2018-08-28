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
public final class Mynlp {

    public static InternalLogger logger = InternalLoggerFactory.getInstance("com.mayabot.nlp.Mynlp");

    private static final ConcurrentHashMap<String, MynlpIOC> map = new ConcurrentHashMap<>();

    public static void install(Consumer<MynlpIOCBuilder> consumer) {
        if (map.isEmpty()) {
            MynlpIOCBuilder builder = new MynlpIOCBuilder();
            consumer.accept(builder);
            map.put("I", builder.build());
        } else {
            throw new RuntimeException("Do install action before call get()!");
        }
    }

    public static void clear() {
        map.clear();
    }

    public static MynlpIOC get() {
        return map.computeIfAbsent("I", Mynlp::create);
    }

    private static MynlpIOC create(String s) {
        return new MynlpIOCBuilder().build();
    }


    private Mynlp() {

    }

    public static <T> T getInstance(Class<T> clazz) {
        return get().getInstance(clazz);
    }

}

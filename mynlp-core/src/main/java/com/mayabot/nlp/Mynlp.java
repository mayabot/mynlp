package com.mayabot.nlp;

import com.google.inject.Injector;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

/**
 * @author jimichan
 */
public class Mynlp {

    public static InternalLogger logger = InternalLoggerFactory.getInstance(Mynlp.class);

    private MynlpEnv env;

    /**
     * guice injector
     */
    private Injector injector;

    public Mynlp(MynlpEnv env, Injector injector) {
        this.env = env;
        this.injector = injector;
    }

    public MynlpEnv getEnv() {
        return env;
    }

    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    public void injectMembers(Object object) {
        injector.injectMembers(object);
    }

    public Injector getInjector() {
        return injector;
    }
}
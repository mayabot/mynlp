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

import com.mayabot.nlp.injector.Injector;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

/**
 * 包含了执行环境和Guice IOC容器
 *
 * @author jimichan
 */
public final class Mynlp {

    public static InternalLogger logger = InternalLoggerFactory.getInstance(Mynlp.class);

    private MynlpEnv env;

    /**
     * guice injector
     */
    private Injector injector;

    Mynlp(MynlpEnv env, Injector injector) {
        this.env = env;
        this.injector = injector;
    }

    public MynlpEnv getEnv() {
        return env;
    }

    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    public Injector getInjector() {
        return injector;
    }
}
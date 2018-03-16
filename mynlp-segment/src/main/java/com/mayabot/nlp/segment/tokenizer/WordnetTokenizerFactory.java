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

package com.mayabot.nlp.segment.tokenizer;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpInjector;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.segment.NamedComponentRegistry;
import com.mayabot.nlp.segment.WordnetInitializer;
import com.mayabot.nlp.segment.wordnet.BestPathComputer;

/**
 * 根据JSON配置文件产生一个WordnetTokenizer对象
 *
 * @author jimichan
 */
@Singleton
public class WordnetTokenizerFactory {

    static InternalLogger logger = InternalLoggerFactory.getInstance(WordnetTokenizerFactory.class);

    private final NamedComponentRegistry registry;
    private final Injector injector;
    private final PipelineFactory pipelineFactory;


    @Inject
    WordnetTokenizerFactory(NamedComponentRegistry registry, Injector injector, PipelineFactory pipelineFactory) {
        this.registry = registry;
        this.injector = injector;
        this.pipelineFactory = pipelineFactory;
    }

    /**
     * Just building 延迟初始化，所以向NamedComponentRegistry注册新的注解需要asEagerSingleton
     *
     * @return
     */
    public static WordnetTokenizerFactory get() {
        return MynlpInjector.getInjector().getInstance(WordnetTokenizerFactory.class);
    }

    public WordnetTokenizer build(String initer, String bestpath, String pipeline, PipelineSettings settings) {

        WordnetTokenizer instance = injector.getInstance(WordnetTokenizer.class);

        WordnetInitializer w = registry.getInstance(initer, WordnetInitializer.class);
        BestPathComputer b = registry.getInstance(bestpath, BestPathComputer.class);
        Pipeline pipeline1 = pipelineFactory.createByName(pipeline, settings);
        instance.setBestPathComputer(b);
        instance.setWordnetInitializer(w);
        instance.setPipeline(pipeline1);

        return instance;
    }

    public WordnetTokenizer build(String initer, String bestpath, String pipeline) {
        return build(initer, bestpath, pipeline, PipelineSettings.EMTPY);
    }

    public WordnetTokenizer buildDefault() {
        return build("core", "viterbi", "default");
    }

}

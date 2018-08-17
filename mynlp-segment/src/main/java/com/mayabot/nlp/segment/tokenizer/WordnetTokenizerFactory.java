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
import com.mayabot.nlp.Settings;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.segment.ComponentRegistry;
import com.mayabot.nlp.segment.WordnetInitializer;
import com.mayabot.nlp.segment.wordnet.BestPathComputer;
import com.mayabot.nlp.segment.wordnet.ViterbiBestPathComputer;

/**
 * 根据JSON配置文件产生一个WordnetTokenizer对象
 *
 * @author jimichan
 */
@Singleton
public class WordnetTokenizerFactory {

    public static final String TOKENIZER_INITER = "tokenizer.initer";
    public static final String TOKENIZER_BESTPATH = "tokenizer.bestpath";
    static InternalLogger logger = InternalLoggerFactory.getInstance(WordnetTokenizerFactory.class);

    private final ComponentRegistry registry;
    private final Injector injector;
    private final PipelineFactory pipelineFactory;
    private final Settings settings;

    @Inject
    WordnetTokenizerFactory(ComponentRegistry registry, Injector injector, PipelineFactory pipelineFactory, Settings settings) {
        this.registry = registry;
        this.injector = injector;
        this.pipelineFactory = pipelineFactory;
        this.settings = settings;
    }

    /**
     * Just building 延迟初始化，所以向NamedComponentRegistry注册新的注解需要asEagerSingleton
     *
     * @return
     */
    public static WordnetTokenizerFactory instance() {
        //FIXME 为了编译 先返回null
        return null;

        //return MynlpInjector.getInjector().getInstance(WordnetTokenizerFactory.class);
    }

    public WordnetTokenizer build(PipelineDefine pipelineDefine, Settings settings) {

        settings = Settings.merge(this.settings, settings);

        String initer = settings.get(TOKENIZER_INITER, ComponentRegistry.WORDNET_INITER_CORE);
        String bestpath = settings.get(TOKENIZER_BESTPATH, ViterbiBestPathComputer.NAME);

        WordnetTokenizer instance = injector.getInstance(WordnetTokenizer.class);

        WordnetInitializer w = registry.getInstance(initer, WordnetInitializer.class);
        BestPathComputer b = registry.getInstance(bestpath, BestPathComputer.class);
        Pipeline pipeline = pipelineFactory.create(pipelineDefine, settings);

        instance.prepare(pipeline, b, w);
        instance.check();

        return instance;
    }

    public WordnetTokenizer buildDefault() {
        return build(PipelineDefine.defaultPipeline, Settings.createEmpty());
    }

}

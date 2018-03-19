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

package com.mayabot.nlp.segment;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.segment.wordnet.ViterbiBestPathComputer;
import com.mayabot.nlp.segment.recognition.org.OrganizationRecognition;
import com.mayabot.nlp.segment.recognition.personname.PersonRecognition;
import com.mayabot.nlp.segment.recognition.place.PlaceRecognition;
import com.mayabot.nlp.segment.wordnet.BestPathComputer;
import com.mayabot.nlp.segment.wordnetiniter.*;
import com.mayabot.nlp.segment.wordprocessor.*;

import java.util.Map;
import java.util.function.Function;

import static com.mayabot.nlp.segment.ComponentNames.*;

/**
 * 注册wordnet 分词器的组件
 * @author jimichan
 */
@Singleton
public final class ComponentRegistry {

    public static final String WORDNET_INITER_CORE = "core";
    public static final String WORDNET_INITER_CRF = "crf";

    static InternalLogger logger = InternalLoggerFactory.getInstance(ComponentRegistry.class);

    private final Injector injector;

    private Table<String, Class, Function> table = HashBasedTable.create();

    @Inject
    public ComponentRegistry(Injector injector) {
        this.injector = injector;
        initDefaultComponents();
    }

    public <T> void register(String name, Class<T> clazz, Function<Injector, ? extends T> factory) {
        table.put(name, clazz, factory);
        logger.info("register {} for class {}", name, clazz);
    }

    public <T> void register(ComponentNames name, Class<T> clazz, Function<Injector, ? extends T> factory) {
        table.put(name.name(), clazz, factory);
        logger.info("register {} for class {}", name, clazz);
    }

    public <T> Function<Injector, T> getFactory(String name, Class<T> clazz) {
        return table.get(name, clazz);
    }

    public <T> T getInstance(String name, Class<T> clazz) {
        Function<Injector, T> fun = getFactory(name, clazz);
        Preconditions.checkNotNull(fun, "Not found name " + name + " class " + clazz);
        T apply = fun.apply(injector);
        Preconditions.checkNotNull(apply);
        return apply;
    }

    public Map<String, Function> getFactoryByClass(Class clazz) {
        return ImmutableMap.copyOf(table.column(clazz));
    }


    private void initDefaultComponents() {
        register(ViterbiBestPathComputer.name, BestPathComputer.class, i -> i.getInstance(ViterbiBestPathComputer.class));

        //wordnet initer
        register(WORDNET_INITER_CRF, WordnetInitializer.class, inject -> new MultiWordnetInit(
                inject.getInstance(CrfOriginalSegment.class),
                inject.getInstance(AtomSegmenter.class),
                inject.getInstance(ConvertAbstractWord.class)
        ));

        register(WORDNET_INITER_CORE, WordnetInitializer.class, inject -> new MultiWordnetInit(
                inject.getInstance(CoreDictionaryOriginalSegment.class),
                inject.getInstance(AtomSegmenter.class),
                inject.getInstance(ConvertAbstractWord.class)
        ));

        //recognition
        register(place, OptimizeProcessor.class, PlaceRecognition::build);
        register(person, OptimizeProcessor.class, PersonRecognition::build);
        register(organization, OptimizeProcessor.class, OrganizationRecognition::build);

        // Wordpath process
        register(subindex, WordpathProcessor.class, instanceOf(new IndexSubwordsProcess()));
        register(mq, WordpathProcessor.class, injectorGetInstance(MergeNumberQuantifierPreProcessor.class));
        register(ml, WordpathProcessor.class, injectorGetInstance(MergeNumberAndLetterPreProcess.class));
        register(speechTagging, WordpathProcessor.class, injectorGetInstance(PartOfSpeechTaggingComputerXProcessor.class));
        register(correction, WordpathProcessor.class, injectorGetInstance(CorrectionXProcessor.class));
        register(customDict, WordpathProcessor.class, injectorGetInstance(CustomDictionaryXProcess.class));
        register(pattern, WordpathProcessor.class, instanceOf(new CommonPatternWordPathProcessor()));
        register(repairWordnet, WordpathProcessor.class, injectorGetInstance(RepairWordnetProcessor.class));

        register("optimizeNet", WordpathProcessor.class, injectorGetInstance(OptimizeWordPathProcessor.class));
    }

    private <T> Function<Injector, T> injectorGetInstance(Class<T> clazz) {
        return injector -> injector.getInstance(clazz);
    }

    private <T> Function<Injector, T> instanceOf(T  obj) {
        return injector -> obj;
    }

}

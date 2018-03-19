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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.Settings;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.segment.ComponentRegistry;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.wordprocessor.OptimizeWordPathProcessor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author jimichan
 */
@Singleton
public class PipelineFactory {

    private ComponentRegistry registry;

    private Settings globalSettings;

    @Inject
    public PipelineFactory(ComponentRegistry registry, Settings globalSettings) {
        this.registry = registry;
        this.globalSettings = globalSettings;
    }

    static InternalLogger logger = InternalLoggerFactory.getInstance(PipelineFactory.class);

    public Pipeline create(PipelineDefine pipelineDefine, Settings settings) {

        //合并全局的默认设置
        settings = Settings.merge(globalSettings, settings);

        //所有的配置使用pipeline前缀
        settings = settings.getByPrefix("pipeline");


        //默认启用所有的Node
        Set<String> enableSets = pipelineDefine.allNodeNames();

        //pipeline.disable 配置项配置禁用的节点
        Set<String> disableNames = Sets.newHashSet(settings.getAsList("disable"));

        logger.debug("PipelineFactory disableNames " + disableNames);

        disableNames.forEach(name -> enableSets.remove(name));

        List<WordpathProcessor> wordPathProcessors = Lists.newArrayList();

        for (Object obj : pipelineDefine.getProcessorNames()) {
            if (obj instanceof String) {
                if (!enableSets.contains(disableNames)) {
                    continue;
                }
                String name = ((String) obj);
                WordpathProcessor instance = registry.getInstance(name, WordpathProcessor.class);
                wordPathProcessors.add(instance);
                instance.setName(name);

            } else if (obj instanceof List) {
                List<String> names = ((List<String>) obj);

                names = names.stream().filter(x -> enableSets.contains(x)).collect(Collectors.toList());

                OptimizeWordPathProcessor p = (OptimizeWordPathProcessor) registry.getInstance("optimizeNet", WordpathProcessor.class);
                p.initOptimizeProcessor(names);

                wordPathProcessors.add(p);

            } else {
                Preconditions.checkState(false, "obj %s format is error", obj.toString());
            }
        }

        for (WordpathProcessor wordPathProcessor : wordPathProcessors) {
            String name = wordPathProcessor.getName();
            Settings partSettings = settings;
            if (name != null) {
                partSettings = settings.getByPrefix(name + ".");
            }
            if (wordPathProcessor instanceof TokenizerSettingListener) {
                ((TokenizerSettingListener) wordPathProcessor).apply(partSettings);
            }
        }


        return new Pipeline(wordPathProcessors);
    }

}

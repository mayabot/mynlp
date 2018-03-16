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

package com.mayabot.nlp.segment.xprocessor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.mayabot.nlp.segment.NamedComponentRegistry;
import com.mayabot.nlp.segment.OptimizeProcessor;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.tokenizer.ApplyPipelineSetting;
import com.mayabot.nlp.segment.tokenizer.PipelineSettings;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.List;
import java.util.Map;

/**
 * 优化网络处理器
 */
public class OptimizeWordPathProcessor implements WordpathProcessor, ApplyPipelineSetting {

    private final NamedComponentRegistry registry;

    private List<OptimizeProcessor> optimizeProcessorList = Lists.newArrayList();
    WordpathProcessor repairWordnet;

    @Inject
    public OptimizeWordPathProcessor(NamedComponentRegistry registry) {
        this.registry = registry;
    }

    public void initConfig(Map<String, Object> map) {
        List<String> list = (List) map.get("list");

        Preconditions.checkArgument(!list.isEmpty(), "");

        repairWordnet = registry.getInstance("repairWordnet", WordpathProcessor.class);

        for (String name : list) {
            OptimizeProcessor pr = registry.getInstance(name, OptimizeProcessor.class);
            Preconditions.checkNotNull(pr, "Not found OptimizeProcessor " + name);
            optimizeProcessorList.add(pr);
        }
    }

    @Override

    public Wordpath process(Wordpath wordPath) {

        Wordnet wordnet = wordPath.getWordnet();

        //之前流水线上的处理器，有可能把wordpath截断，造成wordnet不匹配。这里要修复尝试
        repairWordnet.process(wordPath);

        //标记处优化网络
        wordnet.setOptimizeNet(true);
        wordnet.tagOptimizeNetVertex(wordPath);

        Vertex[] pathWithBE = new Vertex[wordPath.wordCount() + 2];
        int i = 0;
        for (Vertex v : wordPath.getBestPathWithBE()) {
            pathWithBE[i++] = v;
        }

        boolean change = false;


        for (OptimizeProcessor processor : optimizeProcessorList) {
            boolean ch = processor.process(pathWithBE, wordnet);
            if (ch) {
                change = true;
            }
        }

        if (change) {
            wordPath = wordPath.getBestPathComputer().select(wordnet);
            repairWordnet.process(wordPath);
        }

        wordnet.setOptimizeNet(false);

        return wordPath;
    }

    @Override
    public void apply(PipelineSettings settings) {
        for (OptimizeProcessor optimizeProcessor : optimizeProcessorList) {
            if (optimizeProcessor instanceof ApplyPipelineSetting) {
                ((ApplyPipelineSetting) optimizeProcessor).apply(settings);
            }
        }
    }
}


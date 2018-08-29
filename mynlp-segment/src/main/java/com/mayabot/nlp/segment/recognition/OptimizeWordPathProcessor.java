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

package com.mayabot.nlp.segment.recognition;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.mayabot.nlp.segment.OptimizeProcessor;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.common.BaseMynlpComponent;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

/**
 * 优化网络处理器
 * @author jimichan
 */
public class OptimizeWordPathProcessor extends BaseMynlpComponent implements WordpathProcessor {

    private ImmutableList<OptimizeProcessor> optimizeProcessorList = ImmutableList.of();

    private RepairWordnetProcessor repairWordnet;

    @Inject
    public OptimizeWordPathProcessor(RepairWordnetProcessor repairWordnet) {
        this.repairWordnet = repairWordnet;
    }

    public void addOptimizeProcessor(OptimizeProcessor op) {
        optimizeProcessorList =
                ImmutableList.<OptimizeProcessor>builder()
                        .addAll(optimizeProcessorList).add(op).build();
    }

    public void addAllOptimizeProcessor(Iterable<? extends OptimizeProcessor> ops) {
        optimizeProcessorList =
                ImmutableList.<OptimizeProcessor>builder()
                        .addAll(optimizeProcessorList)
                        .addAll(ops).build();
    }

    @Override

    public Wordpath process(Wordpath wordPath) {

        if (optimizeProcessorList.isEmpty()) {
            return wordPath;
        }

        Wordnet wordnet = wordPath.getWordnet();

        //之前流水线上的处理器，有可能把wordpath截断，造成wordnet不匹配。这里要修复尝试
        repairWordnet.process(wordPath);

        Vertex[] pathWithBE = new Vertex[wordPath.wordCount() + 2];
        {
            int i = 0;
            for (Vertex v : wordPath.getBestPathWithBE()) {
                pathWithBE[i++] = v;
            }
        }

        //标记处优化网络
        wordnet.setOptimizeNet(true);
        wordnet.tagOptimizeNetVertex(pathWithBE);

        boolean change = false;

        for (OptimizeProcessor processor : optimizeProcessorList) {
            if (processor.isEnabled()) {
                change |= processor.process(pathWithBE, wordnet);
            }
        }

        if (change) {
            wordPath = wordPath.getBestPathComputer().select(wordnet);
            repairWordnet.process(wordPath);
        }

        wordnet.setOptimizeNet(false);

        return wordPath;
    }

    public ImmutableList<OptimizeProcessor> getOptimizeProcessorList() {
        return optimizeProcessorList;
    }
}


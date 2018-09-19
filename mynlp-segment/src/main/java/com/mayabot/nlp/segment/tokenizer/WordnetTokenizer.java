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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.*;
import com.mayabot.nlp.segment.common.VertexHelper;
import com.mayabot.nlp.segment.tokenizer.recognition.OptimizeWordPathProcessor;
import com.mayabot.nlp.segment.wordnet.BestPathAlgorithm;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 一个基于词图的流水线 要求里面所有的组件都是无状态的，线程安全的类
 *
 * @author jimichan
 */
public class WordnetTokenizer implements MynlpTokenizer {

    public static WordnetTokenizerBuilder builder() {
        return new WordnetTokenizerBuilder();
    }

    /**
     * 当wordnet创建后，调用这些处理器来填充里面的节点
     */
    private WordnetInitializer[] initer;

    /**
     * 处理器网络
     */
    private WordpathProcessor[] pipeline;

    private BestPathAlgorithm bestPathAlgorithm;

    private WordTermCollector collector;

    private VertexHelper vertexHelper;

    private CharNormalize[] charNormalizes;

    WordnetTokenizer(List<WordnetInitializer> initer,
                     WordpathProcessor[] pipeline,
                     BestPathAlgorithm bestPathAlgorithm,
                     WordTermCollector termCollector,
                     List<CharNormalize> charNormalizes,
                     VertexHelper vertexHelper) {
        this.initer = initer.toArray(new WordnetInitializer[0]);
        this.pipeline = pipeline;
        this.bestPathAlgorithm = bestPathAlgorithm;
        this.collector = termCollector;
        this.vertexHelper = vertexHelper;
        this.charNormalizes = charNormalizes.toArray(new CharNormalize[0]);

        Preconditions.checkNotNull(bestPathAlgorithm);
        Preconditions.checkNotNull(this.initer);
        Preconditions.checkNotNull(pipeline);
        Preconditions.checkArgument(pipeline.length != 0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WordnetTokenizer\n\n");
        sb.append("BestPathAlgorithm = " + bestPathAlgorithm.getClass().getSimpleName()).append("\n");
        sb.append("CharNormalize = " + Joiner.on(",").join(Lists.newArrayList(charNormalizes).stream().map(it -> it.getClass().getSimpleName()).collect(Collectors.toList()))).append("\n");
        sb.append("WordTermCollector = " + collector.getClass().getSimpleName() + "\n");

        sb.append("WordnetInitializer = " + Joiner.on(",").join(Lists.newArrayList(initer).stream().map(it -> it.getClass().getSimpleName()).collect(Collectors.toList()))).append("\n");
        sb.append("WordpathProcessor = \n");
        for (WordpathProcessor processor : pipeline) {
            if (processor instanceof OptimizeWordPathProcessor) {
                OptimizeWordPathProcessor xp = (OptimizeWordPathProcessor) processor;
                sb.append("\t" + xp.getOptimizeProcessorList().stream().map(it -> it.getClass().getSimpleName()).collect(Collectors.toList()));
                sb.append("\n");
            } else {
                sb.append("\t" + processor.getClass().getSimpleName()).append("\n");
            }
        }
        System.out.println("\n");


        return sb.toString();
    }

    @Override
    public void token(char[] text, Consumer<WordTerm> consumer) {

        if (charNormalizes != null) {
            for (CharNormalize normalize : charNormalizes) {
                normalize.normal(text);
            }
        }

        // 处理为空的特殊情况
        if (text.length == 0) {
            return;
        }

        //构建一个空的Wordnet对象
        final Wordnet wordnet = new Wordnet(text);
        wordnet.setBestPathAlgorithm(bestPathAlgorithm);

        wordnet.getBeginRow().put(vertexHelper.newBegin());
        wordnet.getEndRow().put(vertexHelper.newEnd());

        for (WordnetInitializer initializer : initer) {
            initializer.fill(wordnet);
        }

        //选择一个路径出来(第一次不严谨的分词结果)
        Wordpath wordPath = bestPathAlgorithm.select(wordnet);

        for (WordpathProcessor processor : pipeline) {
            if (processor.isEnabled()) {
                wordPath = processor.process(wordPath);
            }
        }

        collector.collect(wordnet, wordPath, consumer);
    }


    public List<WordpathProcessor> getPipeline() {
        return ImmutableList.copyOf(pipeline);
    }

    public WordTermCollector getCollector() {
        return collector;
    }

}

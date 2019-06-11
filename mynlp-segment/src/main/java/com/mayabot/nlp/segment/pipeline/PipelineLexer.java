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

package com.mayabot.nlp.segment.pipeline;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.*;
import com.mayabot.nlp.segment.plugins.collector.WordTermCollector;
import com.mayabot.nlp.segment.wordnet.BestPathAlgorithm;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;
import com.mayabot.nlp.utils.Characters;
import com.mayabot.nlp.utils.StringUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 一个基于流水线的分词器架构。
 * 通过PipelineTokenizer可以柔和复用各种分词算法。
 * 要求里面所有的组件都是无状态的，线程安全的类
 *
 * @author jimichan
 */
public class PipelineLexer implements Lexer {

    private BestPathAlgorithm bestPathAlgorithm;

    private WordTermCollector collector;

    private CharNormalize[] charNormalizes;

    /**
     * 当wordnet创建后，调用这些处理器来填充里面的节点
     */
    private WordSplitAlgorithm[] initer;

    /**
     * 处理器网络
     */
    private WordpathProcessor[] pipeline;

    public static PipelineLexerBuilder builder() {
        return new PipelineLexerBuilder();
    }

    PipelineLexer(List<WordSplitAlgorithm> initer,
                  WordpathProcessor[] pipeline,
                  BestPathAlgorithm bestPathAlgorithm,
                  WordTermCollector termCollector,
                  List<CharNormalize> charNormalizes) {
        this.initer = initer.toArray(new WordSplitAlgorithm[0]);
        this.pipeline = pipeline;
        this.bestPathAlgorithm = bestPathAlgorithm;
        this.collector = termCollector;
        this.charNormalizes = charNormalizes.toArray(new CharNormalize[0]);

        Preconditions.checkNotNull(bestPathAlgorithm);
        Preconditions.checkNotNull(this.initer);
        Preconditions.checkNotNull(pipeline);
    }

    @Override
    public void scan(char[] text, Consumer<WordTerm> consumer) {

        if (charNormalizes != null) {
            for (CharNormalize normalize : charNormalizes) {
                normalize.normal(text);
            }
        }

        // 处理为空的特殊情况
        if (text.length == 0) {
            return;
        }

        //处理单子的情况
        if (text.length == 1 && StringUtils.isWhiteSpace(text[0])) {
            if (StringUtils.isWhiteSpace(text[0]) || Characters.isPunctuation(text[0])) {
                WordTerm wordTerm = new WordTerm(new String(text), Nature.w);
                consumer.accept(wordTerm);
            } else {
                WordTerm wordTerm = new WordTerm(new String(text), Nature.x);
                consumer.accept(wordTerm);
            }
            return;
        }

        //构建一个空的Wordnet对象
        final Wordnet wordnet = new Wordnet(text);

        for (WordSplitAlgorithm initializer : initer) {
            initializer.fill(wordnet);
        }

        // 对WordNet进行补齐,避免意外的错误
        wordnet.fillNill();

//      System.out.println(wordnet.toMoreString());

        //选择一个路径出来
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PipelineTokenizer\n\n");
        sb.append("BestPathAlgorithm = " + bestPathAlgorithm.getClass().getSimpleName()).append("\n");
        sb.append("CharNormalize = " + Joiner.on(",").join(Lists.newArrayList(charNormalizes).stream().map(it -> it.getClass().getSimpleName()).collect(Collectors.toList()))).append("\n");
        sb.append("WordTermCollector = " + collector.getClass().getSimpleName() + "\n");

        sb.append("WordSplitAlgorithm = " + Joiner.on(",").join(Lists.newArrayList(initer).stream().map(it -> it.getClass().getSimpleName()).collect(Collectors.toList()))).append("\n");
        sb.append("WordpathProcessor = \n");
        for (WordpathProcessor processor : pipeline) {
            sb.append("\t" + processor.getClass().getSimpleName()).append("\n");
        }
        return sb.toString();
    }

}

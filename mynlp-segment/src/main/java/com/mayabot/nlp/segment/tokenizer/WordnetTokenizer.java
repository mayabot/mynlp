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
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.segment.*;
import com.mayabot.nlp.segment.common.VertexHelper;
import com.mayabot.nlp.segment.wordnet.BestPathComputer;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.List;

/**
 * 一个基于词图的流水线 要求里面所有的组件都是无状态的，线程安全的类
 *
 * @author jimichan
 */
public class WordnetTokenizer implements MynlpTokenizer {


    private static InternalLogger logger = InternalLoggerFactory.getInstance(WordnetTokenizer.class);

    /**
     * 当wordnet创建后，调用这些处理器来填充里面的节点
     */
    WordnetInitializer wordnetInitializer = null;

    /**
     * 处理器网络
     */
    List<WordpathProcessor> pipeline;

    BestPathComputer bestPathComputer;

    private MynlpTermCollector termCollector = MynlpTermCollector.bestPath;

    private VertexHelper vertexHelper;

    @Inject
    WordnetTokenizer(
            VertexHelper vertexHelper) {
        this.vertexHelper = vertexHelper;
    }

    public void check() {
        Preconditions.checkNotNull(bestPathComputer);
        Preconditions.checkNotNull(wordnetInitializer);
        Preconditions.checkNotNull(pipeline);
        Preconditions.checkArgument(!pipeline.isEmpty());
    }

    @Override
    public void token(char[] text, List<MynlpTerm> target) {

        if (!target.isEmpty()) {
            target.clear();
        }

        // 处理为空的特殊情况
        if (text.length == 0) {
            return;
        }

        //构建一个空的Wordnet对象
        final Wordnet wordnet = initEmptyWordNet(text);

        wordnetInitializer.init(wordnet);


        //选择一个路径出来(第一次不严谨的分词结果)
        Wordpath wordPath = bestPathComputer.select(wordnet);

        wordPath = process(wordPath);

        termCollector.collect(wordnet, wordPath, target);


    }

    private Wordpath process(Wordpath wordPath) {
        for (WordpathProcessor processor : pipeline) {
            if (processor.isEnabled()) {
                wordPath = processor.process(wordPath);
            }
        }
        return wordPath;
    }


    /**
     * 模板方法，初始化产生一个词网(Wordnet)
     *
     * @param text
     * @return
     */
    private Wordnet initEmptyWordNet(char[] text) {
        Wordnet wordnet = new Wordnet(text);
        wordnet.getBeginRow().put(vertexHelper.newBegin());
        wordnet.getEndRow().put(vertexHelper.newEnd());
        return wordnet;
    }

    public List<WordpathProcessor> getPipeline() {
        return ImmutableList.copyOf(pipeline);
    }

    public MynlpTermCollector getTermCollector() {
        return termCollector;
    }

    public void setTermCollector(MynlpTermCollector termCollector) {
        this.termCollector = termCollector;
    }
}

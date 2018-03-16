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
import com.google.inject.Inject;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.segment.MynlpTerm;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.WordnetInitializer;
import com.mayabot.nlp.segment.common.VertexHelper;
import com.mayabot.nlp.segment.wordnet.BestPathComputer;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.Iterator;
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
    private WordnetInitializer wordnetInitializer = null;

    /**
     * 处理器网络
     */
    private Pipeline pipeline;

    private BestPathComputer bestPathComputer;

    private VertexHelper vertexHelper;

    @Inject
    WordnetTokenizer(
            VertexHelper vertexHelper) {
        this.vertexHelper = vertexHelper;
    }


    public void check() {
        Preconditions.checkNotNull(bestPathComputer);
        Preconditions.checkNotNull(wordnetInitializer);
    }

    public void initSetting(PipelineSettings settings) {

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

        wordnetInitializer.initialize(wordnet);

        //选择一个路径出来(第一次不严谨的分词结果)
        Wordpath wordPath = bestPathComputer.select(wordnet);

        wordPath = pipeline.process(wordPath);

        path2TermList(wordPath, target);
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


    /**
     * 把PATH转换为List<MyTerm>结果。 PATH中 #Start #End 需要去除
     *
     * @param wordPath
     * @return
     */
    protected void path2TermList(Wordpath wordPath, List<MynlpTerm> target) {
        Iterator<Vertex> vertexIterator = wordPath.iteratorBestPath();
        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();

            MynlpTerm term = new MynlpTerm(vertex.realWord(), vertex.guessNature());
            term.setOffset(vertex.getRowNum());

            if (vertex.subWords != null) {
                term.setSubword(Lists.newArrayListWithCapacity(vertex.subWords.size()));
                for (Vertex subWord : vertex.subWords) {
                    MynlpTerm sub = new MynlpTerm(subWord.realWord(), null);
                    sub.setOffset(subWord.getRowNum());
                    term.getSubword().add(sub);
                }
            }


            target.add(term);
        }
    }

    public WordnetInitializer getWordnetInitializer() {
        return wordnetInitializer;
    }

    void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    void setWordnetInitializer(WordnetInitializer wordnetInitializer) {
        this.wordnetInitializer = wordnetInitializer;
    }


    public BestPathComputer getBestPathComputer() {
        return bestPathComputer;
    }

    /**
     * 设置最优路径选择器
     *
     * @param bestPathComputer
     */
    void setBestPathComputer(BestPathComputer bestPathComputer) {
        this.bestPathComputer = bestPathComputer;
    }


    public Pipeline getPipeline() {
        return pipeline;
    }
}

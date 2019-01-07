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
import com.google.common.collect.Lists;
import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.common.DefaultCharNormalize;
import com.mayabot.nlp.segment.plugins.collector.SentenceCollector;
import com.mayabot.nlp.segment.plugins.collector.SentenceIndexWordCollector;
import com.mayabot.nlp.segment.wordnet.BestPathAlgorithm;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * WordnetTokenizer构建器
 * <p>
 * 调用WordnetTokenizer.builder()来创建WordnetTokenizerBuilder对象
 *
 * @author jimichan
 */
public class PipelineTokenizerBuilder implements MynlpTokenizerBuilder {

    private final Mynlp mynlp;

    /**
     * 词图最优路径选择器
     */
    private BestPathAlgorithm bestPathAlgorithm;

    /**
     * 字符处理器,默认就有最小化和全角半角化
     */
    private List<CharNormalize> charNormalizes = Lists.newArrayList(
            DefaultCharNormalize.instace
    );

    /**
     * 切词器管线
     */
    private LinkedList<WordSplitAlgorithm> wordSplitAlgorithm = Lists.newLinkedList();

    /**
     * 逻辑管线
     */
    private LinkedList<WordpathProcessor> pipeLine = Lists.newLinkedList();

    /**
     * 保存后置监听器逻辑
     */
    private List<ConsumerPair> configListener = Lists.newArrayList();

    /**
     * 最终结构收集器
     */
    private WordTermCollector termCollector;

    /**
     * 默认构造函数
     */
    public PipelineTokenizerBuilder() {
        this.mynlp = Mynlps.get();
    }

    protected void setUp() {

    }


    /**
     * 默认情况下，是否开启字词模式
     */
    private boolean enableIndexModel = false;

    @Override
    public MynlpTokenizer build() {

        setUp();

        // 1. bestPathAlgorithm
        Preconditions.checkNotNull(bestPathAlgorithm);

        // 2. WordSplitAlgorithm
        Preconditions.checkState(!wordSplitAlgorithm.isEmpty());

        // 3.termCollector
        if (termCollector == null) {
            if (enableIndexModel) {
                termCollector = mynlp.getInstance(SentenceIndexWordCollector.class);
            } else {
                termCollector = mynlp.getInstance(SentenceCollector.class);
            }
        }

        // 4
        callListener();

        Collections.sort(wordSplitAlgorithm);
        Collections.sort(pipeLine);

        return new PipelineTokenizer(
                Lists.newArrayList(wordSplitAlgorithm),
                pipeLine.toArray(new WordpathProcessor[0]),
                bestPathAlgorithm
                , termCollector,
                this.charNormalizes);
    }

    /**
     * 调用后置监听器
     */
    private void callListener() {

        //WordTermCollector
        configListener.forEach(pair -> {
            if (pair.clazz.equals(termCollector.getClass()) ||
                    pair.clazz.isAssignableFrom(termCollector.getClass())) {
                pair.consumer.accept(termCollector);
            }
        });

        //wordSplitAlgorithm
        configListener.forEach(pair -> {
            wordSplitAlgorithm.forEach(wf -> {
                if (pair.clazz.equals(wf.getClass()) ||
                        pair.clazz.isAssignableFrom(wf.getClass())) {
                    pair.consumer.accept(wf);
                }
            });

        });

        // pipeLine
        configListener.forEach(pair -> {
            pipeLine.forEach(it -> {
                if (pair.clazz.equals(it.getClass()) || pair.clazz.isAssignableFrom(it.getClass())) {
                    pair.consumer.accept(it);
                }
            });
        });

    }

    /**
     * 设定针对WordpathProcessor，WordSplitAlgorithm，WordTermCollector等组件后置逻辑。
     * 通过这个方法可以已经创建的组件进行配置
     *
     * @param clazz
     * @param listener
     * @param <T>
     * @return
     */
    public <T> PipelineTokenizerBuilder config(Class<T> clazz, Consumer<T> listener) {
        configListener.add(new ConsumerPair(clazz, listener));
        return this;
    }

    /**
     * 关闭组件
     *
     * @param clazz
     * @return
     */
    public PipelineTokenizerBuilder disabledComponent(Class<? extends SegmentComponent> clazz) {
        config(clazz, x -> x.disable());
        return this;
    }

    /**
     * 启用组件
     *
     * @param clazz
     * @return
     */
    public PipelineTokenizerBuilder enableComponent(Class<? extends SegmentComponent> clazz) {
        config(clazz, x -> x.enable());
        return this;
    }


    /**
     * 添加CharNormalize
     *
     * @param charNormalizeClass 通过Guice来初始化该对象
     * @return self
     */
    public PipelineTokenizerBuilder addCharNormalize(Class<? extends CharNormalize> charNormalizeClass) {
        this.charNormalizes.add(mynlp.getInstance(charNormalizeClass));
        return this;
    }

    /**
     * 添加CharNormalize
     *
     * @param charNormalize
     * @return self
     */
    public PipelineTokenizerBuilder addCharNormalize(CharNormalize charNormalize) {
        this.charNormalizes.add(charNormalize);
        return this;
    }

    /**
     * 移除CharNormalize
     *
     * @param clazz
     * @return
     */
    public PipelineTokenizerBuilder removeCharNormalize(Class<? extends CharNormalize> clazz) {
        this.charNormalizes.removeIf(obj -> clazz.isAssignableFrom(obj.getClass()) || obj.getClass().equals(clazz));
        return this;
    }

    /**
     * 设置BestPathComputer的实现对象
     *
     * @param bestPathAlgorithm
     * @return
     */
    public PipelineTokenizerBuilder setBestPathAlgorithm(BestPathAlgorithm bestPathAlgorithm) {
        this.bestPathAlgorithm = bestPathAlgorithm;
        return this;
    }

    /**
     * 设置BestPathComputer的实现类，有Guice创建对象
     *
     * @param clazz
     * @return
     */
    public PipelineTokenizerBuilder setBestPathComputer(Class<? extends BestPathAlgorithm> clazz) {
        this.bestPathAlgorithm = mynlp.getInstance(clazz);
        return this;
    }

    /**
     * 增加一个WordpathProcessor实现对象
     *
     * @param processor
     * @return
     */
    public PipelineTokenizerBuilder addProcessor(WordpathProcessor processor) {
        pipeLine.add(processor);
        Collections.sort(pipeLine);
        return this;
    }


    /**
     * 增加一个WordpathProcessor实现类
     *
     * @param clazz
     * @return
     */
    public PipelineTokenizerBuilder addProcessor(Class<? extends WordpathProcessor> clazz) {
        addProcessor(mynlp.getInstance(clazz));
        return this;
    }


    /**
     * 增加WordnetInitializer对象
     *
     * @param algorithm
     * @return
     */
    public PipelineTokenizerBuilder addWordSplitAlgorithm(WordSplitAlgorithm algorithm) {

        this.wordSplitAlgorithm.add(algorithm);

        Collections.sort(wordSplitAlgorithm);
        return this;
    }

    /**
     * 增加WordnetInitializer
     *
     * @param algorithm Class
     * @return
     */
    public PipelineTokenizerBuilder addWordSplitAlgorithm(Class<? extends WordSplitAlgorithm> algorithm) {
        addWordSplitAlgorithm(mynlp.getInstance(algorithm));
        return this;
    }

    /**
     * 设置分词结果收集器
     *
     * @param termCollector
     * @return
     */
    public PipelineTokenizerBuilder setTermCollector(WordTermCollector termCollector) {
        this.termCollector = termCollector;
        return this;
    }

    /**
     * 设置分词结果收集器
     *
     * @param termCollectorClass
     * @return
     */
    public PipelineTokenizerBuilder setTermCollector(Class<? extends WordTermCollector> termCollectorClass) {
        this.termCollector = mynlp.getInstance(termCollectorClass);
        return this;
    }

    public WordTermCollector getTermCollector() {
        return termCollector;
    }

    public SentenceIndexWordCollector getIndexTermCollector() {
        return (SentenceIndexWordCollector) termCollector;
    }

    private static class ConsumerPair {
        Class clazz;
        Consumer consumer;

        public ConsumerPair(Class clazz, Consumer consumer) {
            this.clazz = clazz;
            this.consumer = consumer;
        }
    }

    public boolean isEnableIndexModel() {
        return enableIndexModel;
    }

    public PipelineTokenizerBuilder setEnableIndexModel(boolean enableIndexModel) {
        this.enableIndexModel = enableIndexModel;
        return this;
    }

}

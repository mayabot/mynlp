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
import com.mayabot.nlp.segment.hmmner.OptimizeProcessor;
import com.mayabot.nlp.segment.hmmner.OptimizeWordPathProcessor;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceCollector;
import com.mayabot.nlp.segment.tokenizer.normalize.DefaultCharNormalize;
import com.mayabot.nlp.segment.wordnet.BestPathAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
     * 词图初始填充器
     */
    private List<WordnetInitializer> wordnetInitializer = Lists.newArrayList();

    /**
     * 分词逻辑管线
     */
    private ArrayList<WordpathProcessor> pipeLine = Lists.newArrayList();

    /**
     * 保存后置监听器逻辑
     */
    private List<ConsumerPair> configListener = Lists.newArrayList();

    /**
     * 最终结构收集器
     */
    WordTermCollector termCollector;

    /**
     * 默认构造函数。不公开
     */
    public PipelineTokenizerBuilder() {
        this.mynlp = Mynlps.get();
    }

    @Override
    public MynlpTokenizer build() {

        // 1. bestPathAlgorithm
        Preconditions.checkNotNull(bestPathAlgorithm);

        // 2. WordnetInitializer
        Preconditions.checkState(!wordnetInitializer.isEmpty());

        // 3.termCollector
        if (termCollector == null) {
            termCollector = mynlp.getInstance(SentenceCollector.class);
        }

        // 4
        callListener();

        Collections.sort(wordnetInitializer);
        Collections.sort(pipeLine);

        return new PipelineTokenizer(
                wordnetInitializer,
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

        //wordnetInitializer
        configListener.forEach(pair -> {
            wordnetInitializer.forEach(wf -> {
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

                if (it instanceof OptimizeWordPathProcessor) {
                    OptimizeWordPathProcessor op = (OptimizeWordPathProcessor) it;
                    op.getOptimizeProcessorList().forEach(pp -> {
                        if (pair.clazz.equals(pp.getClass()) || pair.clazz.isAssignableFrom(pp.getClass())) {
                            pair.consumer.accept(pp);
                        }
                    });
                }
            });
        });

    }

    /**
     * 设定针对WordpathProcessor，WordnetInitializer，WordTermCollector等组件后置逻辑。
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
     * 增加一个WordpathProcessor组件或者WordnetInitializer组件。或者同时实现了这两个接口.
     *
     * @param component SegmentComponent
     * @return self
     */
    public PipelineTokenizerBuilder addComponent(SegmentComponent component) {
        boolean access = false;
        if (component instanceof WordpathProcessor) {
            access = true;
            addProcessor((WordpathProcessor) component);
        }

        if (component instanceof WordnetInitializer) {
            access = true;
            addWordnetInitializer((WordnetInitializer) component);
        }

        if (!access) {
            throw new RuntimeException("Not supoort " + component.getClass());
        }

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
        pipeLine.add(mynlp.getInstance(clazz));
        Collections.sort(pipeLine);
        return this;
    }

    /**
     * 增加一组OptimizeProcessor实现
     *
     * @param ops
     * @return
     */
    public PipelineTokenizerBuilder addOptimizeProcessor(
            List<? extends OptimizeProcessor> ops,
            int order) {
        OptimizeWordPathProcessor instance = mynlp.getInstance(OptimizeWordPathProcessor.class);
        instance.setOrder(order);
        instance.addAllOptimizeProcessor(ops);
        addProcessor(instance);
        return this;
    }

    public PipelineTokenizerBuilder addOptimizeProcessor(
            List<? extends OptimizeProcessor> ops) {
        this.addOptimizeProcessor(ops, 0);
        return this;
    }

    /**
     * 增加一组OptimizeProcessor实现类
     *
     * @param ops
     * @return
     */
    public PipelineTokenizerBuilder addOptimizeProcessorClass(
            List<Class<? extends OptimizeProcessor>> ops, int order) {
        List<OptimizeProcessor> list =
                ops.stream().map(it -> mynlp.getInstance(it)).collect(Collectors.toList());

        return addOptimizeProcessor(list, order);
    }

    public PipelineTokenizerBuilder addOptimizeProcessorClass(
            List<Class<? extends OptimizeProcessor>> ops) {
        this.addOptimizeProcessorClass(ops, 0);
        return this;
    }


    /**
     * 增加WordnetInitializer对象
     *
     * @param initializers
     * @return
     */
    public PipelineTokenizerBuilder addWordnetInitializer(WordnetInitializer... initializers) {

        for (WordnetInitializer initializer : initializers) {
            this.wordnetInitializer.add(initializer);
        }

        Collections.sort(wordnetInitializer);
        return this;
    }

    /**
     * 增加WordnetInitializer
     *
     * @param initializers
     * @return
     */
    public PipelineTokenizerBuilder addWordnetInitializer(Class<? extends WordnetInitializer>... initializers) {

        for (Class<? extends WordnetInitializer> clazz : initializers) {
            this.wordnetInitializer.add(mynlp.getInstance(clazz));
        }
        Collections.sort(wordnetInitializer);
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

    private static class ConsumerPair {
        Class clazz;
        Consumer consumer;

        public ConsumerPair(Class clazz, Consumer consumer) {
            this.clazz = clazz;
            this.consumer = consumer;
        }
    }
}

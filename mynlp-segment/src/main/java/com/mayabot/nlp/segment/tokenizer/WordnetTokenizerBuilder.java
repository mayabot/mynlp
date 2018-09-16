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
import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.*;
import com.mayabot.nlp.segment.common.VertexHelper;
import com.mayabot.nlp.segment.tokenizer.normalize.Full2halfCharNormalize;
import com.mayabot.nlp.segment.tokenizer.normalize.LowerCaseCharNormalize;
import com.mayabot.nlp.segment.tokenizer.recognition.OptimizeWordPathProcessor;
import com.mayabot.nlp.segment.wordnet.BestPathAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * WordnetTokenizer构建器
 * <p>
 * 调用WordnetTokenizer.builder()来创建WordnetTokenizerBuilder对象
 *
 * @author jimichan
 */
public class WordnetTokenizerBuilder implements MynlpTokenizerBuilder {

    private final Mynlp mynlp;

    /**
     * 词图最优路径选择器
     */
    private BestPathAlgorithm bestPathAlgorithm;

    /**
     * 字符处理器,默认就有最小化和全角半角化
     */
    private List<CharNormalize> charNormalizes = Lists.newArrayList(
            LowerCaseCharNormalize.instance,
            Full2halfCharNormalize.instace
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
     * 禁用组件的class集合
     */
    private Set<Class> disabledComponentSet = Sets.newHashSet();

    /**
     * 最终结构收集器
     */
    private WordTermCollector termCollector;

    /**
     * 默认构造函数。不公开
     */
    WordnetTokenizerBuilder() {
        this.mynlp = Mynlps.get();
    }

    @Override
    public MynlpTokenizer build() {

        // 1. bestPathAlgorithm
        Preconditions.checkNotNull(bestPathAlgorithm);

        // 2. WordnetInitializer
        Preconditions.checkState(!wordnetInitializer.isEmpty());

        // 3.termCollector
        Preconditions.checkNotNull(termCollector);

        // 4
        callListener();

        return new WordnetTokenizer(
                wordnetInitializer,
                pipeLine.toArray(new WordpathProcessor[0]),
                bestPathAlgorithm
                , termCollector,
                this.charNormalizes,
                mynlp.getInstance(VertexHelper.class));
    }


    /**
     * 调用后置监听器
     */
    private void callListener() {

        config(OptimizeProcessor.class, p -> {
            if (disabledComponentSet.contains(p.getClass())) {
                p.setEnabled(false);
            }
        });
        config(WordpathProcessor.class, p -> {
            if (disabledComponentSet.contains(p.getClass())) {
                p.setEnabled(false);
            }
        });


        //看看要不要配置 WordTermCollector
        configListener.forEach(pair -> {
            if (pair.clazz.equals(termCollector.getClass()) ||
                    pair.clazz.isAssignableFrom(termCollector.getClass())) {
                pair.consumer.accept(termCollector);
            }
        });

        configListener.forEach(pair -> {
            wordnetInitializer.forEach(wf -> {
                if (pair.clazz.equals(wf.getClass()) ||
                        pair.clazz.isAssignableFrom(wf.getClass())) {
                    pair.consumer.accept(wf);
                }
            });

        });

        // 执行这些监听动作
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
    public <T> WordnetTokenizerBuilder config(Class<T> clazz, Consumer<T> listener) {
        configListener.add(new ConsumerPair(clazz, listener));
        return this;
    }

    /**
     * 禁用指定的组件
     *
     * @param clazz 组件的Class
     * @return
     */
    public WordnetTokenizerBuilder disabledComponent(Class clazz) {
        disabledComponentSet.add(clazz);
        return this;
    }

    /**
     * 添加CharNormalize
     *
     * @param charNormalizeClass 通过Guice来初始化该对象
     * @return self
     */
    public WordnetTokenizerBuilder addCharNormalize(Class<? extends CharNormalize> charNormalizeClass) {
        this.charNormalizes.add(mynlp.getInstance(charNormalizeClass));
        return this;
    }

    /**
     * 添加CharNormalize
     *
     * @param charNormalize
     * @return self
     */
    public WordnetTokenizerBuilder addCharNormalize(CharNormalize charNormalize) {
        this.charNormalizes.add(charNormalize);
        return this;
    }

    /**
     * 移除CharNormalize
     *
     * @param clazz
     * @return
     */
    public WordnetTokenizerBuilder removeCharNormalize(Class<? extends CharNormalize> clazz) {
        this.charNormalizes.removeIf(obj -> clazz.isAssignableFrom(obj.getClass()) || obj.getClass().equals(clazz));
        return this;
    }

    /**
     * 设置BestPathComputer的实现对象
     *
     * @param bestPathAlgorithm
     * @return
     */
    public WordnetTokenizerBuilder setBestPathAlgorithm(BestPathAlgorithm bestPathAlgorithm) {
        this.bestPathAlgorithm = bestPathAlgorithm;
        return this;
    }

    /**
     * 设置BestPathComputer的实现类，有Guice创建对象
     *
     * @param clazz
     * @return
     */
    public WordnetTokenizerBuilder setBestPathComputer(Class<? extends BestPathAlgorithm> clazz) {
        this.bestPathAlgorithm = mynlp.getInstance(clazz);
        return this;
    }

    /**
     * 增加一个WordpathProcessor实现对象
     *
     * @param processor
     * @return
     */
    public WordnetTokenizerBuilder addLastProcessor(WordpathProcessor processor) {
        pipeLine.add(processor);
        return this;
    }

    /**
     * 增加一个WordpathProcessor实现类
     *
     * @param clazz
     * @return
     */
    public WordnetTokenizerBuilder addLastProcessor(Class<? extends WordpathProcessor> clazz) {
        pipeLine.add(mynlp.getInstance(clazz));
        return this;
    }

    /**
     * 增加一组OptimizeProcessor实现
     *
     * @param ops
     * @return
     */
    public WordnetTokenizerBuilder addLastOptimizeProcessor(List<? extends OptimizeProcessor> ops) {
        OptimizeWordPathProcessor instance = mynlp.getInstance(OptimizeWordPathProcessor.class);
        instance.addAllOptimizeProcessor(ops);
        pipeLine.add(instance);
        return this;
    }

    /**
     * 增加一组OptimizeProcessor实现类
     *
     * @param ops
     * @return
     */
    public WordnetTokenizerBuilder addLastOptimizeProcessorClass(List<Class<? extends OptimizeProcessor>> ops) {
        List<OptimizeProcessor> list =
                ops.stream().map(it -> mynlp.getInstance(it)).collect(Collectors.toList());

        return addLastOptimizeProcessor(list);
    }

    /**
     * 增加WordnetInitializer对象
     *
     * @param initializers
     * @return
     */
    public WordnetTokenizerBuilder addLastWordnetInitializer(WordnetInitializer... initializers) {

        for (WordnetInitializer initializer : initializers) {
            this.wordnetInitializer.add(initializer);
        }
        return this;
    }

    /**
     * 增加WordnetInitializer
     *
     * @param initializers
     * @return
     */
    public WordnetTokenizerBuilder addLastWordnetInitializer(Class<? extends WordnetInitializer>... initializers) {

        for (Class<? extends WordnetInitializer> clazz : initializers) {
            this.wordnetInitializer.add(mynlp.getInstance(clazz));
        }
        return this;
    }

    /**
     * 设置分词结果收集器
     *
     * @param termCollector
     * @return
     */
    public WordnetTokenizerBuilder setTermCollector(WordTermCollector termCollector) {
        this.termCollector = termCollector;
        return this;
    }

    /**
     * 设置分词结果收集器
     *
     * @param termCollectorClass
     * @return
     */
    public WordnetTokenizerBuilder setTermCollector(Class<? extends WordTermCollector> termCollectorClass) {
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

package com.mayabot.nlp.segment.tokenizer;

import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.*;
import com.mayabot.nlp.segment.common.normalize.Full2halfCharNormalize;
import com.mayabot.nlp.segment.common.normalize.LowerCaseCharNormalize;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceCollector;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceIndexWordCollector;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CorrectionProcessor;
import com.mayabot.nlp.segment.tokenizer.xprocessor.PartOfSpeechTaggingComputerProcessor;
import com.mayabot.nlp.segment.wordnet.BestPathAlgorithm;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author jimichan
 */
public abstract class BaseTokenizerBuilderApi implements MynlpTokenizerBuilder {

    protected WordnetTokenizerBuilder builder = WordnetTokenizer.builder();

    protected Mynlp mynlp = Mynlps.get();

//    private Consumer<WordnetTokenizerBuilder> consumer;



    /**
     * 子类去设置builder
     *
     * @param builder
     */
    protected abstract void setUp(WordnetTokenizerBuilder builder);

    private boolean lowerCaseCharNormalize = true;

    private boolean full2halfCharNormalize = true;

    /**
     * 是否开启分词纠错
     */
    private boolean correction = true;


    /**
     * 是否开启词性分析
     */
    private boolean pos;

    /**
     * WordTerm 收集器
     */
    private WordTermCollector wordTermCollector;

    /**
     * 是否开启分词纠错
     *
     * @param correction
     * @return
     */
    public BaseTokenizerBuilderApi setCorrection(boolean correction) {
        this.correction = correction;
        return this;
    }

    /**
     * 是否开启词性分析
     * @param pos
     * @return
     */
    public BaseTokenizerBuilderApi setPos(boolean pos) {
        this.pos = pos;
        return this;
    }


    public BaseTokenizerBuilderApi sentenceCollector() {
        wordTermCollector = mynlp.getInstance(SentenceCollector.class);
        return this;
    }

    public BaseTokenizerBuilderApi sentenceIndexCollector() {
        wordTermCollector = mynlp.getInstance(SentenceIndexWordCollector.class);
        return this;
    }
//
//    public MynlpTokenizerBuilder custom(Consumer<WordnetTokenizerBuilder> consumer) {
//        this.consumer = consumer;
//        return this;
//    }

    public BaseTokenizerBuilderApi() {
        setUp(builder);

        if (!lowerCaseCharNormalize) {
            builder.removeCharNormalize(LowerCaseCharNormalize.class);
        }

        if (!full2halfCharNormalize) {
            builder.removeCharNormalize(Full2halfCharNormalize.class);
        }

        //这两个一定是在最后的
        if (correction) {
            builder.addLastProcessor(CorrectionProcessor.class);
        }

        if (pos) {
            builder.addLastProcessor(PartOfSpeechTaggingComputerProcessor.class);
        }

        if (wordTermCollector != null) {
            builder.setTermCollector(wordTermCollector);
        } else {
            builder.setTermCollector(mynlp.getInstance(SentenceCollector.class));
        }
    }

    @Override
    public MynlpTokenizer build() {
        return builder.build();
    }


    /**
     * 设定针对WordpathProcessor，WordnetInitializer，WordTermCollector等组件后置逻辑。
     * 通过这个方法可以已经创建的组件进行配置
     *
     * @param clazz
     * @param listener
     * @return
     */
    public <T> WordnetTokenizerBuilder config(Class<T> clazz, Consumer<T> listener) {
        return builder.config(clazz, listener);
    }

    /**
     * 禁用指定的组件
     *
     * @param clazz 组件的Class
     * @return
     */
    public WordnetTokenizerBuilder disabledComponent(Class clazz) {
        return builder.disabledComponent(clazz);
    }

    /**
     * 添加CharNormalize
     *
     * @param charNormalizeClass 通过Guice来初始化该对象
     * @return self
     */
    public WordnetTokenizerBuilder addCharNormalize(Class<? extends CharNormalize> charNormalizeClass) {
        return builder.addCharNormalize(charNormalizeClass);
    }

    /**
     * 添加CharNormalize
     *
     * @param charNormalize
     * @return self
     */
    public WordnetTokenizerBuilder addCharNormalize(CharNormalize charNormalize) {
        return builder.addCharNormalize(charNormalize);
    }

    /**
     * 移除CharNormalize
     *
     * @param clazz
     * @return
     */
    public WordnetTokenizerBuilder removeCharNormalize(Class<? extends CharNormalize> clazz) {
        return builder.removeCharNormalize(clazz);
    }

    /**
     * 设置BestPathComputer的实现对象
     *
     * @param bestPathAlgorithm
     * @return
     */
    public WordnetTokenizerBuilder setBestPathComputer(BestPathAlgorithm bestPathAlgorithm) {
        return builder.setBestPathAlgorithm(bestPathAlgorithm);
    }

    /**
     * 设置BestPathComputer的实现类，有Guice创建对象
     *
     * @param clazz
     * @return
     */
    public WordnetTokenizerBuilder setBestPathComputer(Class<? extends BestPathAlgorithm> clazz) {
        return builder.setBestPathComputer(clazz);
    }

    /**
     * 增加一个WordpathProcessor实现对象
     *
     * @param processor
     * @return
     */
    public WordnetTokenizerBuilder addLastProcessor(WordpathProcessor processor) {
        return builder.addLastProcessor(processor);
    }

    /**
     * 增加一个WordpathProcessor实现类
     *
     * @param clazz
     * @return
     */
    public WordnetTokenizerBuilder addLastProcessor(Class<? extends WordpathProcessor> clazz) {
        return builder.addLastProcessor(clazz);
    }

    /**
     * 增加一组OptimizeProcessor实现
     *
     * @param ops
     * @return
     */
    public WordnetTokenizerBuilder addLastOptimizeProcessor(List<? extends OptimizeProcessor> ops) {
        return builder.addLastOptimizeProcessor(ops);
    }

    /**
     * 增加一组OptimizeProcessor实现类
     *
     * @param ops
     * @return
     */
    public WordnetTokenizerBuilder addLastOptimizeProcessorClass(List<Class<? extends OptimizeProcessor>> ops) {
        return builder.addLastOptimizeProcessorClass(ops);
    }

    /**
     * 增加WordnetInitializer对象
     *
     * @param initializers
     * @return
     */
    public WordnetTokenizerBuilder addLastWordnetInitializer(WordnetInitializer... initializers) {
        return builder.addLastWordnetInitializer(initializers);
    }

    /**
     * 增加WordnetInitializer
     *
     * @param initializers
     * @return
     */
    public WordnetTokenizerBuilder addLastWordnetInitializer(Class<? extends WordnetInitializer>... initializers) {
        return builder.addLastWordnetInitializer(initializers);
    }

    /**
     * 设置分词结果收集器
     *
     * @param termCollector
     * @return
     */
    public WordnetTokenizerBuilder setTermCollector(WordTermCollector termCollector) {
        return builder.setTermCollector(termCollector);
    }

}

package com.mayabot.nlp.segment.tokenizer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.*;
import com.mayabot.nlp.segment.common.VertexHelper;
import com.mayabot.nlp.segment.common.normalize.Full2halfCharNormalize;
import com.mayabot.nlp.segment.common.normalize.LowerCaseCharNormalize;
import com.mayabot.nlp.segment.recognition.OptimizeWordPathProcessor;
import com.mayabot.nlp.segment.wordnet.BestPathComputer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jimichan
 */
public class WordnetTokenizerBuilder implements MynlpTokenizerBuilder {

    private final Mynlp mynlp;

    private BestPathComputer bestPathComputer;

    /**
     * 字符处理器,默认就有最小化和全角半角化
     */
    private List<CharNormalize> charNormalizes = Lists.newArrayList(
            LowerCaseCharNormalize.instance,
            Full2halfCharNormalize.instace
    );

    private List<WordnetInitializer> wordnetInitializer = Lists.newArrayList();

    private WordTermCollector termCollector;

    private ArrayList<WordpathProcessor> pipeLine = Lists.newArrayList();


    private List<Pair> configListener = Lists.newArrayList();

    private Set<Class> disabledComponentSet = Sets.newHashSet();

    WordnetTokenizerBuilder() {
        this.mynlp = Mynlps.get();
    }

    @Override
    public MynlpTokenizer build() {

        // 1. bestPathComputer
        Preconditions.checkNotNull(bestPathComputer);

        // 2. WordnetInitializer
        Preconditions.checkState(!wordnetInitializer.isEmpty());


        // 3.termCollector
        Preconditions.checkNotNull(termCollector);


        WordpathProcessor[] pipeline = prepare(pipeLine).toArray(new WordpathProcessor[0]);

        return new WordnetTokenizer(
                wordnetInitializer,
                pipeline,
                bestPathComputer
                , termCollector,
                this.charNormalizes,
                mynlp.getInstance(VertexHelper.class));
    }


    public WordnetTokenizerBuilder setTermCollector(WordTermCollector termCollector) {
        this.termCollector = termCollector;
        return this;
    }

    public <T> WordnetTokenizerBuilder config(Class<T> clazz, Consumer<T> listener) {
        configListener.add(new Pair(clazz, listener));
        return this;
    }

    public void disabledComponent(Class clazz) {
        disabledComponentSet.add(clazz);
    }


    private ArrayList<WordpathProcessor> prepare(ArrayList<WordpathProcessor> pipeLine) {

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

        return pipeLine;
    }

    public WordnetTokenizerBuilder addCharNormalizes(CharNormalize charNormalize) {
        this.charNormalizes.add(charNormalize);
        return this;
    }

    public WordnetTokenizerBuilder removeCharNormalizes(Class<? extends CharNormalize> clazz) {
        this.charNormalizes.removeIf(obj -> clazz.isAssignableFrom(obj.getClass()) || obj.getClass().equals(clazz));
        return this;
    }

    public BestPathComputer getBestPathComputer() {
        return bestPathComputer;
    }

    public WordnetTokenizerBuilder setBestPathComputer(BestPathComputer bestPathComputer) {
        this.bestPathComputer = bestPathComputer;
        return this;
    }

    public WordnetTokenizerBuilder setBestPathComputer(Class<? extends BestPathComputer> clazz) {
        this.bestPathComputer = mynlp.getInstance(clazz);
        return this;
    }

    public WordnetTokenizerBuilder addLastProcessor(WordpathProcessor processor) {
        pipeLine.add(processor);
        return this;
    }

    public WordnetTokenizerBuilder addLastProcessor(Class<? extends WordpathProcessor> clazz) {
        pipeLine.add(mynlp.getInstance(clazz));
        return this;
    }

    public WordnetTokenizerBuilder addLastProcessor(Function<Mynlp, ? extends WordpathProcessor> factory) {
        pipeLine.add(factory.apply(mynlp));
        return this;
    }

    public WordnetTokenizerBuilder addLastOptimizeProcessor(List<? extends OptimizeProcessor> ops) {
        OptimizeWordPathProcessor instance = mynlp.getInstance(OptimizeWordPathProcessor.class);
        instance.addAllOptimizeProcessor(ops);
        pipeLine.add(instance);
        return this;
    }


    public WordnetTokenizerBuilder addLastOptimizeProcessorClass(List<Class<? extends OptimizeProcessor>> ops) {
        List<OptimizeProcessor> list =
                ops.stream().map(it -> mynlp.getInstance(it)).collect(Collectors.toList());

        return addLastOptimizeProcessor(list);
    }


    public WordnetTokenizerBuilder addWordnetInitializer(WordnetInitializer... initializers) {

        for (WordnetInitializer initializer : initializers) {
            this.wordnetInitializer.add(initializer);
        }

        return this;
    }

    public WordnetTokenizerBuilder setWordnetInitializer(List<WordnetInitializer> initializers) {

        wordnetInitializer = initializers;

        return this;
    }


    private static class Pair {
        Class clazz;
        Consumer consumer;

        public Pair(Class clazz, Consumer consumer) {
            this.clazz = clazz;
            this.consumer = consumer;
        }
    }

    public ArrayList<WordpathProcessor> getPipeLine() {
        return pipeLine;
    }

}

package com.mayabot.nlp.segment;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.crf.CrfBaseSegment;
import com.mayabot.nlp.segment.recognition.OptimizeWordPathProcessor;
import com.mayabot.nlp.segment.recognition.org.OrganizationRecognition;
import com.mayabot.nlp.segment.recognition.personname.PersonRecognition;
import com.mayabot.nlp.segment.recognition.place.PlaceRecognition;
import com.mayabot.nlp.segment.wordnet.BestPathComputer;
import com.mayabot.nlp.segment.wordnet.ViterbiBestPathComputer;
import com.mayabot.nlp.segment.wordnetiniter.AtomSegmenter;
import com.mayabot.nlp.segment.wordnetiniter.CombineWordnetInit;
import com.mayabot.nlp.segment.wordnetiniter.ConvertAbstractWord;
import com.mayabot.nlp.segment.wordnetiniter.CoreDictionaryOriginalSegment;
import com.mayabot.nlp.segment.xprocessor.*;

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

    private WordnetInitializer wordnetInitializer;

    private ArrayList<WordpathProcessor> pipeLine = Lists.newArrayList();

    private static class Pair {
        Class clazz;
        Consumer consumer;

        public Pair(Class clazz, Consumer consumer) {
            this.clazz = clazz;
            this.consumer = consumer;
        }
    }

    private List<Pair> configListener = Lists.newArrayList();

    private Set<Class> disabledComponentSet = Sets.newHashSet();

    WordnetTokenizerBuilder() {
        this.mynlp = Mynlps.get();
    }

    @Override
    public MynlpTokenizer build() {

        final WordnetTokenizer tokenizer = mynlp.getInstance(WordnetTokenizer.class);

        // 1. bestPathComputer
        if (bestPathComputer == null) {
            bestPathComputer = mynlp.getInstance(ViterbiBestPathComputer.class);
        }

        tokenizer.bestPathComputer = bestPathComputer;


        // 2. WordnetInitializer
        if (wordnetInitializer == null) {
            coreDict();
        }
        tokenizer.wordnetInitializer = wordnetInitializer;

        tokenizer.pipeline = prepare(pipeLine);


        tokenizer.check();
        return tokenizer;
    }


    public <T> WordnetTokenizerBuilder config(Class<T> clazz, Consumer<T> listener) {
        configListener.add(new Pair(clazz, listener));
        return this;
    }

    public void disabledComponent(Class clazz) {
        disabledComponentSet.add(clazz);
    }


    private ArrayList<WordpathProcessor> prepare(ArrayList<WordpathProcessor> pipeLine) {

        if (pipeLine.isEmpty()) {
            setDefaultPipeline();
        }

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

    public WordnetTokenizerBuilder coreDict() {
        wordnetInitializer = new CombineWordnetInit(
                mynlp.getInstance(CoreDictionaryOriginalSegment.class),
                mynlp.getInstance(AtomSegmenter.class),
                mynlp.getInstance(ConvertAbstractWord.class)
        );
        return this;
    }

    public WordnetTokenizerBuilder crf() {
        wordnetInitializer = new CombineWordnetInit(
                mynlp.getInstance(CrfBaseSegment.class),
                mynlp.getInstance(AtomSegmenter.class),
                mynlp.getInstance(ConvertAbstractWord.class)
        );
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


    private void setDefaultPipeline() {
        addLastProcessor(CustomDictionaryProcessor.class);
        addLastProcessor(MergeNumberQuantifierPreProcessor.class);
        addLastProcessor(MergeNumberAndLetterPreProcessor.class);
        addLastProcessor(CommonPatternProcessor.class);

        addLastOptimizeProcessorClass(Lists.newArrayList(
                PersonRecognition.class,
                PlaceRecognition.class,
                OrganizationRecognition.class));


        addLastProcessor(CorrectionProcessor.class);
        addLastProcessor(PartOfSpeechTaggingComputerProcessor.class);
    }


    public ArrayList<WordpathProcessor> getPipeLine() {
        return pipeLine;
    }

}

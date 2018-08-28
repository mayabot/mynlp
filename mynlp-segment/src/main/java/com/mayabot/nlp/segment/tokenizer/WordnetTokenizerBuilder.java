package com.mayabot.nlp.segment.tokenizer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mayabot.nlp.MynlpIOC;
import com.mayabot.nlp.segment.*;
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
import com.mayabot.nlp.segment.wordprocessor.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jimichan
 */
public class WordnetTokenizerBuilder implements TokenizerBuilder {

    private final MynlpIOC mynlp;

    private BestPathComputer bestPathComputer;

    private WordnetInitializer wordnetInitializer;

    private ArrayList<WordpathProcessor> pipeLine = Lists.newArrayList();

    private List<Consumer<WordpathProcessor>> listenerWordpathProcessor = Lists.newArrayList();
    private List<Consumer<OptimizeProcessor>> listenerOptimizeProcessor = Lists.newArrayList();

    private Set<Class> disabledComponentSet = Sets.newHashSet();

    public static WordnetTokenizerBuilder create(MynlpIOC mynlp) {
        return new WordnetTokenizerBuilder(mynlp);
    }

    private WordnetTokenizerBuilder(MynlpIOC mynlp) {
        this.mynlp = mynlp;
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

    public <T extends WordpathProcessor> void dddd(Class<T> wp, Consumer<T> consumer) {

    }

    public void test() {
        WordnetTokenizerBuilder builder = null;

        builder.dddd(CommonPatternWordPathProcessor.class, x -> {
            x.setEnableEmail(false);
        });

    }


    public void disabledComponent(Class clazz) {
        disabledComponentSet.add(clazz);
    }

    public WordnetTokenizerBuilder beforeBuild(Consumer<WordpathProcessor> listener) {
        listenerWordpathProcessor.add(listener);
        return this;
    }

    public WordnetTokenizerBuilder beforeBuildOptimizeProcessor(Consumer<OptimizeProcessor> listener) {
        listenerOptimizeProcessor.add(listener);
        return this;
    }

    private ArrayList<WordpathProcessor> prepare(ArrayList<WordpathProcessor> pipeLine) {

        if (pipeLine.isEmpty()) {
            setDefaultPipeline();
        }

        //
        beforeBuild(x -> {
            if (disabledComponentSet.contains(x)) {
                x.setEnabled(false);
            }
        });

        beforeBuildOptimizeProcessor(x -> {
            if (disabledComponentSet.contains(x)) {
                x.setEnabled(false);
            }
        });


        //execute listenerWordpathProcessor
        pipeLine.forEach(it -> {
            if (it instanceof OptimizeWordPathProcessor) {
                OptimizeWordPathProcessor op = (OptimizeWordPathProcessor) it;
                op.getOptimizeProcessorList().forEach(pp -> {
                    listenerOptimizeProcessor.forEach(x -> x.accept(pp));
                });
            } else {
                listenerWordpathProcessor.forEach(x -> x.accept(it));
            }
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

    public WordnetTokenizerBuilder addLastProcessor(Function<MynlpIOC, ? extends WordpathProcessor> factory) {
        pipeLine.add(factory.apply(mynlp));
        return this;
    }

    public void addLastOptimizeProcessor(List<? extends OptimizeProcessor> ops) {
        OptimizeWordPathProcessor instance = mynlp.getInstance(OptimizeWordPathProcessor.class);
        instance.addAllOptimizeProcessor(ops);
    }


    public void addLastOptimizeProcessorClass(List<Class<? extends OptimizeProcessor>> ops) {
        List<OptimizeProcessor> list =
                ops.stream().map(it -> mynlp.getInstance(it)).collect(Collectors.toList());

        addLastOptimizeProcessor(list);
    }


    private void setDefaultPipeline() {
        addLastProcessor(CustomDictionaryXProcess.class);
        addLastProcessor(MergeNumberQuantifierPreProcessor.class);
        addLastProcessor(MergeNumberAndLetterPreProcess.class);
        addLastProcessor(CommonPatternWordPathProcessor.class);

        addLastOptimizeProcessorClass(Lists.newArrayList(
                PersonRecognition.class,
                PlaceRecognition.class,
                OrganizationRecognition.class));


        addLastProcessor(CorrectionXProcessor.class);
        addLastProcessor(PartOfSpeechTaggingComputerXProcessor.class);
    }


    public ArrayList<WordpathProcessor> getPipeLine() {
        return pipeLine;
    }

}

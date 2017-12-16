package com.mayabot.nlp.segment;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.segment.bestpath.ViterbiBestPathComputer;
import com.mayabot.nlp.segment.recognition.org.OrganizationRecognition;
import com.mayabot.nlp.segment.recognition.personname.PersonRecognition;
import com.mayabot.nlp.segment.recognition.place.PlaceRecognition;
import com.mayabot.nlp.segment.wordnet.BestPathComputer;
import com.mayabot.nlp.segment.wordnetiniter.AtomSegmenter;
import com.mayabot.nlp.segment.wordnetiniter.ConvertAbstractWord;
import com.mayabot.nlp.segment.wordnetiniter.CoreDictionaryOriginalSegment;
import com.mayabot.nlp.segment.wordnetiniter.MultiWordnetInit;
import com.mayabot.nlp.segment.wordnetiniter.crf.CrfOriginalSegment;
import com.mayabot.nlp.segment.xprocessor.*;

import java.util.Map;
import java.util.function.Function;

/**
 * 注册wordnet 分词器的组件
 */
public class NamedComponentRegistry {

    static InternalLogger logger = InternalLoggerFactory.getInstance(NamedComponentRegistry.class);

    private final Injector injector;

    private Table<String, Class, Function> table = HashBasedTable.create();

    @Inject
    public NamedComponentRegistry( Injector injector ) {
        this.injector = injector;
        initDefaultComponents();
    }

    public <T> void register(String name, Class<T> clazz, Function<Injector, ? extends T> factory) {
        table.put(name, clazz, factory);
        logger.info("register {} for class {}", name, clazz);
    }

    public <T> Function<Injector, T> getFactory(String name, Class<T> clazz) {
        Function function = table.get(name, clazz);
        return function;
    }

    public <T> T getInstance(String name, Class<T> clazz) {
        Function<Injector, T> factory = getFactory(name, clazz);
        Preconditions.checkNotNull(factory,"Not found name "+name+" class "+clazz );
        T apply = factory.apply(injector);
        Preconditions.checkNotNull(apply);
        return apply;
    }

    public Map<String, Function> getFactoryByClass(Class clazz) {
        Map<String, Function> column = ImmutableMap.copyOf(table.column(clazz));
        return column;
    }


    private void initDefaultComponents() {
        register("viterbi", BestPathComputer.class, i -> i.getInstance(ViterbiBestPathComputer.class));

        //wordnet initer
        register("crf", WordnetInitializer.class, inject -> new MultiWordnetInit(
                inject.getInstance(CrfOriginalSegment.class),
                inject.getInstance(AtomSegmenter.class),
                inject.getInstance(ConvertAbstractWord.class)
        ));

        register("core", WordnetInitializer.class, inject -> new MultiWordnetInit(
                inject.getInstance(CoreDictionaryOriginalSegment.class),
                inject.getInstance(AtomSegmenter.class),
                inject.getInstance(ConvertAbstractWord.class)
        ));

        //recognition
        register("place", OptimizeProcessor.class, PlaceRecognition::build);
        register("person", OptimizeProcessor.class, PersonRecognition::build);
        register("organization", OptimizeProcessor.class, OrganizationRecognition::build);

        // x process
        register("subindex", WordpathProcessor.class, injector -> new IndexSubwordsProcess());
        register("mq", WordpathProcessor.class, injector -> injector.getInstance(MergeNumberQuantifierPreProcessor.class));
        register("ml", WordpathProcessor.class, injector -> injector.getInstance(MergeNumberAndLetterPreProcess.class));
        register("speechTagging", WordpathProcessor.class, injector -> injector.getInstance(SpeechTaggingComputerXProcessor.class));
        register("correction", WordpathProcessor.class, injector -> injector.getInstance(CorrectionXProcessor.class));
        register("customDict", WordpathProcessor.class, injector -> injector.getInstance(CustomDictionaryXProcess.class));
        register("pattern", WordpathProcessor.class, injector -> new CommonPatternWordPathProcessor());

        register("optimizeNet", WordpathProcessor.class, injector -> injector.getInstance(OptimizeWordPathProcessor.class));
    }

}

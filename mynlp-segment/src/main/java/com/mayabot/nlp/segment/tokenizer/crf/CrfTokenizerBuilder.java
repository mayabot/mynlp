package com.mayabot.nlp.segment.tokenizer.crf;

import com.mayabot.nlp.segment.tokenizer.BaseTokenizerBuilderApi;
import com.mayabot.nlp.segment.tokenizer.WordnetTokenizerBuilder;
import com.mayabot.nlp.segment.tokenizer.bestpath.ViterbiBestPathAlgorithm;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceCollector;
import com.mayabot.nlp.segment.tokenizer.initializer.AtomSegmenterInitializer;
import com.mayabot.nlp.segment.tokenizer.initializer.ConvertAbstractWordInitializer;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CombineProcessor;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CustomDictionaryProcessor;
import com.mayabot.nlp.segment.tokenizer.xprocessor.TimeStringProcessor;

public class CrfTokenizerBuilder extends BaseTokenizerBuilderApi {

    boolean personRecognition = false;
    boolean placeRecognition = false;
    boolean organizationRecognition = false;


    @Override
    protected void setUp(WordnetTokenizerBuilder builder) {

        //wordnet初始化填充
        builder.addLastWordnetInitializer(
                mynlp.getInstance(CrfBaseSegmentInitializer.class),
                mynlp.getInstance(AtomSegmenterInitializer.class),
                mynlp.getInstance(ConvertAbstractWordInitializer.class)
        );
        builder.addLastWordnetInitializer(mynlp.getInstance(TimeStringProcessor.class));

        //最优路径算法w
        builder.setBestPathComputer(ViterbiBestPathAlgorithm.class);


        // Pipeline处理器
        builder.addLastProcessor(CustomDictionaryProcessor.class);
        builder.addLastProcessor(CombineProcessor.class);
        builder.addLastProcessor(TimeStringProcessor.class);

//        List<Class<? extends OptimizeProcessor>> optimizeProcessor = Lists.newArrayList(
//                PersonRecognition.class,
//                PlaceRecognition.class,
//                OrganizationRecognition.class
//        );
//        builder.addLastOptimizeProcessorClass(optimizeProcessor);


        builder.setTermCollector(new SentenceCollector());


    }

}

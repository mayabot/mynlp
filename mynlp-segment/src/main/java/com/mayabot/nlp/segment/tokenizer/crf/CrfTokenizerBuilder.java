package com.mayabot.nlp.segment.tokenizer.crf;

import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.tokenizer.BaseTokenizerBuilderApi;
import com.mayabot.nlp.segment.tokenizer.WordnetTokenizerBuilder;
import com.mayabot.nlp.segment.tokenizer.bestpath.ViterbiBestPathComputer;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceCollector;
import com.mayabot.nlp.segment.tokenizer.filler.AtomSegmenterFiller;
import com.mayabot.nlp.segment.tokenizer.filler.ConvertAbstractWordFiller;
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
        builder.addLastWordnetFiller(
                Lists.newArrayList(
                        mynlp.getInstance(CrfBaseSegmentFiller.class),
                        mynlp.getInstance(AtomSegmenterFiller.class),
                        mynlp.getInstance(ConvertAbstractWordFiller.class))
        );
        builder.addLastWordnetFiller(mynlp.getInstance(TimeStringProcessor.class));

        //最优路径算法w
        builder.setBestPathComputer(ViterbiBestPathComputer.class);


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

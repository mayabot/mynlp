//package com.mayabot.nlp.segment.crf.tokenizer;
//
//import com.mayabot.nlp.segment.PipelineTokenizerBuilder;
//import com.mayabot.nlp.segment.crf.tokenizer.CrfBaseSegmentInitializer;
//import com.mayabot.nlp.segment.tokenizer.BaseTokenizerBuilder;
//import com.mayabot.nlp.segment.tokenizer.bestpath.ViterbiBestPathAlgorithm;
//import com.mayabot.nlp.segment.tokenizer.collector.SentenceCollector;
//import com.mayabot.nlp.segment.tokenizer.xprocessor.AtomSegmenterInitializer;
//import com.mayabot.nlp.segment.tokenizer.xprocessor.CombineProcessor;
//import com.mayabot.nlp.segment.tokenizer.xprocessor.CustomDictionaryProcessor;
//import com.mayabot.nlp.segment.tokenizer.xprocessor.TimeStringProcessor;
//
//public class CrfTokenizerBuilder extends BaseTokenizerBuilder {
//
//
//    @Override
//    protected void setUp(PipelineTokenizerBuilder builder) {
//
//        //wordnet初始化填充
//        builder.addWordnetInitializer(
//                CrfBaseSegmentInitializer.class,
//                AtomSegmenterInitializer.class,
//                TimeStringProcessor.class
//        );
//
//        //最优路径算法w
//        builder.setBestPathComputer(ViterbiBestPathAlgorithm.class);
//
//
//        // Pipeline处理器
//        builder.addProcessor(CustomDictionaryProcessor.class);
//        builder.addProcessor(CombineProcessor.class);
//
//
//        builder.setTermCollector(new SentenceCollector());
//
//
//    }
//
//}

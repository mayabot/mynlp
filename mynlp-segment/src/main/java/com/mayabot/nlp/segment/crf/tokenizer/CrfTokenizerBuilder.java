//package com.mayabot.nlp.segment.crf.tokenizer;
//
//import com.mayabot.nlp.segment.PipelineTokenizerBuilder;
//import com.mayabot.nlp.segment.crf.tokenizer.CrfBaseSegmentInitializer;
//import com.mayabot.nlp.segment.tokenizer.BaseTokenizerBuilder;
//import com.mayabot.nlp.segment.tokenizer.bestpath.ViterbiBestPathAlgorithm;
//import SentenceCollector;
//import com.mayabot.nlp.segment.tokenizer.xprocessor.CommonSplitAlgorithm;
//import com.mayabot.nlp.segment.tokenizer.xprocessor.CommonRuleWordpathProcessor;
//import com.mayabot.nlp.segment.tokenizer.xprocessor.CustomDictionaryProcessor;
//import com.mayabot.nlp.segment.tokenizer.xprocessor.TimeSplitAlgorithm;
//
//public class CrfTokenizerBuilder extends BaseTokenizerBuilder {
//
//
//    @Override
//    protected void setUp(PipelineTokenizerBuilder builder) {
//
//        //wordnet初始化填充
//        builder.addWordSplitAlgorithm(
//                CrfBaseSegmentInitializer.class,
//                CommonSplitAlgorithm.class,
//                TimeSplitAlgorithm.class
//        );
//
//        //最优路径算法w
//        builder.setBestPathComputer(ViterbiBestPathAlgorithm.class);
//
//
//        // Pipeline处理器
//        builder.addProcessor(CustomDictionaryProcessor.class);
//        builder.addProcessor(CommonRuleWordpathProcessor.class);
//
//
//        builder.setTermCollector(new SentenceCollector());
//
//
//    }
//
//}

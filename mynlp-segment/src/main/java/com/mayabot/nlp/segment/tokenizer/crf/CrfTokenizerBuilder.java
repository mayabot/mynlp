package com.mayabot.nlp.segment.tokenizer.crf;

import com.mayabot.nlp.segment.tokenizer.BaseTokenizerBuilder;
import com.mayabot.nlp.segment.tokenizer.WordnetTokenizerBuilder;
import com.mayabot.nlp.segment.tokenizer.bestpath.ViterbiBestPathAlgorithm;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceCollector;
import com.mayabot.nlp.segment.tokenizer.xprocessor.*;

public class CrfTokenizerBuilder extends BaseTokenizerBuilder {


    @Override
    protected void setUp(WordnetTokenizerBuilder builder) {

        //wordnet初始化填充
        builder.addWordnetInitializer(
                CrfBaseSegmentInitializer.class,
                AtomSegmenterInitializer.class,
                ConvertAbstractWordInitializer.class,
                TimeStringProcessor.class
        );

        //最优路径算法w
        builder.setBestPathComputer(ViterbiBestPathAlgorithm.class);


        // Pipeline处理器
        builder.addProcessor(CustomDictionaryProcessor.class);
        builder.addProcessor(CombineProcessor.class);


        builder.setTermCollector(new SentenceCollector());


    }

}

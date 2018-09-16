package com.mayabot.nlp.segment.tokenizer;

import com.mayabot.nlp.segment.tokenizer.bestpath.LongpathBestPathAlgorithm;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceCollector;
import com.mayabot.nlp.segment.tokenizer.initializer.AtomSegmenterInitializer;
import com.mayabot.nlp.segment.tokenizer.initializer.ConvertAbstractWordInitializer;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CustomDictionaryProcessor;

/**
 * 简单快速版本的词典分词，路径选择最大词
 *
 * @author jimichan
 */
public class SimpleDictTokenizerBuilder extends BaseTokenizerBuilderApi {

    @Override
    protected void setUp(WordnetTokenizerBuilder builder) {
        setCorrection(false);

        //wordnet初始化填充
        builder.addLastWordnetInitializer(
                mynlp.getInstance(CoreTokenizerBuilder.CoreDictionaryInitializer.class),
                mynlp.getInstance(AtomSegmenterInitializer.class),
                mynlp.getInstance(ConvertAbstractWordInitializer.class)
        );

        builder.setBestPathComputer(LongpathBestPathAlgorithm.class);


        builder.addLastProcessor(CustomDictionaryProcessor.class);

        //结果收集器
        builder.setTermCollector(mynlp.getInstance(SentenceCollector.class));
    }


}

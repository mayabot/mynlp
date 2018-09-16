package com.mayabot.nlp.segment.tokenizer;

import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceCollector;
import com.mayabot.nlp.segment.tokenizer.filler.AtomSegmenterFiller;
import com.mayabot.nlp.segment.tokenizer.filler.ConvertAbstractWordFiller;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CustomDictionaryProcessor;
import com.mayabot.nlp.segment.wordnet.BestPathComputer;

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
        builder.addLastWordnetFiller(
                Lists.newArrayList(
                        mynlp.getInstance(CoreTokenizerBuilder.CoreDictionaryFiller.class),
                        mynlp.getInstance(AtomSegmenterFiller.class),
                        mynlp.getInstance(ConvertAbstractWordFiller.class))
        );

        builder.setBestPathComputer(BestPathComputer.longpath);


        builder.addLastProcessor(CustomDictionaryProcessor.class);

        //结果收集器
        builder.setTermCollector(mynlp.getInstance(SentenceCollector.class));
    }


}

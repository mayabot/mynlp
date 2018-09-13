package com.mayabot.nlp.segment.tokenizer;

import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceCollector;
import com.mayabot.nlp.segment.wordnet.BestPathComputer;
import com.mayabot.nlp.segment.wordnetiniter.AtomSegmenter;
import com.mayabot.nlp.segment.wordnetiniter.ConvertAbstractWord;
import com.mayabot.nlp.segment.wordnetiniter.CoreDictionaryOriginalSegment;
import com.mayabot.nlp.segment.xprocessor.CustomDictionaryProcessor;

/**
 * 简单快速版本的词典分词，路径选择最大词
 *
 * @author jimichan
 */
public class SimpleDictTokenizerBuilder extends BaseTokenizerBuilderApi {

    @Override
    public void setUp(WordnetTokenizerBuilder builder) {
        setCorrection(false);

        //wordnet初始化填充
        builder.setWordnetInitializer(
                Lists.newArrayList(
                        mynlp.getInstance(CoreDictionaryOriginalSegment.class),
                        mynlp.getInstance(AtomSegmenter.class),
                        mynlp.getInstance(ConvertAbstractWord.class))
        );

        builder.setBestPathComputer(BestPathComputer.longpath);


        builder.addLastProcessor(CustomDictionaryProcessor.class);

        //结果收集器
        builder.setTermCollector(mynlp.getInstance(SentenceCollector.class));
    }


}

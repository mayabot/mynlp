package com.mayabot.nlp.segment.tokenizer;

import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.OptimizeProcessor;
import com.mayabot.nlp.segment.WordTermCollector;
import com.mayabot.nlp.segment.crf.CrfBaseSegment;
import com.mayabot.nlp.segment.recognition.org.OrganizationRecognition;
import com.mayabot.nlp.segment.recognition.personname.PersonRecognition;
import com.mayabot.nlp.segment.recognition.place.PlaceRecognition;
import com.mayabot.nlp.segment.wordnet.ViterbiBestPathComputer;
import com.mayabot.nlp.segment.wordnetiniter.AtomSegmenter;
import com.mayabot.nlp.segment.wordnetiniter.ConvertAbstractWord;
import com.mayabot.nlp.segment.xprocessor.CommonPatternProcessor;
import com.mayabot.nlp.segment.xprocessor.CustomDictionaryProcessor;
import com.mayabot.nlp.segment.xprocessor.MergeNumberAndLetterPreProcessor;
import com.mayabot.nlp.segment.xprocessor.MergeNumberQuantifierPreProcessor;

import java.util.List;

public class CrfTokenizerBuilder extends BaseTokenizerBuilderApi {

    boolean personRecognition = false;
    boolean placeRecognition = false;
    boolean organizationRecognition = false;


    @Override
    void setUp(WordnetTokenizerBuilder builder) {

        //wordnet初始化填充
        builder.setWordnetInitializer(
                Lists.newArrayList(
                        mynlp.getInstance(CrfBaseSegment.class),
                        mynlp.getInstance(AtomSegmenter.class),
                        mynlp.getInstance(ConvertAbstractWord.class))
        );

        //最优路径算法
        builder.setBestPathComputer(ViterbiBestPathComputer.class);


        // Pipeline处理器
        builder.addLastProcessor(CustomDictionaryProcessor.class);
        builder.addLastProcessor(MergeNumberQuantifierPreProcessor.class);
        builder.addLastProcessor(MergeNumberAndLetterPreProcessor.class);
        builder.addLastProcessor(CommonPatternProcessor.class);

        List<Class<? extends OptimizeProcessor>> optimizeProcessor = Lists.newArrayList(
                PersonRecognition.class,
                PlaceRecognition.class,
                OrganizationRecognition.class
        );
        builder.addLastOptimizeProcessorClass(optimizeProcessor);


        //结果收集器
        builder.setTermCollector(WordTermCollector.bestPath);


        if (!personRecognition) {
            builder.disabledComponent(PersonRecognition.class);
        }
        if (!placeRecognition) {
            builder.disabledComponent(PlaceRecognition.class);
        }
        if (!organizationRecognition) {
            builder.disabledComponent(OrganizationRecognition.class);
        }

    }

}

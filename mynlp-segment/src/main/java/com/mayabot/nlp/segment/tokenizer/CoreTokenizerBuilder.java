package com.mayabot.nlp.segment.tokenizer;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.mayabot.nlp.collection.dat.DATMatcher;
import com.mayabot.nlp.segment.OptimizeProcessor;
import com.mayabot.nlp.segment.WordnetFiller;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.tokenizer.bestpath.ViterbiBestPathComputer;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceCollector;
import com.mayabot.nlp.segment.tokenizer.filler.AtomSegmenterFiller;
import com.mayabot.nlp.segment.tokenizer.filler.ConvertAbstractWordFiller;
import com.mayabot.nlp.segment.tokenizer.recognition.org.OrganizationRecognition;
import com.mayabot.nlp.segment.tokenizer.recognition.personname.PersonRecognition;
import com.mayabot.nlp.segment.tokenizer.recognition.place.PlaceRecognition;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CombineProcessor;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CustomDictionaryProcessor;
import com.mayabot.nlp.segment.tokenizer.xprocessor.TimeStringProcessor;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;

import java.util.List;

public class CoreTokenizerBuilder extends BaseTokenizerBuilderApi {

    boolean personRecognition = true;
    boolean placeRecognition = true;
    boolean organizationRecognition = true;


    @Override
    protected void setUp(WordnetTokenizerBuilder builder) {

        //wordnet初始化填充
        builder.addLastWordnetFiller(
                mynlp.getInstance(CoreDictionaryFiller.class),
                mynlp.getInstance(AtomSegmenterFiller.class),
                mynlp.getInstance(ConvertAbstractWordFiller.class)
        );
        builder.addLastWordnetFiller(mynlp.getInstance(TimeStringProcessor.class));


        //最优路径算法
        builder.setBestPathComputer(ViterbiBestPathComputer.class);


        // Pipeline处理器
        builder.addLastProcessor(CustomDictionaryProcessor.class);

        builder.addLastProcessor(CombineProcessor.class);
        builder.addLastProcessor(TimeStringProcessor.class);

        List<Class<? extends OptimizeProcessor>> optimizeProcessor = Lists.newArrayList(
                PersonRecognition.class,
                PlaceRecognition.class,
                OrganizationRecognition.class
        );
        builder.addLastOptimizeProcessorClass(optimizeProcessor);


        //结果收集器
        builder.setTermCollector(mynlp.getInstance(SentenceCollector.class));


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

    public boolean isPersonRecognition() {
        return personRecognition;
    }

    public CoreTokenizerBuilder setPersonRecognition(boolean personRecognition) {
        this.personRecognition = personRecognition;
        return this;
    }

    public boolean isPlaceRecognition() {
        return placeRecognition;
    }

    public CoreTokenizerBuilder setPlaceRecognition(boolean placeRecognition) {
        this.placeRecognition = placeRecognition;
        return this;
    }

    public boolean isOrganizationRecognition() {
        return organizationRecognition;
    }

    public CoreTokenizerBuilder setOrganizationRecognition(boolean organizationRecognition) {
        this.organizationRecognition = organizationRecognition;
        return this;
    }

    /**
     * 基于核心词典的基础切词器
     *
     * @author jimichan
     */

    public static class CoreDictionaryFiller implements WordnetFiller {

        private CoreDictionary coreDictionary;

        @Inject
        public CoreDictionaryFiller(CoreDictionary coreDictionary) {
            this.coreDictionary = coreDictionary;
        }

        @Override
        public void fill(Wordnet wordnet) {
            char[] text = wordnet.getCharArray();

            // 核心词典查询
            DATMatcher<NatureAttribute> searcher = coreDictionary.match(text, 0);

            while (searcher.next()) {
                int offset = searcher.getBegin();
                int length = searcher.getLength();
                int wordId = searcher.getIndex();

                //没有等效词
                Vertex v = new Vertex(length).setWordInfo(wordId, searcher.getValue());

                wordnet.put(offset, v);
            }
        }


    }

}

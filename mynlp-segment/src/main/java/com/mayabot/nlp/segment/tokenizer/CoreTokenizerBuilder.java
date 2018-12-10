package com.mayabot.nlp.segment.tokenizer;

import com.google.inject.Inject;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieStringIntMap;
import com.mayabot.nlp.segment.WordnetInitializer;
import com.mayabot.nlp.segment.common.BaseMynlpComponent;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.tokenizer.bestpath.ViterbiBestPathAlgorithm;
import com.mayabot.nlp.segment.tokenizer.xprocessor.AtomSegmenterInitializer;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CombineProcessor;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CustomDictionaryProcessor;
import com.mayabot.nlp.segment.tokenizer.xprocessor.TimeStringProcessor;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;

/**
 * 基于HMM-BiGram的分词器.
 *
 * @author jimichan
 */
public class CoreTokenizerBuilder extends BaseTokenizerBuilder {

    public static CoreTokenizerBuilder builder() {
        return new CoreTokenizerBuilder();
    }

    /**
     * 是否启用人名识别
     */
    private boolean personRecognition = true;

    /**
     * 是否启用地名识别
     */
    private boolean placeRecognition = true;

    /**
     * 是否启用组织结构名识别
     */
    private boolean organizationRecognition = true;


    /**
     * 在这里装配所需要的零件吧！！！
     *
     * @param builder
     */
    @Override
    protected void setUp(WordnetTokenizerBuilder builder) {

        //wordnet初始化填充
        builder.addWordnetInitializer(CoreDictionaryInitializer.class);
        builder.addWordnetInitializer(AtomSegmenterInitializer.class);
//        builder.addWordnetInitializer(ConvertAbstractWordInitializer.class);

        builder.addWordnetInitializer(TimeStringProcessor.class);

        //最优路径算法
        builder.setBestPathComputer(ViterbiBestPathAlgorithm.class);

        // Pipeline处理器
        builder.addProcessor(CustomDictionaryProcessor.class);

        builder.addProcessor(CombineProcessor.class);

        // 命名实体识别处理器
//        builder.addOptimizeProcessorClass(Lists.newArrayList(
//                PersonRecognition.class,
//                PlaceRecognition.class,
//                OrganizationRecognition.class
//        ));

//        if (!personRecognition) {
//            builder.disabledComponent(PersonRecognition.class);
//        }
//        if (!placeRecognition) {
//            builder.disabledComponent(PlaceRecognition.class);
//        }
//        if (!organizationRecognition) {
//            builder.disabledComponent(OrganizationRecognition.class);
//        }


    }

    /**
     * 是否启用人名识别
     *
     * @param personRecognition
     * @return Self
     */
    public CoreTokenizerBuilder setPersonRecognition(boolean personRecognition) {
        this.personRecognition = personRecognition;
        return this;
    }

    /**
     * 是否启用地名识别
     *
     * @param placeRecognition
     * @return Self
     */
    public CoreTokenizerBuilder setPlaceRecognition(boolean placeRecognition) {
        this.placeRecognition = placeRecognition;
        return this;
    }

    /**
     * 是否启用机构名名识别
     *
     * @param organizationRecognition
     * @return Self
     */
    public CoreTokenizerBuilder setOrganizationRecognition(boolean organizationRecognition) {
        this.organizationRecognition = organizationRecognition;
        return this;
    }

    /**
     * 基于核心词典的基础切词器
     *
     * @author jimichan
     */
    public static class CoreDictionaryInitializer extends BaseMynlpComponent implements WordnetInitializer {

        private CoreDictionary coreDictionary;

        @Inject
        public CoreDictionaryInitializer(CoreDictionary coreDictionary) {
            this.coreDictionary = coreDictionary;
            setOrder(Integer.MIN_VALUE);
        }

        @Override
        public void fill(Wordnet wordnet) {
            char[] text = wordnet.getCharArray();

            // 核心词典查询
            DoubleArrayTrieStringIntMap.DATMapMatcherInt searcher = coreDictionary.match(text, 0);

            while (searcher.next()) {
                int offset = searcher.getBegin();
                int length = searcher.getLength();
                int wordId = searcher.getIndex();

                Vertex v = new Vertex(length, wordId, searcher.getValue());

                wordnet.put(offset, v);
            }
        }


    }

}

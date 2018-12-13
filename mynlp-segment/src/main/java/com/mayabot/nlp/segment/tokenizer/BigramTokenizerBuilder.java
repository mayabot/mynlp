package com.mayabot.nlp.segment.tokenizer;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieStringIntMap;
import com.mayabot.nlp.segment.WordSplitAlgorithm;
import com.mayabot.nlp.segment.common.BaseSegmentComponent;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.hmmner.OptimizeProcessor;
import com.mayabot.nlp.segment.hmmner.org.OrganizationRecognition;
import com.mayabot.nlp.segment.hmmner.personname.PersonRecognition;
import com.mayabot.nlp.segment.hmmner.place.PlaceRecognition;
import com.mayabot.nlp.segment.tokenizer.bestpath.ViterbiBestPathAlgorithm;
import com.mayabot.nlp.segment.tokenizer.splitalgorithm.CommonSplitAlgorithm;
import com.mayabot.nlp.segment.tokenizer.splitalgorithm.TimeSplitAlgorithm;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CommonRuleWordpathProcessor;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CorrectionWordpathProcessor;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CustomDictionaryProcessor;
import com.mayabot.nlp.segment.tokenizer.xprocessor.PosPerceptronProcessor;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;

import java.util.List;

/**
 * 基于HMM-BiGram的分词器.
 * BiGram
 *
 * @author jimichan
 */
public class BigramTokenizerBuilder extends BaseTokenizerBuilder {

    public static BigramTokenizerBuilder builder() {
        return new BigramTokenizerBuilder();
    }

    /**
     * 基于核心词典的基础切词器
     *
     * @author jimichan
     */
    public static class CoreDictionarySplitAlgorithm extends BaseSegmentComponent implements WordSplitAlgorithm {

        private CoreDictionary coreDictionary;

        @Inject
        public CoreDictionarySplitAlgorithm(CoreDictionary coreDictionary) {
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

    /**
     * 是否启用人名识别
     */
    private boolean hmmPersonName = true;

    /**
     * 是否启用地名识别
     */
    private boolean hmmPlace = true;

    /**
     * 是否启用组织结构名识别
     */
    private boolean hmmOrg = true;


    private boolean pos = true;

    private boolean email = false;

    /**
     * 在这里装配所需要的零件吧！！！
     *
     */
    @Override
    protected void setUp() {

        //最优路径算法
        this.setBestPathComputer(ViterbiBestPathAlgorithm.class);

        //切词算法
        this.addWordSplitAlgorithm(
                CoreDictionarySplitAlgorithm.class,
                CommonSplitAlgorithm.class,
                TimeSplitAlgorithm.class
        );

        // Pipeline处理器
        this.addProcessor(CustomDictionaryProcessor.class);

        this.addProcessor(CommonRuleWordpathProcessor.class);

        if (hmmPersonName || hmmPlace || hmmOrg) {

            List<Class<? extends OptimizeProcessor>> list = Lists.newArrayList();
            if (hmmPersonName) {
                list.add(PersonRecognition.class);
            }
            if (hmmPlace) {
                list.add(PlaceRecognition.class);
            }
            if (hmmOrg) {
                list.add(OrganizationRecognition.class);
            }

            // 命名实体是被需要词性信息
            addProcessor(PosPerceptronProcessor.class);

            addOptimizeProcessorClass(list);
        }

        //分词纠错
        addProcessor(CorrectionWordpathProcessor.class);

        if (pos) {
            addProcessor(PosPerceptronProcessor.class);
        }

        //一些通用模式识别的处理
        addProcessor(CommonRuleWordpathProcessor.class);

        config(CommonRuleWordpathProcessor.class, x -> {
            x.setEnableEmail(email);
        });
    }


    /**
     * 词性分析开关
     *
     * @param pos
     * @return
     */
    public BigramTokenizerBuilder setPos(boolean pos) {
        this.pos = pos;
        return this;
    }

    /**
     * 是否启用人名识别
     *
     * @param personRecognition
     * @return Self
     */
    public BigramTokenizerBuilder setPersonRecognition(boolean personRecognition) {
        this.hmmPersonName = personRecognition;
        return this;
    }

    /**
     * 是否启用地名识别
     *
     * @param placeRecognition
     * @return Self
     */
    public BigramTokenizerBuilder setPlaceRecognition(boolean placeRecognition) {
        this.hmmPlace = placeRecognition;
        return this;
    }

    /**
     * 是否启用机构名名识别
     *
     * @param organizationRecognition
     * @return Self
     */
    public BigramTokenizerBuilder setOrganizationRecognition(boolean organizationRecognition) {
        this.hmmOrg = organizationRecognition;
        return this;
    }


}

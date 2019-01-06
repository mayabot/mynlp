package com.mayabot.nlp.segment.core;

import com.mayabot.nlp.segment.PipelineTokenizerBuilder;
import com.mayabot.nlp.segment.plugins.*;
import com.mayabot.nlp.segment.plugins.correction.CorrectionWordpathProcessor;
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryProcessor;
import com.mayabot.nlp.segment.plugins.personname.PersonNameAlgorithm;
import com.mayabot.nlp.segment.plugins.pos.PosPerceptronProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * 基于HMM-BiGram的分词器.
 * @author jimichan
 */
public class CoreTokenizerBuilder extends PipelineTokenizerBuilder
        implements PersonNameRecognition, PlaceNameRecognition, OrgNameRecognition,
        CustomDictionaryRecognition, PartOfSpeechTagging, Correction {

    public static CoreTokenizerBuilder builder() {
        return new CoreTokenizerBuilder();
    }

    private boolean enablePersonName = true;
    private boolean enablePlaceName = true;
    private boolean enableOrgName = true;
    private boolean enableCustomDictionary = true;
    private boolean enablePOS = true;
    private boolean enableCorrection = true;

    /**
     * 在这里装配所需要的零件吧！！！
     */
    @Override
    protected void setUp() {

        //最优路径算法
        this.setBestPathComputer(ViterbiBestPathAlgorithm.class);

        //切词算法
        this.addWordSplitAlgorithm(CoreDictionarySplitAlgorithm.class);
        this.addWordSplitAlgorithm(AtomSplitAlgorithm.class);


        if (enablePersonName) {
            addWordSplitAlgorithm(PersonNameAlgorithm.class);
        }

        // Pipeline处理器
        if (enableCorrection) {
            this.addProcessor(CustomDictionaryProcessor.class);
        }

        //分词纠错
        if (enableCorrection) {
            addProcessor(CorrectionWordpathProcessor.class);
        }

        //词性标注
        if (enablePOS) {
            addProcessor(PosPerceptronProcessor.class);
        }

    }

    public boolean isEnablePersonName() {
        return enablePersonName;
    }

    @NotNull
    @Override
    public CoreTokenizerBuilder setEnablePersonName(boolean enablePersonName) {
        this.enablePersonName = enablePersonName;
        return this;
    }

    public boolean isEnablePlaceName() {
        return enablePlaceName;
    }

    @NotNull
    @Override
    public CoreTokenizerBuilder setEnablePlaceName(boolean enablePlaceName) {
        this.enablePlaceName = enablePlaceName;
        return this;
    }

    public boolean isEnableOrgName() {
        return enableOrgName;
    }

    @NotNull
    @Override
    public CoreTokenizerBuilder setEnableOrgName(boolean enableOrgName) {
        this.enableOrgName = enableOrgName;
        return this;
    }

    public boolean isEnableCustomDictionary() {
        return enableCustomDictionary;
    }

    @NotNull
    @Override
    public CoreTokenizerBuilder setEnableCustomDictionary(boolean enableCustomDictionary) {
        this.enableCustomDictionary = enableCustomDictionary;
        return this;
    }

    public boolean isEnablePOS() {
        return enablePOS;
    }

    @NotNull
    @Override
    public CoreTokenizerBuilder setEnablePOS(boolean enablePOS) {
        this.enablePOS = enablePOS;
        return this;
    }

    public boolean isEnableCorrection() {
        return enableCorrection;
    }

    @NotNull
    @Override
    public CoreTokenizerBuilder setEnableCorrection(boolean enableCorrection) {
        this.enableCorrection = enableCorrection;
        return this;
    }
}

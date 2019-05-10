package com.mayabot.nlp.segment.core;

import com.mayabot.nlp.segment.PipelineLexerBuilder;
import com.mayabot.nlp.segment.plugins.*;
import com.mayabot.nlp.segment.plugins.correction.CorrectionWordpathProcessor;
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryProcessor;
import com.mayabot.nlp.segment.plugins.ner.NerProcessor;
import com.mayabot.nlp.segment.plugins.personname.PersonNameAlgorithm;
import com.mayabot.nlp.segment.plugins.pos.PosPerceptronProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * 基于HMM-BiGram的分词器.
 * @author jimichan
 */
public class CoreLexerBuilder extends PipelineLexerBuilder
        implements PersonNameRecognition, NERRecognition,
        CustomDictionaryRecognition, PartOfSpeechTagging, Correction {

    public static CoreLexerBuilder builder() {
        return new CoreLexerBuilder();
    }

    private boolean enablePersonName = true;
    private boolean enablePOS = true;
    private boolean enableCorrection = true;
    private boolean enableNER = false;
    private boolean enableCustomDictionary = false;
    // 启用小词典
    private boolean atomDict = false;



    /**
     * 在这里装配所需要的零件吧！！！
     */
    @Override
    protected void setUp() {

        //最优路径算法
        this.setBestPathComputer(ViterbiBestPathAlgorithm.class);

        //切词算法
        if (atomDict) {
            this.addWordSplitAlgorithm(new CoreDictionarySplitAlgorithm(
                    mynlp.getInstance(CoreDictionary.class)
            ));
        } else {
            this.addWordSplitAlgorithm(new CoreDictionarySplitAlgorithm(
                    mynlp.getInstance(CoreDictionary.class)
            ));
        }


        this.addWordSplitAlgorithm(AtomSplitAlgorithm.class);


        if (enablePersonName) {
            addWordSplitAlgorithm(PersonNameAlgorithm.class);
        }


        // Pipeline处理器
        if (enableCustomDictionary) {
            addProcessor(CustomDictionaryProcessor.class);
        }

        //分词纠错
        if (enableCorrection) {
            addProcessor(CorrectionWordpathProcessor.class);
        }

        //词性标注(NER时强制开启)
        if (enablePOS || enableNER) {
            addProcessor(PosPerceptronProcessor.class);
        }

        if (enableNER) {
            addProcessor(NerProcessor.class);
        }

    }

    public boolean isEnablePersonName() {
        return enablePersonName;
    }

    @NotNull
    @Override
    public CoreLexerBuilder setEnablePersonName(boolean enablePersonName) {
        this.enablePersonName = enablePersonName;
        return this;
    }

    public boolean isEnableNER() {
        return enableNER;
    }

    @NotNull
    @Override
    public CoreLexerBuilder setEnableNER(boolean enableNER) {
        this.enableNER = enableNER;
        return this;
    }

    public boolean isEnableCustomDictionary() {
        return enableCustomDictionary;
    }

    @NotNull
    @Override
    public CoreLexerBuilder setEnableCustomDictionary(boolean enableCustomDictionary) {
        this.enableCustomDictionary = enableCustomDictionary;
        return this;
    }

    public boolean isEnablePOS() {
        return enablePOS;
    }

    @NotNull
    @Override
    public CoreLexerBuilder setEnablePOS(boolean enablePOS) {
        this.enablePOS = enablePOS;
        return this;
    }

    public boolean isEnableCorrection() {
        return enableCorrection;
    }

    @NotNull
    @Override
    public CoreLexerBuilder setEnableCorrection(boolean enableCorrection) {
        this.enableCorrection = enableCorrection;
        return this;
    }
}

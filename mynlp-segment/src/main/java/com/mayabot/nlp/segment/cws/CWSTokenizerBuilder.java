package com.mayabot.nlp.segment.cws;

import com.mayabot.nlp.segment.PipelineLexerBuilder;
import com.mayabot.nlp.segment.core.ViterbiBestPathAlgorithm;
import com.mayabot.nlp.segment.plugins.AtomSplitAlgorithm;
import com.mayabot.nlp.segment.plugins.Correction;
import com.mayabot.nlp.segment.plugins.CustomDictionaryRecognition;
import com.mayabot.nlp.segment.plugins.PartOfSpeechTagging;
import com.mayabot.nlp.segment.plugins.correction.CorrectionWordpathProcessor;
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryProcessor;
import com.mayabot.nlp.segment.plugins.pos.PosPerceptronProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * 基于感知机模型的分词器
 *
 * @author jimichan
 */
public class CWSTokenizerBuilder extends PipelineLexerBuilder
        implements CustomDictionaryRecognition, PartOfSpeechTagging, Correction {

    public static CWSTokenizerBuilder builder() {
        return new CWSTokenizerBuilder();
    }

    private boolean enablePOS = true;
    private boolean enableCorrection = true;
    private boolean enableCustomDictionary = false;


    /**
     * 在这里装配所需要的零件吧！！！
     */
    @Override
    protected void setUp() {

        //最优路径算法
        this.setBestPathComputer(ViterbiBestPathAlgorithm.class);

        //切词算法
        this.addWordSplitAlgorithm(CWSSplitAlgorithm.class);
        this.addWordSplitAlgorithm(AtomSplitAlgorithm.class);


        // Pipeline处理器

        if (enableCustomDictionary) {
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

    public boolean isEnableCustomDictionary() {
        return enableCustomDictionary;
    }

    @NotNull
    @Override
    public CWSTokenizerBuilder setEnableCustomDictionary(boolean enableCustomDictionary) {
        this.enableCustomDictionary = enableCustomDictionary;
        return this;
    }

    public boolean isEnablePOS() {
        return enablePOS;
    }

    @NotNull
    @Override
    public CWSTokenizerBuilder setEnablePOS(boolean enablePOS) {
        this.enablePOS = enablePOS;
        return this;
    }

    public boolean isEnableCorrection() {
        return enableCorrection;
    }

    @NotNull
    @Override
    public CWSTokenizerBuilder setEnableCorrection(boolean enableCorrection) {
        this.enableCorrection = enableCorrection;
        return this;
    }
}

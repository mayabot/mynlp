package com.mayabot.nlp.segment.tokenizer;

import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.MynlpTokenizerBuilder;
import com.mayabot.nlp.segment.WordTermCollector;
import com.mayabot.nlp.segment.common.normalize.Full2halfCharNormalize;
import com.mayabot.nlp.segment.common.normalize.LowerCaseCharNormalize;
import com.mayabot.nlp.segment.xprocessor.CorrectionProcessor;
import com.mayabot.nlp.segment.xprocessor.PartOfSpeechTaggingComputerProcessor;

/**
 * @author jimichan
 */
public abstract class BaseTokenizerBuilderApi implements MynlpTokenizerBuilder {

    protected WordnetTokenizerBuilder builder = WordnetTokenizer.builder();

    protected Mynlp mynlp = Mynlps.get();


    /**
     * 子类去设置builder
     *
     * @param builder
     */
    public abstract void setUp(WordnetTokenizerBuilder builder);

    private boolean lowerCaseCharNormalize = true;

    private boolean full2halfCharNormalize = true;

    /**
     * 是否开启分词纠错
     */
    private boolean correction = true;


    /**
     * 是否开启词性分析
     */
    private boolean pos;

    public BaseTokenizerBuilderApi setCorrection(boolean correction) {
        this.correction = correction;
        return this;
    }

    public BaseTokenizerBuilderApi setPos(boolean pos) {
        this.pos = pos;
        return this;
    }

    public BaseTokenizerBuilderApi sentenceResult() {
        builder.setTermCollector(WordTermCollector.bestPath);
        return this;
    }

    public BaseTokenizerBuilderApi sentenceFlatResult() {
        builder.setTermCollector(WordTermCollector.bestpath_subword_flat);
        return this;
    }

    public BaseTokenizerBuilderApi indexResult() {
        builder.setTermCollector(WordTermCollector.indexs_);
        return this;
    }


    @Override
    public MynlpTokenizer build() {

        setUp(builder);

        if (!lowerCaseCharNormalize) {
            builder.removeCharNormalizes(LowerCaseCharNormalize.class);
        }

        if (!full2halfCharNormalize) {
            builder.removeCharNormalizes(Full2halfCharNormalize.class);
        }

        //这两个一定是在最后的
        if (correction) {
            builder.addLastProcessor(CorrectionProcessor.class);
        }

        if (pos) {
            builder.addLastProcessor(PartOfSpeechTaggingComputerProcessor.class);
        }

        return builder.build();
    }

}

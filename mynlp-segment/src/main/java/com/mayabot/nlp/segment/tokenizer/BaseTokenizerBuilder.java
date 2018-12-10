package com.mayabot.nlp.segment.tokenizer;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceCollector;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceIndexWordCollector;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CorrectionProcessor;

/**
 * @author jimichan
 */
public abstract class BaseTokenizerBuilder extends WordnetTokenizerBuilder {

    /**
     * 子类去设置builder
     *
     * @param builder
     */
    protected abstract void setUp(WordnetTokenizerBuilder builder);

    /**
     * 是否开启分词纠错
     */
    private boolean correction = true;

    /**
     * 是否开启词性分析
     */
    private boolean pos = true;

    /**
     * 是否开启分词纠错
     *
     * @param correction
     * @return
     */
    public BaseTokenizerBuilder setCorrection(boolean correction) {
        this.correction = correction;
        return this;
    }

    /**
     * 是否开启词性分析
     *
     * @param pos
     * @return
     */
    public BaseTokenizerBuilder setPos(boolean pos) {
        this.pos = pos;
        return this;
    }

//    public BaseTokenizerBuilder setLowerCaseCharNormalize(boolean lowerCaseCharNormalize) {
//        this.lowerCaseCharNormalize = lowerCaseCharNormalize;
//        return this;
//    }
//
//    public BaseTokenizerBuilder setFull2halfCharNormalize(boolean full2halfCharNormalize) {
//        this.full2halfCharNormalize = full2halfCharNormalize;
//        return this;
//    }

    public BaseTokenizerBuilder sentenceCollector() {
        setTermCollector(SentenceCollector.class);
        return this;
    }

    public BaseTokenizerBuilder sentenceIndexCollector() {
        setTermCollector(SentenceIndexWordCollector.class);
        return this;
    }


    @Override
    public MynlpTokenizer build() {

        setUp(this);

//        if (!lowerCaseCharNormalize) {
//            removeCharNormalize(LowerCaseCharNormalize.class);
//        }
//
//        if (!full2halfCharNormalize) {
//            removeCharNormalize(DefaultCharNormalize.class);
//        }

        //这两个一定是在最后的
        if (correction) {
            addProcessor(CorrectionProcessor.class);
        }

//        if (pos) {
//            addProcessor(PartOfSpeechTaggingComputerProcessor.class);
//        }

        return super.build();
    }

}

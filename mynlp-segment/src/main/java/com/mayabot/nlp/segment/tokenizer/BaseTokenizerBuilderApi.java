package com.mayabot.nlp.segment.tokenizer;

import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.MynlpTokenizerBuilder;
import com.mayabot.nlp.segment.WordTermCollector;
import com.mayabot.nlp.segment.common.normalize.Full2halfCharNormalize;
import com.mayabot.nlp.segment.common.normalize.LowerCaseCharNormalize;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceCollector;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceIndexWordCollector;
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

    /**
     * WordTerm 收集器
     */
    private WordTermCollector wordTermCollector;

    /**
     * 是否开启分词纠错
     *
     * @param correction
     * @return
     */
    public BaseTokenizerBuilderApi setCorrection(boolean correction) {
        this.correction = correction;
        return this;
    }

    /**
     * 是否开启词性分析
     * @param pos
     * @return
     */
    public BaseTokenizerBuilderApi setPos(boolean pos) {
        this.pos = pos;
        return this;
    }


    public BaseTokenizerBuilderApi sentenceCollector() {
        wordTermCollector = mynlp.getInstance(SentenceCollector.class);
        return this;
    }

    public BaseTokenizerBuilderApi sentenceIndexCollector() {
        wordTermCollector = mynlp.getInstance(SentenceIndexWordCollector.class);
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

        if (wordTermCollector != null) {
            builder.setTermCollector(wordTermCollector);
        }

        return builder.build();
    }

}

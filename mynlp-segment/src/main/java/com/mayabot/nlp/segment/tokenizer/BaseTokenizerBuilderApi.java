package com.mayabot.nlp.segment.tokenizer;

import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.WordTermCollector;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceCollector;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceIndexWordCollector;
import com.mayabot.nlp.segment.tokenizer.normalize.Full2halfCharNormalize;
import com.mayabot.nlp.segment.tokenizer.normalize.LowerCaseCharNormalize;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CorrectionProcessor;
import com.mayabot.nlp.segment.tokenizer.xprocessor.PartOfSpeechTaggingComputerProcessor;

/**
 * @author jimichan
 */
public abstract class BaseTokenizerBuilderApi extends WordnetTokenizerBuilder {

//    protected WordnetTokenizerBuilder builder = WordnetTokenizer.builder();

//    private Consumer<WordnetTokenizerBuilder> consumer;

    /**
     * 子类去设置builder
     *
     * @param builder
     */
    protected abstract void setUp(WordnetTokenizerBuilder builder);

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
        wordTermCollector = Mynlps.getInstance(SentenceCollector.class);
        return this;
    }

    public BaseTokenizerBuilderApi sentenceIndexCollector() {
        wordTermCollector = Mynlps.getInstance(SentenceIndexWordCollector.class);
        return this;
    }


    @Override
    public MynlpTokenizer build() {

        setUp(this);

        if (!lowerCaseCharNormalize) {
            removeCharNormalize(LowerCaseCharNormalize.class);
        }

        if (!full2halfCharNormalize) {
            removeCharNormalize(Full2halfCharNormalize.class);
        }

        //这两个一定是在最后的
        if (correction) {
            addProcessor(CorrectionProcessor.class);
        }

        if (pos) {
            addProcessor(PartOfSpeechTaggingComputerProcessor.class);
        }

        if (wordTermCollector != null) {
            setTermCollector(wordTermCollector);
        } else {
            setTermCollector(Mynlps.getInstance(SentenceCollector.class));
        }

        return super.build();
    }

}

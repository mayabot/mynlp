package com.mayabot.nlp.segment.tokenizer;

import com.mayabot.nlp.segment.PipelineTokenizerBuilder;

/**
 * @author jimichan
 */
public abstract class BaseTokenizerBuilder extends PipelineTokenizerBuilder {

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


}

package com.mayabot.nlp.segment;

/**
 * @author jimichan
 */
public interface TokenizerBuilder {

    /**
     * 构建一个MynlpTokenizer
     *
     * @return
     */
    MynlpTokenizer build();
}

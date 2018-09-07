package com.mayabot.nlp.segment;

import com.mayabot.nlp.segment.tokenizer.CoreTokenizerBuilder;

public class MynlpTokenizers {

    public static MynlpTokenizer coreTokenizer() {
        return new CoreTokenizerBuilder().build();
    }

    public static CoreTokenizerBuilder coreTokenizerBuilder() {
        return new CoreTokenizerBuilder();
    }

}

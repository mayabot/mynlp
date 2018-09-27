package com.mayabot.nlp.segment.tokenizer;

import com.mayabot.nlp.segment.tokenizer.xprocessor.CombineProcessor;

public class Main {
    public static void main(String[] args) {
        WordnetTokenizerBuilder builder = WordnetTokenizer.builder();

        builder.addProcessor(CombineProcessor.class);

        builder.config(CombineProcessor.class, processor -> {
            processor.setEnableBookName(false);
        });
    }
}

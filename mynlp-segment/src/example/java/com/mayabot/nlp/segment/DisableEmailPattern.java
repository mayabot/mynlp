package com.mayabot.nlp.segment;

import com.mayabot.nlp.segment.tokenizer.CoreTokenizerBuilder;
import com.mayabot.nlp.segment.tokenizer.WordnetTokenizerBuilder;
import com.mayabot.nlp.segment.xprocessor.CommonPatternProcessor;

import java.util.List;

public class DisableEmailPattern {

    public static void main(String[] args) {
        MynlpTokenizer tokenizer = new CoreTokenizerBuilder() {
            @Override
            public void setUp(WordnetTokenizerBuilder builder) {
                builder.config(CommonPatternProcessor.class, p -> p.setEnableEmail(false));
            }
        }.build();


        List<String> list = tokenizer.tokenToStringList("这是我的email jimichan@gmail.com");

        System.out.println(list);

    }
}

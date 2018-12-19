package com.mayabot.nlp.segment;

import org.junit.Test;

public class IndexSegmentTest {


    @Test
    public void test() {

        MynlpTokenizer mynlpTokenizer = MynlpTokenizers.
                coreTokenizerBuilder()
//                .sentenceIndexCollector().
                .build();


        System.out.println(mynlpTokenizer.parse("中华人民共和国的利益"));


    }
}

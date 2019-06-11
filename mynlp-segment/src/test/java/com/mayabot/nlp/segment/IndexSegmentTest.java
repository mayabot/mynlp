package com.mayabot.nlp.segment;

import org.junit.Test;

public class IndexSegmentTest {


    @Test
    public void test() {

        Lexer mynlpTokenizer = Lexers.
                coreBuilder()
                .collector().indexPickup().done()
                .build();


        System.out.println(mynlpTokenizer.scan("中华人民共和国的利益"));

    }
}

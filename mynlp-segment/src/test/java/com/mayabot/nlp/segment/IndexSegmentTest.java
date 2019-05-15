package com.mayabot.nlp.segment;

import com.mayabot.nlp.segment.plugins.collector.TermCollectorModel;
import org.junit.Test;

public class IndexSegmentTest {


    @Test
    public void test() {

        Lexer mynlpTokenizer = Lexers.
                coreBuilder()
                .collector().collectorIndex(TermCollectorModel.ATOM)
                .build();


        System.out.println(mynlpTokenizer.scan("中华人民共和国的利益"));

    }
}

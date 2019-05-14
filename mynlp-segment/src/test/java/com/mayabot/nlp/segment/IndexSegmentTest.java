package com.mayabot.nlp.segment;

import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import org.junit.Test;

public class IndexSegmentTest {


    @Test
    public void test() {

        Lexer mynlpTokenizer = Lexers.
                coreLexerBuilder(
                        PipelineLexerBuilder::enableIndexModel)
                .build();


        System.out.println(mynlpTokenizer.scan("中华人民共和国的利益"));

    }
}

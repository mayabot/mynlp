package com.mayabot.nlp;

import com.mayabot.nlp.segment.MynlpSegments;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.analyzer.StandardAnalyzerFactory;
import org.junit.Test;

public class StopwordFilterTest {

    @Test
    public void testStopword() {
        MynlpTokenizer tokenizer = MynlpSegments.nlpTokenizer();

        StandardAnalyzerFactory factory = new StandardAnalyzerFactory(tokenizer);

        System.out.println(factory.create("我的世界I LOV ９ 9 ").toList());


    }
}

package com.mayabot.nlp;

import com.mayabot.nlp.segment.MynlpAnalyzer;
import com.mayabot.nlp.segment.MynlpSegments;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.analyzer.StopwordFilter;
import org.junit.Test;

public class StopwordFilterTest {

    @Test
    public void testStopword() {
        MynlpTokenizer tokenizer = MynlpSegments.nlpTokenizer();


        MynlpAnalyzer ana = tokenizer.createAnalyzer();

        StopwordFilter stopwordFilter = new StopwordFilter(ana);

        stopwordFilter.reset("我的世界");


        for (WordTerm wordTerm : stopwordFilter) {
            System.out.println(wordTerm);
        }


    }
}

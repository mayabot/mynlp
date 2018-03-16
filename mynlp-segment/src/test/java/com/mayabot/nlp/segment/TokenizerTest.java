package com.mayabot.nlp.segment;

import org.junit.Test;

public class TokenizerTest {

    MynlpTokenizer mynlpTokenizer = MynlpSegments.nlpTokenizer();


    @Test
    public void test() {
        System.out.println(mynlpTokenizer.tokenToList("优酷总裁魏明介绍了优酷2015年的内容战略，表示要以“大电影、大网剧、大综艺”为关键词\""));
    }

}

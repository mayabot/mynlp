package com.mayabot.nlp.segment;

import com.mayabot.nlp.segment.plugins.collector.SentenceCollectorBuilder;
import org.junit.Assert;
import org.junit.Test;

public class IndexSegmentTest {


    @Test
    public void test() {

        Lexer mynlpTokenizer = Lexers.
                coreBuilder()
                .customSentenceCollector(SentenceCollectorBuilder::indexSubword)
                .build();

        String str = mynlpTokenizer.scan("中华人民共和国的利益").toString();

        Assert.assertEquals("[中华 华人 人民 人民共和国 共和 共和国] 的 利益",str);
    }
}

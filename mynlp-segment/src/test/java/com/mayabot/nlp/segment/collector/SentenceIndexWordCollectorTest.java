package com.mayabot.nlp.segment.collector;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.core.CoreTokenizerBuilder;
import com.mayabot.nlp.segment.plugins.collector.SentenceIndexWordCollector;
import org.junit.Assert;
import org.junit.Test;

import java.util.TreeMap;

public class SentenceIndexWordCollectorTest {

    @Test
    public void test() {
        SentenceIndexWordCollector collector = new SentenceIndexWordCollector();
        collector.setFolded(true);

        MynlpTokenizer tokenizer = CoreTokenizerBuilder.builder().setTermCollector(
                collector
        ).build();

        //System.out.println(tokenizer.parse("中华人民共和国的主副食品安全法").toString());

        Assert.assertTrue(
                "[中华 华人 人民 人民共和国 共和 共和国]/ns 的/u 主副/n 食品/n 安全/a 法/k"
                        .equals(tokenizer.parse("中华人民共和国的主副食品安全法").toString()));

    }

    @Test
    public void test2() {
        SentenceIndexWordCollector collector = new SentenceIndexWordCollector();
        collector.setFolded(false);

        MynlpTokenizer tokenizer = CoreTokenizerBuilder.builder().setTermCollector(
                collector
        ).build();

        //System.out.println(tokenizer.parse("中华人民共和国的主副食品安全法").toString());
        Assert.assertTrue(
                "中华人民共和国/ns 中华 华人 人民 人民共和国 共和 共和国 的/u 主副/n 食品/n 安全/a 法/k"
                        .equals(tokenizer.parse("中华人民共和国的主副食品安全法").toString()));

    }

    @Test
    public void testExclude() {

        TreeMap<String, String[]> exclude = new TreeMap<>();

        exclude.put("中华人民共和国", new String[]{"华人", "共和"});

        SentenceIndexWordCollector collector = new SentenceIndexWordCollector();
        collector.setFolded(false);
        collector.setExcludeDict(exclude);


        MynlpTokenizer tokenizer = CoreTokenizerBuilder.builder().setTermCollector(
                collector
        ).build();

        Assert.assertEquals("中华人民共和国/ns 中华 人民 人民共和国 共和国 的/u 主副/n 食品/n 安全/a 法/k",
                tokenizer.parse("中华人民共和国的主副食品安全法").toString()
        );


    }
}

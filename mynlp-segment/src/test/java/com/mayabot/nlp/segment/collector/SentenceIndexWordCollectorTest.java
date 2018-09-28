package com.mayabot.nlp.segment.collector;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.tokenizer.CoreTokenizerBuilder;
import com.mayabot.nlp.segment.tokenizer.collector.SentenceIndexWordCollector;
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

        Assert.assertTrue(
                "[中华人民共和国/ns[中华/nz, 华人/n, 人民/n, 共和/n, 共和国/n], 的/ude1, 主副食品/n[主副食/j, 副食/n, 副食品/n, 食品/n], 安全/an, 法/n]"
                        .equals(tokenizer.tokenToTermList("中华人民共和国的主副食品安全法").toString()));

    }

    @Test
    public void test2() {
        SentenceIndexWordCollector collector = new SentenceIndexWordCollector();
        collector.setFolded(false);

        MynlpTokenizer tokenizer = CoreTokenizerBuilder.builder().setTermCollector(
                collector
        ).build();

        Assert.assertTrue(
                "[中华人民共和国/ns, 中华/nz, 华人/n, 人民/n, 共和/n, 共和国/n, 的/ude1, 主副食品/n, 主副食/j, 副食/n, 副食品/n, 食品/n, 安全/an, 法/n]"
                        .equals(tokenizer.tokenToTermList("中华人民共和国的主副食品安全法").toString()));

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

        Assert.assertEquals("[中华人民共和国/ns, 中华/nz, 人民/n, 共和国/n, 的/ude1, 主副食品/n, 主副食/j, 副食/n, 副食品/n, 食品/n, 安全/an, 法/n]",
                tokenizer.tokenToTermList("中华人民共和国的主副食品安全法").toString()
        );


    }
}

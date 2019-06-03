package com.mayabot.nlp.segment.collector;

import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.Lexers;
import org.junit.Assert;
import org.junit.Test;

public class SentenceIndexWordCollectorTest {
//
//    @Test
//    public void test() {
//        IndexCollector collector = new IndexCollector(TermCollectorMode.TOP);
//        collector.setFolded(true);
//
//        Lexer tokenizer = Lexers.coreBuilder()
//                .withPos()
//                .withPersonName()
//                .collector().collectorIndex(TermCollectorMode.TOP)
//                .build();
//
//        //System.out.println(tokenizer.scan("中华人民共和国的主副食品安全法").toString());
//
//        Assert.assertTrue(
//                "[中华 华人 人民 人民共和国 共和 共和国]/ns 的/u 主副/n 食品/n 安全/a 法/k"
//                        .equals(tokenizer.scan("中华人民共和国的主副食品安全法").toString()));
//
//    }
//
//    @Test
//    public void test2() {
//        IndexCollector collector = new IndexCollector();
//        collector.setFolded(false);
//
//        Lexer tokenizer = CoreLexerBuilder.builder(builder -> {
//            builder.setTermCollector(collector);
//        }).build();
//
//        //System.out.println(tokenizer.scan("中华人民共和国的主副食品安全法").toString());
//        Assert.assertTrue(
//                "中华人民共和国/ns 中华 华人 人民 人民共和国 共和 共和国 的/u 主副/n 食品/n 安全/a 法/k"
//                        .equals(tokenizer.scan("中华人民共和国的主副食品安全法").toString()));
//
//    }
//
//    @Test
//    public void testExclude() {
//
//        TreeMap<String, String[]> exclude = new TreeMap<>();
//
//        exclude.put("中华人民共和国", new String[]{"华人", "共和"});
//
//        IndexCollector collector = new IndexCollector();
//        collector.setFolded(false);
//        collector.setExcludeDict(exclude);
//
//
//        Lexer tokenizer = CoreLexerBuilder.builder(builder -> {
//            builder.setTermCollector(collector);
//        }).build();
//
//        Assert.assertEquals("中华人民共和国/ns 中华 人民 人民共和国 共和国 的/u 主副/n 食品/n 安全/a 法/k",
//                tokenizer.scan("中华人民共和国的主副食品安全法").toString()
//        );
//
//
//    }
}

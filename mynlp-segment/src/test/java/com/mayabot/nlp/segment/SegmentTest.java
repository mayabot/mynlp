package com.mayabot.nlp.segment;

import org.junit.Test;

public class SegmentTest {

    String text1 = "\n" +

            "商品和服务\n";
//            "\n" +
//            "为了在对文本和索引词例进行迭代时使用词汇缓存，您需要明确词例是否应当纳入词汇表中。一般的标准是词例在语料库中出现次数是否超过预设频率。如词例的出现频率低于预设值，则不把该词例纳入词汇表，而是仅仅作为词例处理。";


    @Test
    public void test() {
        MynlpTokenizer mynlpTokenizer = MynlpTokenizers.coreTokenizer();

        Iterable<WordTerm> parse = mynlpTokenizer.parse(text1).asWordList();


        parse.forEach(System.out::println);



    }

    @Test
    public void test2(){
        MynlpTokenizer mynlpTokenizer = MynlpTokenizers.coreTokenizer();

        System.out.println(mynlpTokenizer.parse("年收入达百万"));

    }
}

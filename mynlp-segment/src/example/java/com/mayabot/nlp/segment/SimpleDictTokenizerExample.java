package com.mayabot.nlp.segment;

import com.mayabot.nlp.segment.tokenizer.SimpleDictTokenizerBuilder;

public class SimpleDictTokenizerExample {

    public static void main(String[] args) {
        MynlpTokenizer tokenizer = new SimpleDictTokenizerBuilder().build();


        String text1 = "中华人民共和国陆地面积约960万平方公里，大陆海岸线1.8万多千米，岛屿岸线1.4万多千米，内海和边海的水域面积约470多万平方千米。海域分布有大小岛屿7600多个，其中台湾岛最大，面积35798平方千米。 [1]  陆地同14国接壤，与6国海上相邻。";
        String text = "上卷 第一回  甄士隐梦幻识通灵　贾雨村风尘怀闺秀";

        tokenizer.tokenToTermList(text).forEach(
                System.out::println
        );

        System.out.println("--------------------");
        tokenizer = new SimpleDictTokenizerBuilder().sentenceIndexCollector().build();

        tokenizer.tokenToTermList(text1).forEach(
                System.out::println
        );
    }
}

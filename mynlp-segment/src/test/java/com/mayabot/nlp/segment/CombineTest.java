package com.mayabot.nlp.segment;

import org.junit.Assert;
import org.junit.Test;

public class CombineTest {

    @Test
    public void test() {

        MynlpTokenizerBuilder builder =
                MynlpTokenizers.coreTokenizerBuilder();



        MynlpTokenizer tokenizer = builder.build();

        String test = "体重182kg\n" +
                "五十八公斤\n" +
                "产品编号BN-598\n" +
                "产品编号BN-598-122N\n" +
                "我买了一台 very cool iPhone7\n" +
                "分词标签是__lable__";


        String[] result = ("体重 182kg\n" +
                "五十八公斤\n" +
                "产品 编号 bn-598\n" +
                "产品 编号 bn-598-122n\n" +
                "我 买了 一台 very cool iphone7\n" +
                "分词 标签 是 __lable__").split("\n");

        int i = 0;
        for (String text : test.split("\n")) {
            String t = tokenizer.parse(text).toString();
            Assert.assertTrue(t + "--->" + result[i], t.equals(result[i].toLowerCase()));
            i++;
        }

    }
}

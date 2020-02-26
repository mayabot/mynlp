package com.mayabot.nlp.segment;

import org.junit.Assert;
import org.junit.Test;

/**
 * CoreTokenizer自带Combine逻辑，不再需要后置处理了。
 */
public class CombineTest {

    @Test
    public void test() {

        Lexer tokenizer = Lexers.core();

        String test = "体重182kg\n" +
                "五十八公斤\n" +
                "产品编号BN-598\n" +
                "产品编号BN-598-122N\n" +
                "我买了一台very cool iPhone7\n" +
                "分词标签是__lable__";


        String[] result = ("体重 182kg\n" +
                "五十八公斤\n" +
                "产品 编号 bn-598\n" +
                "产品 编号 bn-598-122n\n" +
                "我 买了 一台 very cool iphone7\n" +
                "分词 标签 是 __lable__").split("\n");

        int i = 0;
        for (String text : test.split("\n")) {
            String t = tokenizer.scan(text).toPlainString();
            Assert.assertTrue(t + "--->" + result[i], t.equals(result[i].toLowerCase()));
            i++;
        }

    }
}

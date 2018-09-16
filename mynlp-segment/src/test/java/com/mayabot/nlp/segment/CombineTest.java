package com.mayabot.nlp.segment;

import com.google.common.base.Joiner;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CombineProcessor;
import org.junit.Assert;
import org.junit.Test;

public class CombineTest {

    @Test
    public void test() {

        MynlpTokenizerBuilder builder =
                MynlpTokenizers.coreTokenizerBuilder()
                        .setPersonRecognition(false)
                        .custom(b -> {
                            b.config(CombineProcessor.class, x -> x.setEnableShuLiang(true));

                        });

        MynlpTokenizer tokenizer = builder.build();

        String test = "体重182kg\n" +
                "五十八公斤\n" +
                "产品编号BN-598\n" +
                "产品编号BN-598-122N\n" +
                "这个是典型的\"非正常BWW\"综合征\n" +
                "阅读了《西行漫步》这一本书\n" +
                "我买了一台 very cool iPhone7\n" +
                "分词标签是__lable__";


        String[] result = ("体重 182kg\n" +
                "五十八公斤\n" +
                "产品 编号 bn-598\n" +
                "产品 编号 bn-598-122n\n" +
                "这个 是 典型 的 \"非正常BWW\" 综合征\n" +
                "阅读 了 《西行漫步》 这 一 本书\n" +
                "我 买了 一台 very cool iphone7\n" +
                "分词 标签 是 __lable__").split("\n");

        int i = 0;
        for (String text : test.split("\n")) {
            String t = Joiner.on(" ").join(tokenizer.tokenToStringList(text)).toLowerCase();
            Assert.assertTrue(t + "--->" + result[i], t.equals(result[i].toLowerCase()));
            i++;
        }

    }
}

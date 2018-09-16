package com.mayabot.nlp.segment;

import org.junit.Test;

/**
 * 招行分词需求
 */
public class CmbSegment {

    @Test
    public void test() {
        String text = "" +
                "2018年年度收入\n" +
                "2018年收入\n" +
                "17年账单\n" +
                "我要找1到3个月出入账\n" +
                "周一到周三花了多少钱\n" +
                "最近三天花了多少钱\n" +
                "最近一周转账记录\n" +
                "6月账单\n" +
                "半年流水\n" +
                "二月份明细账\n" +
                "最近6个月全部账单\n" +
                "一年流水\n" +
                "四个月流水\n" +
                "四月份收入\n" +
                "上一月支出\n" +
                "6月1号账单\n" +
                "6月28号流水\n";

        // 1. 自定义词库
        // 2. 人工纠错规则

        MynlpTokenizer tokenizer = MynlpTokenizers.coreTokenizer();

        for (String line : text.split("\n")) {
            System.out.print(line + "\t");

            System.out.println(tokenizer.tokenToStringList(line));
        }
    }

}

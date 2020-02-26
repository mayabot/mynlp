package com.mayabot.nlp;

import com.mayabot.nlp.segment.Lexers;

public class Test {
    public static void main(String[] args) {
        System.out.println(Lexers.core().scan(
                "阿里云仓库地址正确,陈宝奇怪别人不好"));
    }
}

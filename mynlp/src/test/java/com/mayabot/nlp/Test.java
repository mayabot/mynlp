package com.mayabot.nlp;

import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.Lexers;

public class Test {
    public static void main(String[] args) {
        String text = "技术和服务";

        Lexer lexer = Lexers.perceptron();

        lexer.scan(text).forEach(x -> {
            System.out.println(x);
        });
    }
}
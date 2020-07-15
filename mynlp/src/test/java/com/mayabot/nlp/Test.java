package com.mayabot.nlp;

import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.Lexers;

public class Test {
    public static void main(String[] args) {
        String text = "A系统“9000000004,hello";

        Lexer lexer = Lexers.core();

        lexer.scan(text).forEach(x -> {
            System.out.println(x);
        });
    }
}
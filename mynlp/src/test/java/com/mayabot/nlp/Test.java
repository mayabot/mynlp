package com.mayabot.nlp;

import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.Lexers;

import java.util.function.Consumer;

public class Test {
    public static void main(String[] args) {
        Mynlps.install((Consumer<MynlpBuilder>) it ->
                it.setDataDir("/path")
        );
        String text = "A系统“9000000004,hello";

        Lexer lexer = Lexers.core();

        lexer.scan(text).forEach(x -> {
            System.out.println(x);
        });
    }
}
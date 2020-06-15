package com.mayabot.nlp;

import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.Lexers;

public class Test {
    public static void main(String[] args) {
        String text = "上海市人民政府";

        Lexer lexer = Lexers.coreBuilder()
                .collector().indexPickup().done().build();

        lexer.scan(text).forEach(x -> {
            System.out.println(x);
        });
    }
}
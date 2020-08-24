package com.mayabot.nlp;

import com.mayabot.nlp.segment.Lexers;

public class Test {
    public static void main(String[] args) {
        Lexers.builder()
                .core()
                .withPos()
                .withNer()
                .withPersonName();
    }
}
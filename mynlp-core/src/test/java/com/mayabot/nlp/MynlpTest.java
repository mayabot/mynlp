package com.mayabot.nlp;

import com.mayabot.nlp.resources.MynlpResource;
import com.mayabot.nlp.utils.CharSourceLineReader;

import javax.inject.Inject;

public class MynlpTest {
    public static void main(String[] args) throws Exception {
        Mynlp mynlp = Mynlp.builder().build();

        MynlpResource resource = mynlp.loadResource("xiyouji.txt");

        CharSourceLineReader charSourceLineReader = resource.openLineReader();
        charSourceLineReader.forEachRemaining(System.out::println);

    }

    public static class A {

        @Inject
        public A(Mynlp mynlp) {
            System.out.println(mynlp);
        }
    }
}

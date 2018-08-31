package com.mayabot.nlp.segment;

import com.mayabot.nlp.segment.recognition.personname.PersonRecognition;
import com.mayabot.nlp.segment.xprocessor.CommonPatternProcessor;

import java.util.List;

public class DisableEmailPattern {

    public static void main(String[] args) {
        MynlpTokenizer tokenizer = MynlpSegments.builder()
                .config(CommonPatternProcessor.class, p -> {
                    p.setEnableEmail(true);
                }).config(WordpathProcessor.class, p -> {
                    // 你看，这里指定WordpathProcessor就可以配置所有的子类
                    System.out.println("xxt \t" + p);
                }).config(OptimizeProcessor.class, p -> {
                    System.out.println("yyyy \t" + p);
                }).config(PersonRecognition.class, p -> {
                    System.out.println("--------- \t" + p);
                }).build();


        List<String> list = tokenizer.tokenToList("这是我的email jimichan@gmail.com");

        System.out.println(list);

    }
}

package com.mayabot.nlp.segment;

import java.util.List;

public class CoreSegment {

    public static void main(String[] args) {
        MynlpTokenizer mynlpTokenizer = MynlpTokenizers.coreTokenizer();

        List<String> x = mynlpTokenizer.tokenToStringList("你好好 _lable_pos");

        System.out.println(x);

        MynlpAnalyzer standard = MynlpAnalyzers.standard();

    }
}

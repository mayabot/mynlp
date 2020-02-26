package com.mayabot.nlp.segment.lexer.perceptron;

import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.Sentence;
import com.mayabot.nlp.segment.plugins.ner.PerceptronNerService;

import java.util.List;

public class PerceptronNerServiceTest {


    public static void main(String[] args) {
        PerceptronNerService ner = Mynlps.instanceOf(PerceptronNerService.class);
        PerceptronsSegmentService cws = Mynlps.instanceOf(PerceptronsSegmentService.class);


        List<String> words = cws.splitWord("悦胜公司成立之初系杭州市体育发展集团（杭州市体育局所属事业单位）下属的全资子公司，主要经营体育事业相关业务，后为服务2018年第14届FINA世界游泳锦标赛，增资扩股为国有控股公司。\n" +
                "\n" +
                "\n");

        Sentence ner1 = ner.ner(words);

        System.out.println(ner1);

    }
}

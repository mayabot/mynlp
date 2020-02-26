package com.mayabot.nlp.segment;

import com.google.common.collect.Lists;
import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.plugins.pos.PerceptronPosService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PosTest {

    public static void main(String[] args) {

//        PerceptronPosService service = Mynlps.instanceOf(PerceptronPosService.class);
//        List<String> words = Lists.newArrayList("第三 章".split(" "));
//        List<Nature> pos = service.pos(words);
//
//        for (int i = 0; i < words.size(); i++) {
//            System.out.println(words.get(i)+"/"+pos.get(i));
//        }

        System.out.println(Lexers.core().scan("第三章,章先生"));
    }

}

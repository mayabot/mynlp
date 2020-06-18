package com.mayabot.nlp.segment;


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

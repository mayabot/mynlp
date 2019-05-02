package com.mayabot.nlp.transform;

import org.junit.Assert;

public class Test {

    @org.junit.Test
    public void test() {

        Simplified2Traditional simplified2Traditional = TransformService.simplified2Traditional();
        Traditional2Simplified traditional2Simplified = TransformService.traditional2Simplified();


        String text = "軟件和體育的藝術";
        String text_s = "软件和体育的艺术";

        Assert.assertTrue(text.equals(simplified2Traditional.transform(text_s)));
        Assert.assertTrue(text_s.equals(traditional2Simplified.transform(text)));
    }
}
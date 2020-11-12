package com.mayabot.nlp.trans;

import com.mayabot.nlp.Mynlp;
import org.junit.Assert;

public class Test {

    @org.junit.Test
    public void test() {

        Mynlp mynlp = Mynlp.instance();

        String text = "軟件和體育的藝術";
        String text_s = "软件和体育的艺术";

        Assert.assertTrue(text.equals(mynlp.s2t(text_s)));

        Assert.assertTrue(text_s.equals(mynlp.t2s(text)));
    }
}
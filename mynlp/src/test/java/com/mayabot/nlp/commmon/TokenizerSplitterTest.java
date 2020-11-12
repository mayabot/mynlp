package com.mayabot.nlp.commmon;


import org.junit.Assert;
import org.junit.Test;

import static com.mayabot.nlp.common.TokenizerSplitter.parts;

public class TokenizerSplitterTest {

    @Test
    public void test() {
        System.out.println();
        Assert.assertTrue(parts("").isEmpty());
        Assert.assertEquals(parts(",abc,efg").toString(), "[abc, efg]");
        Assert.assertEquals(parts(",,abc efg.").toString(), "[abc, efg]");
        Assert.assertEquals(parts("abcefg").toString(), "[abcefg]");
        Assert.assertEquals(parts("ou may skip through a book, reading only those passages concerned  ").toString(),
                "[ou, may, skip, through, a, book, reading, only, those, passages, concerned]");

        Assert.assertEquals(parts("你可以跳读一本书，只拣那些有关的段落读一下即可。").toString(),
                "[你可以跳读一本书, 只拣那些有关的段落读一下即可]");

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            parts("你可以跳读一本书，只拣那些有关的段落读一下即可。");
        }
        long t2 = System.currentTimeMillis();
        long time = t2 - t1;
        Assert.assertTrue(time < 50);
    }
}

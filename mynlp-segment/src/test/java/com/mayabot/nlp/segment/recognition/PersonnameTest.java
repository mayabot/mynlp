package com.mayabot.nlp.segment.recognition;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.TokenizerTestHelp;
import com.mayabot.nlp.segment.tokenizer.BigramTokenizerBuilder;
import org.junit.Test;

public class PersonnameTest {

    @Test
    public void test() {
        {
            String text = "这|是|陈|建国|的|快递";

            MynlpTokenizer tokenizer = new BigramTokenizerBuilder()
                    .setPersonRecognition(false)
                    .build();

            System.out.println(tokenizer.tokenToStringList("这是陈建国的快递"));

            TokenizerTestHelp.test(tokenizer, text);
        }


        {
            String text = "这|是|陈建国|的|快递";

            MynlpTokenizer tokenizer = new BigramTokenizerBuilder()
                    .setPersonRecognition(true)
                    .build();

            System.out.println(tokenizer.tokenToStringList("龚学平等领导说,邓颖超生前杜绝超生"));

            TokenizerTestHelp.test(tokenizer, text);
        }


    }
}

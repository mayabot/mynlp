package com.mayabot.nlp.segment.recognition;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.TokenizerTestHelp;
import com.mayabot.nlp.segment.tokenizer.CoreTokenizerBuilder;
import org.junit.Test;

public class PersonnameTest {

    @Test
    public void test() {
        {
            String text = "这|是|陈|建国|的|快递";

            MynlpTokenizer tokenizer = new CoreTokenizerBuilder()
                    .setPersonRecognition(false)
                    .build();

            TokenizerTestHelp.test(tokenizer, text);
        }


        {
            String text = "这|是|陈建国|的|快递";

            MynlpTokenizer tokenizer = new CoreTokenizerBuilder()
                    .setPersonRecognition(true)
                    .build();


            TokenizerTestHelp.test(tokenizer, text);
        }


    }
}

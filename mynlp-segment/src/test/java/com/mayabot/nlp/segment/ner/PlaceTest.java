package com.mayabot.nlp.segment.ner;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.core.CoreTokenizerBuilder;
import com.mayabot.nlp.segment.utils.TokenizerTestHelp;
import org.junit.Test;

public class PlaceTest {

    @Test
    public void test() {


        {
            String text = "中央|大街|浪漫|永|存";

            MynlpTokenizer tokenizer = new CoreTokenizerBuilder()
                    .setEnableNER(false)
                    .build();

            TokenizerTestHelp.test(tokenizer, text);
        }


        {
            String text = "中央大街|浪漫|永|存";

            MynlpTokenizer tokenizer = new CoreTokenizerBuilder()
                    .setEnableNER(true)
                    .build();


            TokenizerTestHelp.test(tokenizer, text);
        }


    }
}

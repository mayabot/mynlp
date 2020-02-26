package com.mayabot.nlp.segment.ner;

import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.Lexers;
import com.mayabot.nlp.segment.utils.TokenizerTestHelp;
import org.junit.Test;

public class PlaceTest {

    @Test
    public void test() {


        {
            String text = "中央|大街|浪漫|永|存";

            Lexer tokenizer = Lexers.coreBuilder()

                    .build();

            TokenizerTestHelp.test(tokenizer, text);
        }


        {
            String text = "中央大街|浪漫|永|存";

            Lexer tokenizer = Lexers.coreBuilder()
                    .withNer()
                    .build();


            TokenizerTestHelp.test(tokenizer, text);
        }


    }
}

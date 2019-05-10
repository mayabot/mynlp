package com.mayabot.nlp.segment.ner;

import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.core.CoreLexerBuilder;
import com.mayabot.nlp.segment.utils.TokenizerTestHelp;
import org.junit.Test;

public class PlaceTest {

    @Test
    public void test() {


        {
            String text = "中央|大街|浪漫|永|存";

            Lexer tokenizer = new CoreLexerBuilder()
                    .setEnableNER(false)
                    .build();

            TokenizerTestHelp.test(tokenizer, text);
        }


        {
            String text = "中央大街|浪漫|永|存";

            Lexer tokenizer = new CoreLexerBuilder()
                    .setEnableNER(true)
                    .build();


            TokenizerTestHelp.test(tokenizer, text);
        }


    }
}

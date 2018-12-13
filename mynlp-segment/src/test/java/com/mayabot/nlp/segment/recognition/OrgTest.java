package com.mayabot.nlp.segment.recognition;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.TokenizerTestHelp;
import com.mayabot.nlp.segment.tokenizer.BigramTokenizerBuilder;
import org.junit.Test;

public class OrgTest {

    @Test
    public void test() {
        {
            String text = "这|是|上海|万行|信息|科技|有限公司|的|财务报表";

            MynlpTokenizer tokenizer = new BigramTokenizerBuilder()
                    .setOrganizationRecognition(false)
                    .build();

            TokenizerTestHelp.test(tokenizer, text);
        }


        {
            String text = "这|是|上海|万行|信息科技有限公司|的|财务报表";

            MynlpTokenizer tokenizer = new BigramTokenizerBuilder()
                    .setOrganizationRecognition(true)
                    .build();


            TokenizerTestHelp.test(tokenizer, text);
        }


    }
}

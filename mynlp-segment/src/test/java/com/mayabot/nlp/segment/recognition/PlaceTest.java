package com.mayabot.nlp.segment.recognition;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.TokenizerTestHelp;
import com.mayabot.nlp.segment.tokenizer.BigramTokenizerBuilder;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CustomDictionaryProcessor;
import org.junit.Test;

public class PlaceTest {

    @Test
    public void test() {


        {
            String text = "南翔|向|宁夏|固原市|彭|阳|县|红|河镇|黑|牛|沟|村|捐赠|了|挖掘机";

            MynlpTokenizer tokenizer = new BigramTokenizerBuilder()
                    .setPlaceRecognition(false)
                    .setPersonRecognition(false)
                    .setOrganizationRecognition(false)
                    .disabledComponent(CustomDictionaryProcessor.class)
                    .build();

            TokenizerTestHelp.test(tokenizer, text);
        }


        {
            String text = "南翔|向|宁夏|固原市|彭阳县|红河镇|黑牛沟村|捐赠|了|挖掘机";

            MynlpTokenizer tokenizer = new BigramTokenizerBuilder()
                    .setPlaceRecognition(true)
                    .setPersonRecognition(false)
                    .setOrganizationRecognition(false)
                    .disabledComponent(CustomDictionaryProcessor.class)
                    .build();


            TokenizerTestHelp.test(tokenizer, text);
        }


    }
}

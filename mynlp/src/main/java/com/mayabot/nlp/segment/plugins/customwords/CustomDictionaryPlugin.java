package com.mayabot.nlp.segment.plugins.customwords;

import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;

public class CustomDictionaryPlugin implements PipelineLexerPlugin {

    private CustomDictionary customDictionary;

    public CustomDictionaryPlugin(CustomDictionary customDictionary) {
        this.customDictionary = customDictionary;
    }

    public CustomDictionaryPlugin() {
    }


    @Override
    public void init(PipelineLexerBuilder builder) {
        CustomDictionary temp;
        if (customDictionary == null) {
            temp = builder.getMynlp().getInstance(CustomDictionary.class);
        } else {
            temp = customDictionary;
        }

        builder.addProcessor(new CustomDictionaryProcessor(temp));
    }

}

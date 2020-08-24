package com.mayabot.nlp.segment.plugins.customwords;

import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;

public class CustomDictionaryPlugin implements PipelineLexerPlugin {

    private CustomDictionary customDictionary;

    public CustomDictionaryPlugin(CustomDictionary customDictionary) {
        this.customDictionary = customDictionary;
    }

    public CustomDictionaryPlugin() {
        customDictionary = Mynlps.instanceOf(CustomDictionary.class);
    }


    @Override
    public void init(PipelineLexerBuilder builder) {
        builder.addProcessor(new CustomDictionaryProcessor(customDictionary));
    }

}

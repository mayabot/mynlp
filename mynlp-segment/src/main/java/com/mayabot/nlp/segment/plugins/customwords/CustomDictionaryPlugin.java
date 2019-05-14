package com.mayabot.nlp.segment.plugins.customwords;

import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;

public class CustomDictionaryPlugin implements PipelineLexerPlugin {

    @Override
    public void install(PipelineLexerBuilder builder) {
        builder.addProcessor(CustomDictionaryProcessor.class);
    }

}

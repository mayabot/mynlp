package com.mayabot.nlp.segment.plugins.personname;

import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;

/**
 * @author jimichan
 */
public class PersonNamePlugin implements PipelineLexerPlugin {

    @Override
    public void install(PipelineLexerBuilder builder) {
        builder.addWordSplitAlgorithm(PersonNameAlgorithm.class);
    }

}

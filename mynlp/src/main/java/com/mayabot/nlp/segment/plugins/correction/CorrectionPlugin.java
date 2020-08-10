package com.mayabot.nlp.segment.plugins.correction;

import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;

/**
 * @author jimichan
 */
public class CorrectionPlugin implements PipelineLexerPlugin {

    @Override
    public void install(PipelineLexerBuilder builder) {
        builder.addProcessor(CorrectionWordpathProcessor.class);
    }

}

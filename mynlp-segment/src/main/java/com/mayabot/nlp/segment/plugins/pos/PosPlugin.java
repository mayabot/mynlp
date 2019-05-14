package com.mayabot.nlp.segment.plugins.pos;

import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;

/**
 * 词性模块
 *
 * @author jimichan
 */
public class PosPlugin implements PipelineLexerPlugin {

    @Override
    public void install(PipelineLexerBuilder builder) {
        builder.addProcessor(PosPerceptronProcessor.class);
    }
}

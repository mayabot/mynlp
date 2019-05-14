package com.mayabot.nlp.segment.plugins.ner;

import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;
import com.mayabot.nlp.segment.plugins.pos.PosPerceptronProcessor;
import com.mayabot.nlp.segment.plugins.pos.PosPlugin;

/**
 * @author jimichan
 */
public class NerPlugin implements PipelineLexerPlugin {

    @Override
    public void install(PipelineLexerBuilder builder) {

        //如果不存在那么自行安装Pos模块
        if (!builder.existWordPathProcessor(PosPerceptronProcessor.class)) {
            builder.install(new PosPlugin());
        }

        builder.addProcessor(NerProcessor.class);
    }
}

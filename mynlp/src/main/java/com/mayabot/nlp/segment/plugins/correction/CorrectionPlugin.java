package com.mayabot.nlp.segment.plugins.correction;

import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * @author jimichan
 */
public class CorrectionPlugin implements PipelineLexerPlugin {

    CorrectionDictionary dictionary = null;

    public CorrectionPlugin(@NotNull CorrectionDictionary dictionary) {
        this.dictionary = dictionary;
    }

    public CorrectionPlugin() {
    }

    @Override
    public void init(PipelineLexerBuilder builder) {

        CorrectionDictionary temp = dictionary;
        if (temp == null) {
            temp = builder.getMynlp().getInstance(CorrectionDictionary.class);
        }

        builder.addProcessor(new CorrectionWordpathProcessor(temp));
    }

}

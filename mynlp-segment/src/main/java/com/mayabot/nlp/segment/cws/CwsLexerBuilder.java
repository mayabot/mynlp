package com.mayabot.nlp.segment.cws;

import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.LexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;
import com.mayabot.nlp.segment.plugins.correction.CorrectionPlugin;
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryPlugin;
import com.mayabot.nlp.segment.plugins.pos.PosPlugin;

/**
 * 基于感知机模型的分词器
 *
 * @author jimichan
 */
public class CwsLexerBuilder implements LexerBuilder {

    final PipelineLexerPlugin custom;

    private boolean enablePOS = true;
    private boolean enableCorrection = true;
    private boolean enableCustomDictionary = false;

    public static CwsLexerBuilder builder() {
        return new CwsLexerBuilder();
    }

    public static CwsLexerBuilder builder(PipelineLexerPlugin custom) {
        return new CwsLexerBuilder(custom);
    }

    public CwsLexerBuilder() {
        this.custom = null;
    }

    public CwsLexerBuilder(PipelineLexerPlugin custom) {
        this.custom = custom;
    }

    @Override
    public Lexer build() {
        PipelineLexerBuilder builder = PipelineLexerBuilder.builder();

        builder.install(new CwsLexerPlugin());

        if (enableCustomDictionary) {
            builder.install(new CustomDictionaryPlugin());
        }

        //分词纠错
        if (enableCorrection) {
            builder.install(new CorrectionPlugin());
        }

        //词性标注
        if (enablePOS) {
            builder.install(new PosPlugin());
        }

        if (custom != null) {
            builder.install(custom);
        }

        return builder.build();
    }


    public boolean isEnablePOS() {
        return enablePOS;
    }

    public CwsLexerBuilder setEnablePOS(boolean enablePOS) {
        this.enablePOS = enablePOS;
        return this;
    }

    public boolean isEnableCorrection() {
        return enableCorrection;
    }

    public CwsLexerBuilder setEnableCorrection(boolean enableCorrection) {
        this.enableCorrection = enableCorrection;
        return this;
    }

    public boolean isEnableCustomDictionary() {
        return enableCustomDictionary;
    }

    public CwsLexerBuilder setEnableCustomDictionary(boolean enableCustomDictionary) {
        this.enableCustomDictionary = enableCustomDictionary;
        return this;
    }
}

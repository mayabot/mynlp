package com.mayabot.nlp.segment.core;

import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.LexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryPlugin;
import com.mayabot.nlp.segment.plugins.ner.NerPlugin;
import com.mayabot.nlp.segment.plugins.personname.PersonNamePlugin;
import com.mayabot.nlp.segment.plugins.pos.PosPlugin;

/**
 * Core分析器,基于HMM-BiGram的分词器.
 *
 * @author jimichan
 */
public class CoreLexerBuilder implements LexerBuilder {

    private final PipelineLexerPlugin custom;

    private boolean enablePersonName = true;

    private boolean enablePOS = true;

//    private boolean enableCorrection = true;

    private boolean enableNER = false;
    private boolean enableCustomDictionary = false;

    public static CoreLexerBuilder builder() {
        return new CoreLexerBuilder();
    }

    public static CoreLexerBuilder builder(PipelineLexerPlugin custom) {
        return new CoreLexerBuilder(custom);
    }

    public CoreLexerBuilder() {
        this.custom = null;
    }

    public CoreLexerBuilder(PipelineLexerPlugin custom) {
        this.custom = custom;
    }

    @Override
    public Lexer build() {
        PipelineLexerBuilder builder = PipelineLexerBuilder.builder();

        builder.install(new CoreLexerPlugin());

        if (enablePersonName) {
            builder.install(new PersonNamePlugin());
        }

        if (enableCustomDictionary) {
            builder.install(new CustomDictionaryPlugin());
        }

        //分词纠错
//        if (enableCorrection) {
//            builder.install(new CorrectionPlugin());
//        }

        //词性标注
        if (enablePOS) {
            builder.install(new PosPlugin());
        }

        //命名实体识别模块
        if (enableNER) {
            builder.install(new NerPlugin());
        }

        if (custom != null) {
            builder.install(custom);
        }

        return builder.build();
    }

    public boolean isEnablePersonName() {
        return enablePersonName;
    }

    public CoreLexerBuilder setEnablePersonName(boolean enablePersonName) {
        this.enablePersonName = enablePersonName;
        return this;
    }

    public boolean isEnablePOS() {
        return enablePOS;
    }

    public CoreLexerBuilder setEnablePOS(boolean enablePOS) {
        this.enablePOS = enablePOS;
        return this;
    }

    public boolean isEnableNER() {
        return enableNER;
    }

    public CoreLexerBuilder setEnableNER(boolean enableNER) {
        this.enableNER = enableNER;
        return this;
    }

    public boolean isEnableCustomDictionary() {
        return enableCustomDictionary;
    }

    public CoreLexerBuilder setEnableCustomDictionary(boolean enableCustomDictionary) {
        this.enableCustomDictionary = enableCustomDictionary;
        return this;
    }
}

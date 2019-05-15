package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.segment.core.DictionaryMatcher;
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;

/**
 * @author jimichan
 */
public class SentenceCollectorPlugin implements PipelineLexerPlugin {

    private TermCollectorMode model = TermCollectorMode.TOP;

    private DictionaryMatcher subwordDictionary = null;

    public SentenceCollectorPlugin(TermCollectorMode model) {
        this.model = model;
    }

    @Override
    public void install(PipelineLexerBuilder builder) {

        SentenceCollector ic = new SentenceCollector(model);

        ic.setSubwordDictionary(subwordDictionary);

        builder.setTermCollector(ic);
    }

    public TermCollectorMode getModel() {
        return model;
    }

    public SentenceCollectorPlugin setModel(TermCollectorMode model) {
        this.model = model;
        return this;
    }

    public DictionaryMatcher getSubwordDictionary() {
        return subwordDictionary;
    }

    public SentenceCollectorPlugin setSubwordDictionary(DictionaryMatcher subwordDictionary) {
        this.subwordDictionary = subwordDictionary;
        return this;
    }
}

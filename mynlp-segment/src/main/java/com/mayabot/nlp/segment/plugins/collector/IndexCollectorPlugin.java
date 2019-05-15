package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.segment.core.DictionaryMatcher;
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;

/**
 * @author jimichan
 */
public class IndexCollectorPlugin implements PipelineLexerPlugin {

    private TermCollectorMode model = TermCollectorMode.MIXED;

    private DictionaryMatcher subwordDictionary = null;

    public IndexCollectorPlugin(TermCollectorMode model) {
        this.model = model;
    }

    @Override
    public void install(PipelineLexerBuilder builder) {

        IndexCollector ic = new IndexCollector(model);

        ic.setSubwordDictionary(subwordDictionary);


        builder.setTermCollector(ic);
    }

    public TermCollectorMode getModel() {
        return model;
    }

    public IndexCollectorPlugin setModel(TermCollectorMode model) {
        this.model = model;
        return this;
    }

    public DictionaryMatcher getSubwordDictionary() {
        return subwordDictionary;
    }

    public IndexCollectorPlugin setSubwordDictionary(DictionaryMatcher subwordDictionary) {
        this.subwordDictionary = subwordDictionary;
        return this;
    }
}

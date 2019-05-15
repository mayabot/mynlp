package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.segment.core.DictionaryMatcher;
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;

/**
 * @author jimichan
 */
public class IndexCollectorPlugin implements PipelineLexerPlugin {

    private TermCollectorModel model = TermCollectorModel.MIXED;

    private DictionaryMatcher subwordDictionary = null;

    public IndexCollectorPlugin(TermCollectorModel model) {
        this.model = model;
    }

    @Override
    public void install(PipelineLexerBuilder builder) {

        IndexCollector ic = new IndexCollector(model);

        ic.setSubwordDictionary(subwordDictionary);


        builder.setTermCollector(ic);
    }

    public TermCollectorModel getModel() {
        return model;
    }

    public IndexCollectorPlugin setModel(TermCollectorModel model) {
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

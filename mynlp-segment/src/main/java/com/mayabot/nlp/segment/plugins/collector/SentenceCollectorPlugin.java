package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.lexer.core.CoreDictionary;
import com.mayabot.nlp.segment.lexer.core.DictionaryMatcher;
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;

/**
 * @author jimichan
 */
public class SentenceCollectorPlugin implements PipelineLexerPlugin {

    private TermCollectorMode model = TermCollectorMode.TOP;

    private SubwordCollector subwordCollector = null;

    private ComputeMoreSubword computeMoreSubword = null;

    public SentenceCollectorPlugin() {

    }

    public SentenceCollectorPlugin atom(){
        this.model = TermCollectorMode.ATOM;
        return this;
    }

    public SentenceCollectorPlugin mixed(){
        this.model = TermCollectorMode.MIXED;
        return this;
    }

    public SentenceCollectorPlugin top(){
        this.model = TermCollectorMode.TOP;
        return this;
    }

    public SentenceCollectorPlugin indexedSubword() {
        subwordCollector = new IndexSubwordCollector();
        model = TermCollectorMode.MIXED;
        return this;
    }

    public SentenceCollectorPlugin indexedSubword(int minWordLen) {
        IndexSubwordCollector subwordCollector = new IndexSubwordCollector();
        subwordCollector.setMinWordLength(minWordLen);
        this.subwordCollector = subwordCollector;
        model = TermCollectorMode.MIXED;
        return this;
    }

    public SentenceCollectorPlugin dictMoreSubword(DictionaryMatcher dbcms) {
        this.computeMoreSubword = new DictBasedComputeMoreSubword(dbcms);
        return this;
    }

    public SentenceCollectorPlugin dictMoreSubword() {
        dictMoreSubword(Mynlps.instanceOf(CoreDictionary.class));
        return this;
    }

    @Override
    public void install(PipelineLexerBuilder builder) {

        SentenceCollector ic = new SentenceCollector();
        ic.setModel(model);
        ic.setComputeMoreSubword(computeMoreSubword);
        ic.setSubwordCollector(subwordCollector);

        builder.setTermCollector(ic);
    }

    public TermCollectorMode getModel() {
        return model;
    }

    public SentenceCollectorPlugin setModel(TermCollectorMode model) {
        this.model = model;
        return this;
    }

    public SubwordCollector getSubwordCollector() {
        return subwordCollector;
    }

    public SentenceCollectorPlugin setSubwordCollector(SubwordCollector subwordCollector) {
        this.subwordCollector = subwordCollector;
        return this;
    }

    public ComputeMoreSubword getComputeMoreSubword() {
        return computeMoreSubword;
    }

    public SentenceCollectorPlugin setComputeMoreSubword(ComputeMoreSubword computeMoreSubword) {
        this.computeMoreSubword = computeMoreSubword;
        return this;
    }
}

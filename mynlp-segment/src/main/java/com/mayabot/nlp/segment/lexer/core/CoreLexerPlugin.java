package com.mayabot.nlp.segment.lexer.core;

import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;
import com.mayabot.nlp.segment.plugins.atom.AtomSplitAlgorithm;

/**
 * @author jimichan
 */
public class CoreLexerPlugin implements PipelineLexerPlugin {

    private DictionaryMatcher dictionaryMatcher;

    private AtomSplitAlgorithm atomSplitAlgorithm;

    public CoreLexerPlugin(DictionaryMatcher dictionaryMatcher) {
        this.dictionaryMatcher = dictionaryMatcher;
    }

    public CoreLexerPlugin(Mynlp mynlp) {
        this.dictionaryMatcher = mynlp.getInstance(CoreDictionary.class);
    }

    public CoreLexerPlugin() {
        this(Mynlps.get());
    }

    @Override
    public void install(PipelineLexerBuilder builder) {

        builder.setBestPathComputer(ViterbiBestPathAlgorithm.class);

        builder.addWordSplitAlgorithm(new CoreDictionarySplitAlgorithm(
                dictionaryMatcher
        ));

        if (atomSplitAlgorithm != null) {
            builder.addWordSplitAlgorithm(atomSplitAlgorithm);
        } else {
            builder.addWordSplitAlgorithm(new AtomSplitAlgorithm());
        }
    }

    public AtomSplitAlgorithm getAtomSplitAlgorithm() {
        return atomSplitAlgorithm;
    }

    public CoreLexerPlugin setAtomSplitAlgorithm(AtomSplitAlgorithm atomSplitAlgorithm) {
        this.atomSplitAlgorithm = atomSplitAlgorithm;
        return this;
    }
}

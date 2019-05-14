package com.mayabot.nlp.segment.core;

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

    public CoreLexerPlugin(DictionaryMatcher dictionaryMatcher) {
        this.dictionaryMatcher = dictionaryMatcher;
    }

    public CoreLexerPlugin(Mynlp mynlp) {
        this.dictionaryMatcher = mynlp.getInstance(CoreDictionary.class);
    }

    public CoreLexerPlugin() {
        this(Mynlps.get());
    }

    public void install(PipelineLexerBuilder builder) {

        builder.setBestPathComputer(ViterbiBestPathAlgorithm.class);

        builder.addWordSplitAlgorithm(new CoreDictionarySplitAlgorithm(
                dictionaryMatcher
        ));

        builder.addWordSplitAlgorithm(AtomSplitAlgorithm.class);
    }
}

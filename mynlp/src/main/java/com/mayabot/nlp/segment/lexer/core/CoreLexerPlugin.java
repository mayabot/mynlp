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

    private CoreDictionary dictionaryMatcher;

    public CoreLexerPlugin(CoreDictionary dictionaryMatcher) {
        this.dictionaryMatcher = dictionaryMatcher;
    }

    public CoreLexerPlugin(Mynlp mynlp) {
        this.dictionaryMatcher = mynlp.getInstance(CoreDictionaryImpl.class);
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

        builder.addWordSplitAlgorithm(AtomSplitAlgorithm.class);

    }

}

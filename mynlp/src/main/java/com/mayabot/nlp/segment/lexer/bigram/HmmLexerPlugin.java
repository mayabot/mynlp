package com.mayabot.nlp.segment.lexer.bigram;

import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;
import com.mayabot.nlp.segment.plugins.atom.AtomSplitAlgorithm;

/**
 * @author jimichan
 */
public class HmmLexerPlugin implements PipelineLexerPlugin {

    private CoreDictionary dictionaryMatcher;

    public HmmLexerPlugin(CoreDictionary dictionaryMatcher) {
        this.dictionaryMatcher = dictionaryMatcher;
    }

    public HmmLexerPlugin(Mynlp mynlp) {
        this.dictionaryMatcher = mynlp.getInstance(CoreDictionary.class);
    }

    @Override
    public void init(PipelineLexerBuilder builder) {

        builder.setBestPathComputer(ViterbiBestPathAlgorithm.class);


        builder.addWordSplitAlgorithm(new CoreDictionarySplitAlgorithm(
                dictionaryMatcher
        ));

        builder.addWordSplitAlgorithm(AtomSplitAlgorithm.class);

    }

}

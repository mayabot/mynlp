package com.mayabot.nlp.segment.lexer.perceptron;

import com.mayabot.nlp.segment.lexer.core.ViterbiBestPathAlgorithm;
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;
import com.mayabot.nlp.segment.plugins.atom.AtomSplitAlgorithm;
import com.mayabot.nlp.segment.plugins.collector.SentenceCollectorPlugin;
import com.mayabot.nlp.segment.plugins.collector.TermCollectorMode;

public class CwsLexerPlugin implements PipelineLexerPlugin {
    @Override
    public void install(PipelineLexerBuilder builder) {

        //切词算法
        builder.addWordSplitAlgorithm(PerceptronCwsSplitAlgorithm.class);

        builder.addWordSplitAlgorithm(AtomSplitAlgorithm.class);

        //最优路径算法
        builder.setBestPathComputer(ViterbiBestPathAlgorithm.class);

    }
}

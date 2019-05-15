package com.mayabot.nlp.segment.cws;

import com.mayabot.nlp.segment.core.ViterbiBestPathAlgorithm;
import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;
import com.mayabot.nlp.segment.plugins.atom.AtomSplitAlgorithm;
import com.mayabot.nlp.segment.plugins.collector.SentenceCollectorPlugin;
import com.mayabot.nlp.segment.plugins.collector.TermCollectorModel;

public class CwsLexerPlugin implements PipelineLexerPlugin {
    @Override
    public void install(PipelineLexerBuilder builder) {

        //切词算法
        builder.addWordSplitAlgorithm(CwsSplitAlgorithm.class);

        builder.addWordSplitAlgorithm(AtomSplitAlgorithm.class);

        //最优路径算法
        builder.setBestPathComputer(ViterbiBestPathAlgorithm.class);

        builder.install(new SentenceCollectorPlugin(TermCollectorModel.TOP));
    }
}

package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

/**
 * Nlp收集方式，不处理子词
 * 按照WordPath里面描述的唯一切分路径，构建WordTerm序列
 *
 * @author jimichan
 */
public class SentenceCollector extends AbstractWordTermCollector {

    public SentenceCollector(TermCollectorModel model) {
        super(model);
    }

    @Override
    void fillSubWord(WordTerm term, Wordnet wordnet, Wordpath wordPath) {
    }

}

package com.mayabot.nlp.segment.plugins.collector;

import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

/**
 * 从wordnet中计算出子词的方法。
 * @author jimichan
 */
public interface SubwordCollector {

    void subWord(WordTerm term, Wordnet wordnet, Wordpath wordPath);
}

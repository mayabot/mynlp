package com.mayabot.nlp.segment.analyzer;

import com.mayabot.nlp.segment.WordTerm;

/**
 * @author jimichan
 */
public interface WordTermGenerator {

    /**
     * 下一个WordTerm
     *
     * @return 返回null表示终止
     */
    WordTerm nextWord();

}

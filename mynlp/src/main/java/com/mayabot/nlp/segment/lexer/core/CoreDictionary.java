package com.mayabot.nlp.segment.lexer.core;

import com.mayabot.nlp.algorithm.collection.dat.DoubleArrayTrieStringIntMap.DATMapMatcherInt;
import com.mayabot.nlp.common.injector.ImplementedBy;

/**
 * @author jimichan
 */
@ImplementedBy(CoreDictionaryImpl.class)
public interface CoreDictionary {

    /**
     * 匹配算法
     *
     * @param text
     * @param offset
     * @return DATMapMatcherInt
     */
    DATMapMatcherInt match(char[] text, int offset);

    /**
     * 词频总量
     *
     * @return int 词频总量
     */
    int totalFreq();

    void refresh() throws Exception;
}

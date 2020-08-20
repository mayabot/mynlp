package com.mayabot.nlp.segment.lexer.core;

import com.mayabot.nlp.algorithm.collection.dat.DoubleArrayTrieStringIntMap.DATMapMatcherInt;
import com.mayabot.nlp.common.injector.ImplementedBy;
import org.jetbrains.annotations.NotNull;

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

    int wordId(char[] chars, int pos, int len);

    int wordId(CharSequence word);

    public int wordFreq(int wordID);

    boolean contains(@NotNull String word);

    int getWordID(String word);

    int size();
}

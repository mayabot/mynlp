package com.mayabot.nlp.segment.lexer.core;

import com.mayabot.nlp.collection.dat.DoubleArrayTrieStringIntMap.DATMapMatcherInt;

public interface DictionaryMatcher {

    DATMapMatcherInt match(char[] text, int offset);
}

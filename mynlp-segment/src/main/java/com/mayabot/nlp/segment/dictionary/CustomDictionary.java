package com.mayabot.nlp.segment.dictionary;

import com.google.inject.ImplementedBy;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.segment.dictionary.custom.DefaultCustomDictionary;

/**
 * 自定义词典结构.
 * 对外提供一个DoubleArrayTrie
 * @author jimichan
 */
@ImplementedBy(DefaultCustomDictionary.class)
public interface CustomDictionary {

    DoubleArrayTrie<NatureAttribute> getTrie();

}

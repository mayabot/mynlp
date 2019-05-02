package com.mayabot.nlp.transform;

import javax.inject.Singleton;
import java.util.TreeMap;

/**
 * 繁体转简体的词典
 *
 * @author jimichan
 */
@Singleton
public class Traditional2Simplified extends BaseTransformDictionary {

    public static final String rsName = "ts-dict/t2s.txt";

    public Traditional2Simplified() {
    }

    @Override
    public TreeMap<String, String> loadDictionary() {
        return loadFromResouce(rsName);
    }
}

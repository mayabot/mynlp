package com.mayabot.nlp.transform;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.TreeMap;

/**
 * 简体转繁体的词典
 *
 * @author jimichan
 */
@Singleton
public class Simplified2Traditional extends BaseTransformDictionary {

    public Simplified2Traditional() throws IOException {
    }

    @Override
    public TreeMap<String, String> loadDictionary() {
        return loadFromResouce("ts/s2t.txt");
    }
}

package com.mayabot.nlp.module.pinyin;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author jimichan
 */
public class CustomPinyin {

    private Map<String, String> map = new TreeMap<>();

    public void put(String text, String pinyin) {
        map.put(text, pinyin);
    }

    public void remove(String text) {
        map.remove(text);
    }

    public Map<String, String> getMap() {
        return map;
    }

}

package com.mayabot.nlp.pinyin;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.util.Map;

/**
 * @author jimichan
 */
public class CustomPinyin {

    private Map<String, String> map = Maps.newTreeMap();

    public void put(String text, String pinyin) {
        map.put(text, pinyin);
    }

    public void remove(String text) {
        map.remove(text);
    }

    public Map<String, String> getMap() {
        return map;
    }

    public String hash() {
        if (map.isEmpty()) {
            return "";
        }

        Hasher hasher = Hashing.md5().newHasher();

        map.forEach((key, value) -> {
            hasher.putString(key, Charsets.UTF_8);
            hasher.putString(value, Charsets.UTF_8);
        });

        return hasher.hash().toString();
    }
}

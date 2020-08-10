package com.mayabot.nlp.module.pinyin;

import com.mayabot.nlp.common.EncryptionUtil;

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

    public String hash() {
        if (map.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

//        Hasher hasher = Hashing.md5().newHasher();

        map.forEach((key, value) -> {
            sb.append(key).append(value);
//            hasher.putString(key, Charsets.UTF_8);
//            hasher.putString(value, Charsets.UTF_8);
        });

        return EncryptionUtil.md5(sb.toString());
//        return hasher.hash().toString();
    }
}

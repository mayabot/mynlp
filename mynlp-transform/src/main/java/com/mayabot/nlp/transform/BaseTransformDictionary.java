package com.mayabot.nlp.transform;

import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.collection.ahocorasick.AhoCoraickDoubleArrayTrieBuilder;
import com.mayabot.nlp.collection.ahocorasick.AhoCorasickDoubleArrayTrie;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

/**
 * 繁简体转换基础词典
 *
 * @author jimichan
 */
public abstract class BaseTransformDictionary {

    public static final String rsVersion = "1.0.0";

    public abstract TreeMap<String, String> loadDictionary();

    AhoCorasickDoubleArrayTrie<String> trie;


    TreeMap<String, String> loadFromResouce(String resourceName) {
        TreeMap<String, String> treeMap = new TreeMap<>();

        try {

            Mynlp mynlp = Mynlps.get();

            mynlp.getEnv().registeResourceMissing("transform", (rsName, env) -> {
                if (rsName.equals(Simplified2Traditional.rsName) || rsName.equals(Traditional2Simplified.rsName)) {
                    File file = env.download("mynlp-resource-transform-" + rsVersion + ".jar");

                    if (file != null && file.exists()) {
                        return true;
                    }

                }
                return false;
            });

            CharSourceLineReader charSourceLineReader = mynlp.getEnv().loadResource(resourceName).openLineReader();

            charSourceLineReader.forEachRemaining(
                    line -> {
                        String[] split = line.split("=");
                        if (split.length == 2) {
                            treeMap.put(split[0], split[1]);
                        }
                    }
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return treeMap;
    }

    public BaseTransformDictionary() {
        AhoCoraickDoubleArrayTrieBuilder<String> builder = new AhoCoraickDoubleArrayTrieBuilder<>();
        trie = builder.build(loadDictionary());
    }

    public String transform(String text) {
        return this.transform(text.toCharArray());
    }

    public String transform(char[] charArray) {

        final String[] wordNet = new String[charArray.length];
        final int[] lengthNet = new int[charArray.length];
        trie.parseText(charArray, (begin, end, value) -> {
            int length = end - begin;
            if (length > lengthNet[begin]) {
                wordNet[begin] = value;
                lengthNet[begin] = length;
            }
        });
        StringBuilder sb = new StringBuilder(charArray.length);
        for (int offset = 0; offset < wordNet.length; ) {
            if (wordNet[offset] == null) {
                sb.append(charArray[offset]);
                ++offset;
                continue;
            }
            sb.append(wordNet[offset]);
            offset += lengthNet[offset];
        }
        return sb.toString();
    }
}

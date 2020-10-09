package com.mayabot.nlp.module.trans;

import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.algorithm.collection.ahocorasick.AhoCoraickDoubleArrayTrieBuilder;
import com.mayabot.nlp.algorithm.collection.ahocorasick.AhoCorasickDoubleArrayTrie;
import com.mayabot.nlp.common.resources.UseLines;
import com.mayabot.nlp.common.utils.CharSourceLineReader;

import java.io.IOException;
import java.util.TreeMap;

/**
 * 繁简体转换基础词典
 *
 * @author jimichan
 */
public abstract class BaseTransformDictionary {

    public abstract TreeMap<String, String> loadDictionary();

    private AhoCorasickDoubleArrayTrie<String> trie;

    TreeMap<String, String> loadFromResource(String resourceName) {
        TreeMap<String, String> treeMap = new TreeMap<>();

        try {

            Mynlp mynlp = Mynlp.singleton();

            CharSourceLineReader charSourceLineReader =
                    UseLines.lineReader(mynlp.getEnv().loadResource(resourceName).inputStream());

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

package com.mayabot.nlp;

import com.mayabot.nlp.algorithm.collection.dat.DoubleArrayTrieMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class DoubleArrayTrieTest {

    DoubleArrayTrieMap<String> trie() {
        TreeMap<String, String> map = new TreeMap<>();

        String[] list = (
                "一举\n" +
                        "一举一动\n" +
                        "一举成名\n" +
                        "一举成名天下知\n" +
                        "五谷\n" +
                        "五谷丰登\n" +
                        "万万").split("\n");

        Set<Character> sets = new HashSet<>();
        for (int i = 0; i < list.length; i++) {
            map.put(list[i], list[i]);
            char[] chars = list[i].toCharArray();
            for (int j = 0; j < chars.length; j++) {
                sets.add(chars[j]);
            }
        }

        DoubleArrayTrieMap<String> trie = new DoubleArrayTrieMap<String>(map);

        return trie;
    }


    @Test
    public void test() {
        DoubleArrayTrieMap<String> trie = trie();

        Assert.assertTrue(trie.containsKey("一举成名"));
        Assert.assertTrue(!trie.containsKey("一举2"));
    }

}

package com.mayabot.nlp.collection.dat;

import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.IntIntMap;
import com.google.common.collect.Lists;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class DoubleArrayTrieTest {

    DoubleArrayTrie<String> trie() {
        TreeMap<String, String> map = new TreeMap<>();

                String[] list  = (
                        "一举\n" +
                        "一举一动\n" +
                        "一举成名\n" +
                        "一举成名天下知\n" +
                        "万能\n" +
                        "万一\n" +
                        "万能胶").split("\n");

        Set<Character> sets = new HashSet<>();
        for (int i = 0; i < list.length; i++) {
            map.put(list[i], list[i]);
            char[] chars = list[i].toCharArray();
            for (int j = 0; j < chars.length; j++) {
                sets.add(chars[j]);
            }
        }

        DoubleArrayTrieBuilder builder = new DoubleArrayTrieBuilder();
        DoubleArrayTrie trie = builder.build(map);

        sets.forEach(
                c -> System.out.println(c + " -> " + (int) c)
        );

        return trie;
    }


    @Test
    public void test() {
        DoubleArrayTrie<String> trie = trie();

        String x = trie.get("一举成名");

        Assert.assertEquals(x,"一举成名");

        System.out.println(trie.base.length);

        System.out.println("Index" + "\t" + "Base" + "\t" + "Check");

        IntIntHashMap intIntMap = new IntIntHashMap();

        for (int i = 0; i < trie.base.length; i++) {
            if (trie.base[i] != 0 || trie.check[i] != 0) {
                intIntMap.put(i, trie.base[i]);
                System.out.println(i + "\t\t" + trie.base[i] + "\t\t" + trie.check[i]);
            }
        }

        System.out.println(intIntMap.size());
        System.out.println(intIntMap.keys.length);
        System.out.println(intIntMap.values.length);
    }
}

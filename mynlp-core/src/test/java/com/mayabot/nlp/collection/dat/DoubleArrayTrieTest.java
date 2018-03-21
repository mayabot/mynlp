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

        int[] base = trie.base;
        int c =0;
        for (int i = base.length-1; i >=0 ; i--) {
            if (0== base[i]) {
                c++;
            }else{
                System.out.println("+"+i);
                break;
            }
        }

        System.out.println(base.length);
        System.out.println(c);

        System.out.println(x);
        Assert.assertEquals(x,"一举成名");

        System.out.println(trie.base.length);

        System.out.println("Index" + "\t" + "Base" + "\t" + "Check");

        //IntIntHashMap intIntMap = new IntIntHashMap();

        for (int i = 0; i < trie.base.length; i++) {
            if (trie.base[i] != 0 || trie.check[i] != 0) {
                System.out.println(i + "\t\t" + trie.base[i] + "\t\t" + trie.check[i]);
            }
        }

    }
}

package com.mayabot.nlp.collection.dat;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class FastDatStringSet {

    private DoubleArrayTrieStringIntMap map;

    public FastDatStringSet(String... list) {
        HashSet<String> set = new HashSet<>();
        for (String aChar : list) {
            set.add(aChar);
        }

        set(set);
    }

    public FastDatStringSet(Set<String> characterSet) {
        set(characterSet);
    }

    private void set(Set<String> set) {
        TreeMap<String, Integer> treeMap = new TreeMap<>();

        for (String e : set) {
            treeMap.put(e, 1);
        }

        this.map = new DoubleArrayTrieStringIntMap(treeMap);
    }

    public boolean contains(char ch) {
        return map.containsKey(ch);
    }
}

package com.mayabot.nlp.collection.dat;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class FastDatStringSet {

    private DoubleArrayTrie map;

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
        TreeSet<String> treeset = new TreeSet<>();

        for (String e : set) {
            treeset.add(e);
        }

        this.map = new DoubleArrayTrie(treeset);
    }

    public boolean contains(String ch) {
        return map.containsKey(ch);
    }
}

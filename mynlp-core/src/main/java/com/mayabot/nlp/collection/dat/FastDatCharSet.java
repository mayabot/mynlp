package com.mayabot.nlp.collection.dat;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class FastDatCharSet {

    private DoubleArrayTrieStringIntMap map;

    public FastDatCharSet(char... chars) {
        HashSet<Character> set = new HashSet<>();
        for (char aChar : chars) {
            set.add(aChar);
        }

        set(set);
    }

    public FastDatCharSet(Set<Character> characterSet) {
        set(characterSet);
    }

    private void set(Set<Character> characterSet) {
        TreeMap<String, Integer> treeMap = new TreeMap<>();

        for (Character character : characterSet) {
            treeMap.put(character.toString(), 1);
        }

        this.map = new DoubleArrayTrieStringIntMap(treeMap);
    }

    public boolean contains(char ch) {
        return map.containsKey(ch);
    }
}

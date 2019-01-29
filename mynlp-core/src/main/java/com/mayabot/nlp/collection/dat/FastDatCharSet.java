package com.mayabot.nlp.collection.dat;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class FastDatCharSet {

    private DoubleArrayTrie map;

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
        TreeSet<String> treeMap = new TreeSet<>();

        for (Character character : characterSet) {
            treeMap.add(character.toString());
        }

        this.map = new DoubleArrayTrie(treeMap);
    }

    public boolean contains(char ch) {
        return map.indexOf(ch) != -1;
    }

}

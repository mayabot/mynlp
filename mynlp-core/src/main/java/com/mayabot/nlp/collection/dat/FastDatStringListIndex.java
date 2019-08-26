package com.mayabot.nlp.collection.dat;

import java.util.List;
import java.util.TreeMap;

/**
 * 给一个没有重复的字符串数组建立索引，可以快速查询字符串在数组中的位置
 *
 * @author jimichan
 */
public class FastDatStringListIndex {

    private DoubleArrayTrieStringIntMap map;

    public FastDatStringListIndex(List<String> list) {
        init(list);
    }

    private void init(List<String> list) {

        TreeMap<String, Integer> treeMap = new TreeMap<>();
        int c = 0;
        for (String e : list) {
            treeMap.put(e, c++);
        }

        if (c != list.size()) {
            throw new RuntimeException("list must has unique element");
        }

        this.map = new DoubleArrayTrieStringIntMap(treeMap);
    }

    /**
     * 查询字符串的下班
     *
     * @param ch
     * @return 返回-1表示不存在
     */
    public int indexOf(String ch) {
        return map.indexOf(ch);
    }

}

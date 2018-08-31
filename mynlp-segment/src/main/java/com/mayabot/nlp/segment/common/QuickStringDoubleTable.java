package com.mayabot.nlp.segment.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import java.util.*;

/**
 * 一个定制的数据结构,快速根据String，String查询对应的double值。
 * 只能在小数据范围的情况下使用
 *
 * @author jimichan
 */
public class QuickStringDoubleTable {

    private int[] labelBase;
    private int labelSize;

    double[] data;

    public QuickStringDoubleTable(Table<String, String, Double> table) {
        ArrayList<String> labelList = Lists.newArrayList(table.rowKeySet());

        labelBase = findABase(labelList);
        labelSize = labelBase.length;


        data = new double[labelSize * labelSize];

        for (String rowKey : table.rowKeySet()) {
            for (String colKey : table.columnKeySet()) {
                int rowid = labelBase[rowKey.hashCode() % labelSize];
                int colid = labelBase[colKey.hashCode() % labelSize];

                data[rowid * labelSize + colid] = table.get(rowKey, colKey);
            }
        }
    }

    /**
     * 这个方法执行速度应该想飞一样
     * @param row
     * @param col
     * @return
     */
    public double get(String row, String col) {
        int rowid = labelBase[row.hashCode() % labelSize];
        int colid = labelBase[col.hashCode() % labelSize];
        if (rowid == -1 || colid == -1) {
            return Double.MIN_VALUE;
        }
        return data[rowid * labelSize + colid];
    }

    public static int[] findABase(List<String> tags) {

        int len = tags.size() * 10;

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            Set<Integer> set = new HashSet<>();
            boolean notGood = false;
            for (String tag : tags) {
                int code = tag.hashCode() % len;
                if (set.contains(code)) {
                    notGood = true;
                    break;
                }
                set.add(code);
            }
            if (notGood) {
                len++;
            } else {
                int[] base = new int[len];
                Arrays.fill(base, -1);
                for (int i1 = 0; i1 < tags.size(); i1++) {
                    String tag = tags.get(i1);
                    int index = tag.hashCode() % base.length;
                    base[index] = i1;
                }
                return base;
            }
        }

        throw new RuntimeException("findBase not find");
    }

}

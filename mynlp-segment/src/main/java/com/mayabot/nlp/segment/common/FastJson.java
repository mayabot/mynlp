package com.mayabot.nlp.segment.common;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class FastJson {

    public static String toJson(List<Integer> intList) {
        if (intList.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("[,");
        for (Integer i : intList) {
            sb.append(i);
            sb.append(',');
        }
        sb.append("]");

        return sb.toString();

    }

    public static List<Integer> fromJsonListInteger(String json) {
        if (json.equals("[]")) {
            return Lists.newArrayListWithExpectedSize(1);
        }

        String[] split = json.split(",");

        ArrayList<Integer> list = Lists.newArrayListWithCapacity(split.length - 2);

        int max = split.length - 1;
        for (int i = 1; i < max; i++) {
            list.add(Integer.parseInt(split[i]));
        }

        return list;
    }
}

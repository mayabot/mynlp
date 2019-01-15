package com.mayabot.nlp.utils;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Set;

public class CharNormUtils {

    private static char[] table = new char[65535];

    static {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try {

                List<String> lines = Resources.readLines(Resources.getResource(
                        CharNormUtils.class, "char_norm"), Charsets.UTF_8);


                Set<String> left = Sets.newHashSet();
                List<String> right = Lists.newArrayList();
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (line.isEmpty() || !line.contains("=")) {
                        continue;
                    }
                    int x = line.indexOf("=");
                    if (x != 1 && line.length() != 3) {
                        System.err.println("Error " + line);
                        continue;
                    }

                    String first = line.substring(0, 1);
                    String second = line.substring(2, 3);
                    left.add(first);
                    right.add(second);
                    if (first.length() == 1 && second.length() == 1) {
                        table[first.charAt(0)] = second.charAt(0);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });

    }

    public static char convert(char input) {
        char c = table[input];

        if (c != 0) {
            return c;
        } else {
            return input;
        }
    }

    public static void convert(char[] chars) {
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            char to = table[c];
            if (to != 0) {
                chars[i] = to;
            }
        }
    }

    public static String convert(String input) {
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            char to = table[c];
            if (to != 0) {
                chars[i] = to;
            }
        }
        return new String(chars);
    }

    public static void main(String[] args) throws Exception {

        System.out.println(convert("今日/t 股价/n 指数/n ＝/w (/w 今日/t 市价/n 总值/n //w 基期/n 市价/n 总值/n )/w ×/w 100/nx 。/w 市价/n 总值/n 为/v 当天/t 全部/m 股票/n 的/u 收盘价/n 乘以/v 总/b 发行/vn 股数/n 。/w 这样/r ,/w 以/p \"/w 基期/n \"/w 为/v 参照系/n ,/w \"/w 今日/t \"/w 股价/n 的/u 涨涨落落/vn 也/d 就/d 一目了然/i 了/y 。/w"));

    }

}

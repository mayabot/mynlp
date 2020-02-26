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
        final int len = input.length();
        int find = -1;
        for (int i = 0; i < len; i++) {
            if(table[input.charAt(i)]!=0){
                find = i;
                break;
            }
        }

        if (find == -1) {
            return input;
        }

        char[] chars = input.toCharArray();
        for (int i = find; i < chars.length; i++) {
            char c = chars[i];
            char to = table[c];
            if (to != 0) {
                chars[i] = to;
            }
        }
        return new String(chars);
    }

}

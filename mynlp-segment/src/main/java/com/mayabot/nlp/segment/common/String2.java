package com.mayabot.nlp.segment.common;

import org.jetbrains.annotations.NotNull;

public class String2 implements CharSequence {


    @NotNull
    private char[] chars;

    private int start = 0;
    private int end = 0;

    private int len = 0;


    public String2(@NotNull char[] chars) {
        this.chars = chars;
        start = 0;
        this.end = chars.length;
        len = chars.length;
    }

    public String2(@NotNull char[] chars, int start, int end) {
        this.chars = chars;
        this.start = start;
        this.end = end;
        this.len = end - start;
    }

    public void setStartEnd(int start, int end) {
        this.start = start;
        this.end = end;
        this.len = end - start;
    }

    public int getStart() {
        return start;
    }

    @Override
    public int length() {
        return len;
    }

    @Override
    public char charAt(int index) {
        return chars[index + start];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new String2(chars, this.start + start, this.start + end);
    }

    @Override
    public String toString() {
        return new String(chars, start, len);
    }

    public static void main(String[] args) {
        String2 s2 = new String2("123456789".toCharArray(), 1, 5);

        System.out.println(s2);
        System.out.println(s2.subSequence(1, 2));

    }
}

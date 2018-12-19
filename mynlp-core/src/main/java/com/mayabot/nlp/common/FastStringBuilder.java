package com.mayabot.nlp.common;

/**
 * 在固定buffer长度下。实现高性能的定制的StringBuilder
 *
 * @author jimichan
 */
public class FastStringBuilder implements CharSequence {

    private char[] text;

    private int length = 0;

    public FastStringBuilder(int max) {
        text = new char[max];
    }

    public final void append(char c1, char c2) {
        text[length++] = c1;
        text[length++] = c2;
    }

    public final void append(char c1, char c2, char c3, char c4) {
        text[length++] = c1;
        text[length++] = c2;
        text[length++] = c3;
        text[length++] = c4;
    }

    public final void set2(char c1, char c2) {
        text[0] = c1;
        text[1] = c2;
        length = 2;
    }

    public final void set4(char c1, char c2, char c3, char c4) {
        text[0] = c1;
        text[1] = c2;
        text[2] = c3;
        text[3] = c4;
        length = 4;
    }


    public final void clear() {
        length = 0;
    }

    @Override
    public final int length() {
        return length;
    }

    @Override
    public final char charAt(int index) {
        return text[index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return null;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(text[i]);
        }
        return sb.toString();
    }
}

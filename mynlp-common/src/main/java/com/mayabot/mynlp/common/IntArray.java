package com.mayabot.mynlp.common;

public class IntArray {

    public final int[] data;
    final int size;

    IntArray(int[] data, int size) {
        this.data = data;
        this.size = size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public int get(int idx) {
        if (idx >= size) {
            throw new ArrayIndexOutOfBoundsException(idx);
        }
        return data[idx];
    }

    public void put(int idx, int value) {
        if (idx >= size) {
            throw new ArrayIndexOutOfBoundsException(idx);
        }
        data[idx] = value;
    }

    public int[] getBuffer() {
        return data;
    }
}

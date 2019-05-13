package com.mayabot.nlp.common;

public final class Pair<T, R> {

    public T first;
    public R second;

    public Pair(T first, R second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ')';
    }
}

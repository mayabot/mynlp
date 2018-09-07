package org.trie4j.util;

import java.util.Iterator;

public abstract class IterableAdapter<T, U> implements Iterable<U> {
    public IterableAdapter(Iterable<T> orig) {
        this.orig = orig;
    }

    @Override
    public Iterator<U> iterator() {
        final Iterator<T> it = orig.iterator();
        return new Iterator<U>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public U next() {
                return convert(it.next());
            }

            @Override
            public void remove() {
                it.remove();
            }

        };
    }

    protected abstract U convert(T value);

    private Iterable<T> orig;
}

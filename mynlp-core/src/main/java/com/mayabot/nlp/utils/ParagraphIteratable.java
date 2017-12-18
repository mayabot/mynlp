package com.mayabot.nlp.utils;

import com.google.common.collect.AbstractIterator;

import java.util.Iterator;

/**
 * ParagraphReader包装成iterable对象
 *
 * @author jimichan
 */
public class ParagraphIteratable implements Iterable<String> {

    private ParagraphReader reader;

    public ParagraphIteratable(ParagraphReader reader) {
        this.reader = reader;
    }

    @Override
    public Iterator<String> iterator() {

        Iterator<String> iterator = new AbstractIterator<String>() {

            @Override
            protected String computeNext() {
                try {
                    String next = reader.next();
                    if (next == null) {
                        return endOfData();
                    }
                    return next;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        return iterator;
    }
}

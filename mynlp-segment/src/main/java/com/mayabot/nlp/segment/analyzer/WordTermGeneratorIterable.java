package com.mayabot.nlp.segment.analyzer;

import com.google.common.collect.AbstractIterator;
import com.mayabot.nlp.segment.WordTerm;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class WordTermGeneratorIterable implements Iterable<WordTerm> {

    WordTermGenerator generator;

    public WordTermGeneratorIterable(WordTermGenerator generator) {
        this.generator = generator;
    }

    @Override
    public Iterator<WordTerm> iterator() {
        return new AbstractIterator<WordTerm>() {
            @Override
            protected WordTerm computeNext() {
                WordTerm word = generator.nextWord();
                if (word == null) {
                    return endOfData();
                } else {
                    return word;
                }
            }
        };
    }

    public Stream<WordTerm> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
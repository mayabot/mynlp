package com.mayabot.nlp.segment.reader;

import com.mayabot.nlp.segment.LexerReader;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.WordTermSequence;

import java.io.Reader;
import java.util.Iterator;
import java.util.function.Predicate;

public abstract class FilterLexerReader implements LexerReader, Predicate<WordTerm> {

    private final LexerReader source;

    public FilterLexerReader(LexerReader source) {
        this.source = source;
    }

    @Override
    public WordTermSequence scan(Reader reader) {
        Iterator<WordTerm> iterator = source.scan(reader).iterator();
        Iterator<WordTerm> change = new FilterWordItemIterator(iterator, this);
        return new WordTermSequence(change);
    }

    @Override
    public WordTermSequence scan(String text) {
        Iterator<WordTerm> iterator = source.scan(text).iterator();
        Iterator<WordTerm> change = new FilterWordItemIterator(iterator, this);
        return new WordTermSequence(change);
    }
}

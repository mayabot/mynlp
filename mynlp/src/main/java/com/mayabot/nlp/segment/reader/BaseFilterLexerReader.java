package com.mayabot.nlp.segment.reader;

import com.mayabot.nlp.segment.LexerReader;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.WordTermSequence;

import java.io.Reader;
import java.util.Iterator;
import java.util.function.Predicate;

public abstract class BaseFilterLexerReader implements LexerReader, Predicate<WordTerm> {

    private final LexerReader source;

    private boolean enable = true;

    public BaseFilterLexerReader(LexerReader source) {
        this.source = source;
    }

    public LexerReader getSource() {
        return source;
    }

    @Override
    public WordTermSequence scan(Reader reader) {
        WordTermSequence wts = source.scan(reader);
        if (!enable) {
            return wts;
        }
        Iterator<WordTerm> iterator = wts.iterator();
        Iterator<WordTerm> change = new FilterWordItemIterator(iterator, this);
        return new WordTermSequence(change);
    }

    @Override
    public WordTermSequence scan(String text) {
        WordTermSequence wts = source.scan(text);
        if (!enable) {
            return wts;
        }
        Iterator<WordTerm> iterator = wts.iterator();
        Iterator<WordTerm> change = new FilterWordItemIterator(iterator, this);
        return new WordTermSequence(change);
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}

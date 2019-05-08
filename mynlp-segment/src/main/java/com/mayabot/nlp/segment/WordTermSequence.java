package com.mayabot.nlp.segment;

import com.google.common.collect.Iterables;
import com.mayabot.nlp.common.FastCharReader;
import com.mayabot.nlp.common.ParagraphReader;
import com.mayabot.nlp.common.ParagraphReaderSmart;
import com.mayabot.nlp.common.ParagraphReaderString;
import com.mayabot.nlp.segment.reader.LexerIterator;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * WordTerm序列。表示一个未知长度的WordTerm序列，对应Reader的输出对象。
 *
 * @author jimichan
 */
public final class WordTermSequence implements Iterable<WordTerm> {

    private final Iterator<WordTerm> source;

    public WordTermSequence(Iterator<WordTerm> source) {
        this.source = source;
    }

    public WordTermSequence(Iterable<WordTerm> source) {
        this(source.iterator());
    }

    public WordTermSequence(Lexer lexer, ParagraphReader paragraphReader) {
        this(new LexerIterator(lexer, paragraphReader));
    }

    public WordTermSequence(Lexer lexer, Reader reader) {
        this(new LexerIterator(lexer, new ParagraphReaderSmart(new FastCharReader(reader, 128), 1024)));
    }

    public WordTermSequence(Lexer lexer, String text) {
        this(new LexerIterator(lexer, new ParagraphReaderString(text)));
    }

    @NotNull
    @Override
    public Iterator<WordTerm> iterator() {
        return source;
    }

    public Iterable<String> toWordSequence() {
        return Iterables.transform(this, wordTerm -> wordTerm != null ? wordTerm.getWord() : null);
    }

    public Stream<WordTerm> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public Sentence toSentence() {
        return Sentence.of(this);
    }
}

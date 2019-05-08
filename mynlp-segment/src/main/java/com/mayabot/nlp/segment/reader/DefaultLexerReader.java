package com.mayabot.nlp.segment.reader;

import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.LexerReader;
import com.mayabot.nlp.segment.WordTermSequence;

import java.io.Reader;

public class DefaultLexerReader implements LexerReader {

    private final Lexer lexer;

    public DefaultLexerReader(Lexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public WordTermSequence scan(Reader reader) {
        return new WordTermSequence(lexer, reader);
    }

    @Override
    public WordTermSequence scan(String text) {
        return new WordTermSequence(lexer, text);
    }
}

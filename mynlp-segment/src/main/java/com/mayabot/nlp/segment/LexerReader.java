package com.mayabot.nlp.segment;

import com.mayabot.nlp.segment.reader.DefaultLexerReader;
import com.mayabot.nlp.segment.reader.PunctuationFilter;
import com.mayabot.nlp.segment.reader.StopwordFilter;

import java.io.Reader;

/**
 * 面向Reader的词法分析器。主要解决从Reader返回分词结果。
 *
 * @author jimichan
 * @since 2.1.0
 */
public interface LexerReader {

    WordTermSequence scan(Reader reader);

    WordTermSequence scan(String text);

    static LexerReader from(Lexer lexer) {
        return new DefaultLexerReader(lexer);
    }

    static LexerReader filter(Lexer lexer, boolean punctuation, boolean stopWord) {

        LexerReader reader = new DefaultLexerReader(lexer);
        if (punctuation) {
            reader = new PunctuationFilter(reader);
        }
        if (stopWord) {
            reader = new StopwordFilter(reader);
        }
        return reader;
    }

}

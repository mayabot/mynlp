package com.mayabot.nlp.module.lucene;

import com.mayabot.nlp.segment.LexerReader;
import com.mayabot.nlp.segment.WordTermIterableMode;
import org.apache.lucene.analysis.Analyzer;

/**
 * @author jimichan
 */
public class MynlpAnalyzer extends Analyzer {

    private final LexerReader lexerReader;

    private WordTermIterableMode mode = WordTermIterableMode.TOP;


    public MynlpAnalyzer(LexerReader lexerReader) {
        this.lexerReader = lexerReader;
    }

    public MynlpAnalyzer(LexerReader lexerReader, WordTermIterableMode mode) {
        this.lexerReader = lexerReader;
        this.mode = mode;
    }


    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {
        return new TokenStreamComponents(new MynlpTokenizer(lexerReader, mode));
    }

}

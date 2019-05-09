package com.mayabot.nlp.lucene;

import com.mayabot.nlp.segment.LexerReader;
import org.apache.lucene.analysis.Analyzer;

/**
 * @author jimichan
 */
public class MynlpAnalyzer extends Analyzer {

    private LexerReader lexerReader;

    public MynlpAnalyzer(LexerReader lexerReader) {
        this.lexerReader = lexerReader;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {

        final MynlpTokenizer src = new MynlpTokenizer(lexerReader);

        return new TokenStreamComponents(src, src);
    }

}

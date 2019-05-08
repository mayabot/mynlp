package com.mayabot.nlp.lucene;

import com.mayabot.nlp.segment.LexerReader;
import org.apache.lucene.analysis.Analyzer;

/**
 * @author jimichan
 */
public class MynlpLuceneAnalyzer extends Analyzer {

    private LexerReader lexerReader;

    public MynlpLuceneAnalyzer(LexerReader lexerReader) {
        this.lexerReader = lexerReader;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {

        final MynlpLuceneTokenizer src = new MynlpLuceneTokenizer(lexerReader);

        return new TokenStreamComponents(src, src);
    }

}

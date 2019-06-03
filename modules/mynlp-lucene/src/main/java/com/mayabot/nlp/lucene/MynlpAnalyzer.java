package com.mayabot.nlp.lucene;

import com.mayabot.nlp.segment.LexerReader;
import org.apache.lucene.analysis.Analyzer;

/**
 * @author jimichan
 */
public class MynlpAnalyzer extends Analyzer {

    private MynlpTokenizer tokenizer;

    public MynlpAnalyzer(MynlpTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public MynlpAnalyzer(LexerReader reader) {
        this.tokenizer = new MynlpTokenizer(reader);
    }
    public MynlpAnalyzer(LexerReader reader,IterableMode mode) {
        this.tokenizer = new MynlpTokenizer(reader,mode);
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {
        return new TokenStreamComponents(tokenizer);
    }

}

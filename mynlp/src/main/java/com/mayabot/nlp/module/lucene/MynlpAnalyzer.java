package com.mayabot.nlp.module.lucene;

import com.mayabot.nlp.segment.LexerReader;
import com.mayabot.nlp.segment.WordTermIterableMode;
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

    public MynlpAnalyzer(LexerReader reader, WordTermIterableMode mode) {
        this.tokenizer = new MynlpTokenizer(reader, mode);
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {
        return new TokenStreamComponents(tokenizer);
    }

}

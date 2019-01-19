package com.mayabot.nlp.lucene;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.Tokenizers;
import com.mayabot.nlp.segment.analyzer.BaseMynlpAnalyzer;
import com.mayabot.nlp.segment.analyzer.PunctuationFilter;
import com.mayabot.nlp.segment.analyzer.StopwordFilter;
import com.mayabot.nlp.segment.analyzer.WordTermGenerator;
import org.apache.lucene.analysis.Analyzer;

/**
 * @author jimichan
 */
public class MynlpLuceneAnalyzer extends Analyzer {

    private MynlpTokenizer tokenizer;

    private boolean stopWord = true;
    private boolean punctuation = true;

    public MynlpLuceneAnalyzer() {
        this(Tokenizers.coreTokenizerBuilder().setEnablePOS(false).build());
    }

    public MynlpLuceneAnalyzer(MynlpTokenizer mynlpTokenizer) {
        this.tokenizer = mynlpTokenizer;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {

        StandardMynlpAnalyzer analyzer = new StandardMynlpAnalyzer(tokenizer);
        analyzer.punctuation = punctuation;
        analyzer.stopWord = stopWord;

        final MynlpLuceneTokenizer src = new MynlpLuceneTokenizer(analyzer);

        return new TokenStreamComponents(src, src);
    }


    public boolean isStopWord() {
        return stopWord;
    }

    public MynlpLuceneAnalyzer setStopWord(boolean stopWord) {
        this.stopWord = stopWord;
        return this;
    }

    public boolean isPunctuation() {
        return punctuation;
    }

    public MynlpLuceneAnalyzer setPunctuation(boolean punctuation) {
        this.punctuation = punctuation;
        return this;
    }

    private class StandardMynlpAnalyzer extends BaseMynlpAnalyzer {

        boolean stopWord = true;
        boolean punctuation = true;

        public StandardMynlpAnalyzer(MynlpTokenizer tokenizer) {
            super(tokenizer);
        }


        public StandardMynlpAnalyzer() {
            this(Tokenizers.coreTokenizer());
        }

        @Override
        protected WordTermGenerator warp(WordTermGenerator base) {
            if (punctuation) {
                base = new PunctuationFilter(base);
            }
            if (stopWord) {
                base = new StopwordFilter(base);
            }
            return base;
        }

    }


}

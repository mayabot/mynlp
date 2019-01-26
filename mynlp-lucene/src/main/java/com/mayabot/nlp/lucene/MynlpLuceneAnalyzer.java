package com.mayabot.nlp.lucene;

import com.mayabot.nlp.segment.MynlpAnalyzer;
import org.apache.lucene.analysis.Analyzer;

/**
 * @author jimichan
 */
public class MynlpLuceneAnalyzer extends Analyzer {

    private MynlpAnalyzer mynlpAnalyzer;

    public MynlpLuceneAnalyzer(MynlpAnalyzer mynlpAnalyzer) {
        this.mynlpAnalyzer = mynlpAnalyzer;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {

        final MynlpLuceneTokenizer src = new MynlpLuceneTokenizer(mynlpAnalyzer);

        return new TokenStreamComponents(src, src);
    }


}

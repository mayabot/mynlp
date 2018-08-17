package com.mayabot.mylp.lucene;

import com.mayabot.nlp.segment.MynlpSegments;
import com.mayabot.nlp.segment.support.DefaultMynlpAnalyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

import java.util.Map;

public class MynlpLuceneTokenizerFactory extends TokenizerFactory {


    private Map<String, String> args;

    public MynlpLuceneTokenizerFactory(Map<String, String> args) {
        super(args);
        this.args = args;
    }

    @Override
    public Tokenizer create(AttributeFactory factory) {
        return new MynlpLuceneTokenizer(new DefaultMynlpAnalyzer(MynlpSegments.nlpTokenizer()));
    }

}
package com.mayabot.nlp.segment.analyzer;

import com.mayabot.nlp.segment.MynlpAnalyzer;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.WordTerm;

import java.io.Reader;
import java.util.stream.Stream;

/**
 * @author jimichan
 */
public abstract class BaseMynlpAnalyzer implements MynlpAnalyzer {

    private MynlpTokenizer tokenizer;

    protected abstract WordTermGenerator warp(WordTermGenerator base);

    public BaseMynlpAnalyzer(MynlpTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    @Override
    public Iterable<WordTerm> parse(Reader reader) {
        WordTermGenerator generator = new TokenWordTermGenerator(reader, tokenizer);
        generator = warp(generator);
        return new WordTermGeneratorIterable(generator);
    }

    @Override
    public Stream<WordTerm> stream(Reader reader) {
        WordTermGenerator generator = new TokenWordTermGenerator(reader, tokenizer);
        generator = warp(generator);
        return new WordTermGeneratorIterable(generator).stream();
    }


    @Override
    public Iterable<WordTerm> parse(String text) {
        WordTermGenerator generator = new TokenWordTermGenerator(text, tokenizer);
        generator = warp(generator);
        return new WordTermGeneratorIterable(generator);
    }

    @Override
    public Stream<WordTerm> stream(String text) {
        WordTermGenerator generator = new TokenWordTermGenerator(text, tokenizer);
        generator = warp(generator);
        return new WordTermGeneratorIterable(generator).stream();
    }

}

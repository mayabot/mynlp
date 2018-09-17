/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

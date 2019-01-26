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

import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.MynlpTokenizer;

import java.util.HashSet;
import java.util.Set;

/**
 * 标准的，包含过滤标点符号和停用词.
 * 用户可以实现自定义的
 *
 * @author jimichan
 */
public class DefaultMynlpAnalyzer extends BaseMynlpAnalyzer {


    private boolean filterPunctuaction = true;

    private boolean fillStopWord = false;

    Set<String> stopWords;


    public DefaultMynlpAnalyzer(MynlpTokenizer tokenizer, Set<String> stopWords) {
        super(tokenizer);
        setStopWords(stopWords);
    }

    public DefaultMynlpAnalyzer(MynlpTokenizer tokenizer) {
        this(tokenizer, null);
    }

    @Override
    protected WordTermGenerator warp(WordTermGenerator base) {

        if (filterPunctuaction) {
            base = new PunctuationFilter(base);
        }
        if (fillStopWord) {
            base = new StopwordFilter(base, stopWords);
        }

        return base;
    }

    public boolean isFilterPunctuaction() {
        return filterPunctuaction;
    }

    public DefaultMynlpAnalyzer setFilterPunctuaction(boolean filterPunctuaction) {
        this.filterPunctuaction = filterPunctuaction;
        return this;
    }

    public boolean isFillStopWord() {
        return fillStopWord;
    }

    public DefaultMynlpAnalyzer setFillStopWord(boolean fillStopWord) {
        this.fillStopWord = fillStopWord;
        return this;
    }

    public Set<String> getStopWords() {
        return stopWords;
    }

    public DefaultMynlpAnalyzer setStopWords(Set<String> stopWords) {

        this.stopWords = new HashSet<>();

        Set<String> defaultSet = Mynlps.instanceOf(StopWordDict.class).getSet();

        if (stopWords == null) {
            this.stopWords.addAll(defaultSet);
        } else {
            this.stopWords.addAll(stopWords);
        }

        return this;
    }

}

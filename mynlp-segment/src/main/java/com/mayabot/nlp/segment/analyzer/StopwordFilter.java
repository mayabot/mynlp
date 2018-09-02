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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.mayabot.nlp.segment.WordTerm;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 过滤停用词
 *
 * @author jimichan
 */
public class StopwordFilter extends FilterdWordTermGenerator implements Predicate<WordTerm>,
        com.google.common.base.Predicate<WordTerm> {

    static Set<String> defaultSet = ImmutableSet.of();

    static {
        URL url = Resources.getResource("maya_data/dictionary/stopwords.txt");
        try {
            defaultSet = ImmutableSet.copyOf(Resources.asCharSource(url, Charsets.UTF_8).readLines().stream().collect(Collectors.toSet()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Set<String> stopWords;

    public StopwordFilter(WordTermGenerator base, Set<String> stopwords) {
        super(base);
        this.stopWords = stopWords;

        stopWords = new HashSet<>();
        if (stopwords == null) {
            stopWords.addAll(defaultSet);
        } else {
            this.stopWords.addAll(stopwords);
        }
    }

    public StopwordFilter(WordTermGenerator base) {
        this(base, null);
    }

    public Set<String> getStopWords() {
        return stopWords;
    }

    public void add(String word) {
        this.stopWords.add(word);
    }

    public void remove(String word) {
        stopWords.remove(word);
    }


    @Override
    public boolean apply(WordTerm term) {
        return !stopWords.contains(term.word);
    }

    @Override
    public boolean test(WordTerm term) {
        return !stopWords.contains(term.word);
    }
}

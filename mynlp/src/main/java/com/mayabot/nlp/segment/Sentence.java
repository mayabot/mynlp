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
package com.mayabot.nlp.segment;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 中文句子
 */
public final class Sentence implements Iterable<WordTerm> {

    public static Sentence of() {
        return new Sentence();
    }

    public static Sentence of(List<WordTerm> words) {
        return new Sentence(words);
    }

    public static Sentence of(Iterable<WordTerm> words) {
        return new Sentence(words);
    }

    private List<WordTerm> words;

    private Sentence(List<WordTerm> words) {
        this.words = words;
    }

    private Sentence(Iterable<WordTerm> words) {
        this.words = Lists.newArrayList(words);
    }

    private Sentence() {
        this.words = ImmutableList.of();
    }

    public String toPlainString() {
        return Joiner.on(' ').join(toWordList());
    }

    public String pkuFormat() {
        return toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        int size = words.size();
        int last = size - 1;

        for (int i = 0; i < size; i++) {
            WordTerm term = words.get(i);

            if (term.hasSubword()) {
                sb.append("[");
                sb.append(new Sentence(term.getSubword()).toString());
                sb.append("]");
                if (term.getNature() != null) {
                    sb.append("/");
                    sb.append(term.getNature());
                }
                sb.append(" ");
            } else {
                sb.append(term.word);
                if (term.getNature() != null) {
                    sb.append("/");
                    sb.append(term.getNature());
                }

                if (i < last) {
                    sb.append(" ");
                }
            }
        }

        return sb.toString();
    }

    @NotNull
    public List<String> toWordList() {
        List<String> list = Lists.newArrayListWithExpectedSize(words.size());
        for (WordTerm w : words) {
            list.add(w.word);
        }
        return list;
    }


    public List<WordTerm> toList() {
        return words;
    }

    public Stream<WordTerm> stream() {
        return StreamSupport.stream(spliterator(), false);
    }


    @NotNull
    @Override
    public Iterator<WordTerm> iterator() {
        return words.iterator();
    }
}

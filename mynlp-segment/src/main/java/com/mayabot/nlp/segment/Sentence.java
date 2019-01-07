package com.mayabot.nlp.segment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * 中文句子
 */
public final class Sentence {

    public static final Sentence of(List<WordTerm> words) {
        return new Sentence(words);
    }

    private List<WordTerm> words;

    public Sentence(List<WordTerm> words) {
        this.words = words;
    }

    public Sentence() {
        this.words = ImmutableList.of();
    }

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

                if (i < last) sb.append(" ");
            }
        }

        return sb.toString();
    }

    public List<String> asStringList() {
        List<String> list = Lists.newArrayListWithExpectedSize(words.size());
        for (WordTerm w : words) {
            list.add(w.word);
        }
        return list;
    }

    public String pkuFormat() {
        return toString();
    }

    public List<WordTerm> asWordList() {
        return words;
    }
}

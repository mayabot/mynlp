package com.mayabot.nlp.segment.dictionary;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.List;

public class CorrectionWord {
    public String path;
    public String raw;
    public int[] words;

    static Splitter splitter = Splitter.on("/").trimResults().omitEmptyStrings();

    public int[] getWords() {
        return words;
    }

    public String getPath() {
        return path;
    }

    public String getRaw() {
        return raw;
    }

    /**
     * 第几套/房
     *
     * @param line
     * @return
     */
    public static CorrectionWord parse(String line) {
        CorrectionWord adjustWord = new CorrectionWord();
        adjustWord.raw = line.trim();

        List<String> list = splitter.splitToList(adjustWord.raw);
        adjustWord.path = Joiner.on("").join(list);
        List<Integer> words = Lists.newArrayList();
        for (String s : list) {
            words.add(s.length());
        }

        adjustWord.words = Ints.toArray(words);
        return adjustWord;
    }

    @Override
    public String toString() {
        return "CorrectionWord{" + "path='" + path + '\'' +
                ", raw='" + raw + '\'' +
                ", words=" + words +
                '}';
    }

}
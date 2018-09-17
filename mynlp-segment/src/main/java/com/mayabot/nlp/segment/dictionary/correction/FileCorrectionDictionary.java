package com.mayabot.nlp.segment.dictionary.correction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import com.mayabot.nlp.segment.dictionary.CorrectionDictionary;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.TreeMap;

/**
 * File版本CorrectionDictionary
 * 文件内容格式：
 * 第几套/房
 * <p>
 * 一行一个规则
 *
 * @author jimichan
 */
public class FileCorrectionDictionary implements CorrectionDictionary {

    private TreeMap<String, AdjustWord> dict;

    private DoubleArrayTrie<AdjustWord> trie;

    public FileCorrectionDictionary(File file, Charset charset) throws IOException {
        TreeMap<String, AdjustWord> dict = Maps.newTreeMap();

        ImmutableList<String> lines = Files.asCharSource(file, charset).readLines();

        for (String line : lines) {

            AdjustWord adjustWord = AdjustWord.parse(line);

            dict.put(adjustWord.path, adjustWord);
        }

        trie = new DoubleArrayTrieBuilder<AdjustWord>().build(dict);
    }

    @Override
    public DoubleArrayTrie<AdjustWord> getTrie() {
        return trie;
    }

}

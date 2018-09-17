package com.mayabot.nlp.segment.dictionary.custom;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import com.mayabot.nlp.segment.dictionary.CustomDictionary;
import com.mayabot.nlp.segment.dictionary.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.TreeMap;

/**
 * File版本CustomDictionary
 *
 * @author jimichan
 */
public class FileCustomDictionary implements CustomDictionary {

    private TreeMap<String, NatureAttribute> dict;

    private DoubleArrayTrie<NatureAttribute> trie;

    public FileCustomDictionary(File file, Charset charset) throws IOException {
        TreeMap<String, NatureAttribute> dict = Maps.newTreeMap();

        ImmutableList<String> lines = Files.asCharSource(file, charset).readLines();

        for (String line : lines) {

            String[] params = line.split("\\s");

            int natureCount = (params.length - 1) / 2;

            NatureAttribute attribute;
            if (natureCount == 0) {
                attribute = NatureAttribute.create1000(Nature.n);
            } else {
                attribute = NatureAttribute.create(params);
            }

            dict.put(params[0], attribute);
        }

        trie = new DoubleArrayTrieBuilder<NatureAttribute>().build(dict);
    }

    @Override
    public DoubleArrayTrie<NatureAttribute> getTrie() {
        return trie;
    }

}

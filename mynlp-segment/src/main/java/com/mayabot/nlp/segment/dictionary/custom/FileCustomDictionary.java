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

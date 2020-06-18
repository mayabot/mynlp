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

package com.mayabot.nlp.segment.plugins.customwords;

import com.mayabot.nlp.collection.dat.DoubleArrayTrieStringIntMap;
import com.mayabot.nlp.common.Guava;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.TreeMap;

/**
 * File版本CustomDictionary
 * 不管什么格式 壁式网球 1
 * 只取第一段，后面的忽略
 *
 * @author jimichan
 */
public class FileCustomDictionary implements CustomDictionary {

    private DoubleArrayTrieStringIntMap trie;

    public FileCustomDictionary(File file, Charset charset) throws IOException {
        TreeMap<String, Integer> dict = new TreeMap();

        List<String> lines = Guava.readLines(file, charset);

        for (String line : lines) {

            String[] params = line.split("\\s");

            dict.put(params[0], 1000);

        }

        trie = new DoubleArrayTrieStringIntMap(dict);
    }

    @Override
    public DoubleArrayTrieStringIntMap getTrie() {
        return trie;
    }

}

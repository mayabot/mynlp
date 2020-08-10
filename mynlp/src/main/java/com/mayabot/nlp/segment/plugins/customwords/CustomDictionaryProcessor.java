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

import com.mayabot.nlp.algorithm.collection.dat.DoubleArrayTrieStringIntMap;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.common.BaseSegmentComponent;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.Collections;

/**
 * 自定义词典的合并处理器.
 * <p>
 * 小词合并为大词
 * 但是不去解决  AAA BBB CCC 有一个自定义词汇 ABBBC 这个时候不能去拆分，变更原有路径
 * 只能解决 A BB C 然后有自定义词 ABBC 那可以把他们联合起来
 *
 * @author jimichan
 */
public class CustomDictionaryProcessor extends BaseSegmentComponent implements WordpathProcessor {

    private CustomDictionary dictionary;

    public CustomDictionaryProcessor(CustomDictionary dictionary) {
        super(LEVEL2);
        this.dictionary = dictionary;
    }

    @Override
    public Wordpath process(Wordpath wordPath) {

        DoubleArrayTrieStringIntMap dat = dictionary.getTrie();

        if (dat == null) {
            return wordPath;
        }

        Wordnet wordnet = wordPath.getWordnet();
        char[] text = wordnet.getCharArray();

        for (DoubleArrayTrieStringIntMap d : Collections.singleton(dat)) {
            if (d == null) {
                continue;
            }

            DoubleArrayTrieStringIntMap.DATMapMatcherInt datSearch = d.match(text, 0);

            while (datSearch.next()) {
                int offset = datSearch.getBegin();
                int length = datSearch.getLength();

                boolean willCutOtherWords = wordPath.willCutOtherWords(offset, length);

                if (!willCutOtherWords) {
                    if (wordnet.getVertex(offset, length) == null) {
                        wordPath.combine(offset, length);
                    } else {
                        // 也就是自定义词典里面包含了重复的词汇
                    }
                }
            }
        }

        return wordPath;
    }

    public CustomDictionary getDictionary() {
        return dictionary;
    }

    public CustomDictionaryProcessor setDictionary(CustomDictionary dictionary) {
        this.dictionary = dictionary;
        return this;
    }
}

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

package com.mayabot.nlp.segment.tokenizer.xprocessor;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.collection.dat.DATMapMatcher;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieMap;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.common.BaseMynlpComponent;
import com.mayabot.nlp.segment.dictionary.CustomDictionary;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

/**
 * 自定义词典的合并处理器.
 * <p>
 * 小词合并为大词
 * 但是不去解决  AAA BBB CCC 有一个自定义词汇 ABBBC 这个时候不能去拆分，变更原有路径
 * 只能解决 A BB C 然后有自定义词 ABBC 那可以把他们联合起来
 * 以后可以标记如果自定义词非常高的优先级或者只能从分词纠错中来解决
 *
 * @author jimichan
 */
public class CustomDictionaryProcessor extends BaseMynlpComponent implements WordpathProcessor {

    private CustomDictionary dictionary;

    private CoreDictionary coreDictionary;

    @Inject
    public CustomDictionaryProcessor(CustomDictionary dictionary, CoreDictionary coreDictionary) {
        this.dictionary = dictionary;
        this.coreDictionary = coreDictionary;
        this.setOrder(ORDER_MIDDLE - 10);

    }

    public CustomDictionaryProcessor(CustomDictionary dictionary) {
        this(dictionary, Mynlps.getInstance(CoreDictionary.class));
    }

    @Override
    public Wordpath process(Wordpath wordPath) {

        DoubleArrayTrieMap<Integer> dat = dictionary.getTrie();

        if (dat == null) {
            return wordPath;
        }

        Wordnet wordnet = wordPath.getWordnet();
        char[] text = wordnet.getCharArray();

        for (DoubleArrayTrieMap<Integer> d : ImmutableList.of(dat)) {
            if (d == null) {
                continue;
            }

            DATMapMatcher<Integer> datSearch = d.match(text, 0);

            while (datSearch.next()) {
                int offset = datSearch.getBegin();
                int length = datSearch.getLength();

                boolean willCutOtherWords = wordPath.willCutOtherWords(offset, length);

                if (!willCutOtherWords) {
                    if (wordnet.getVertex(offset, length) == null) {
                        // wordnet 中是否缺乏对应的节点，如果没有需要补上

                        Integer freq = datSearch.getValue();

                        Vertex v = wordPath.combine(offset, length);

                        //没有等效果词
                        //v.setWordInfo(coreDictionary.X_WORD_ID, CoreDictionary.X_TAG, attr);
                    } else {
                        //FIXME 要不要覆盖attr.
                        //也就是自定义词典里面包含了重复的词汇
                    }

                }
            }
        }

        return wordPath;
    }

    public CustomDictionary getDictionary() {
        return dictionary;
    }
}

/*
 *  Copyright 2017 mayabot.com authors. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mayabot.nlp.segment.xprocessor;

import com.google.inject.Inject;
import com.mayabot.nlp.collection.dat.DATMatcher;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.segment.WordPathProcessor;
import com.mayabot.nlp.segment.dictionary.CustomDictionary;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.WordPath;
import com.mayabot.nlp.segment.wordnet.Wordnet;

/**
 * 自定义词典的合并处理器
 * 小词合并为大词
 * 但是不去解决  AAA BBB CCC 有一个自定义词汇 ABBBC 这个时候不能去拆分，变更原有路径
 * 只能解决 A BB C 然后有自定义词 ABBC 那可以把他们联合起来
 * 以后可以标记如果自定义词非常高的优先级或者只能从分词纠错中来解决
 *
 * @author jimichan
 * @author fred
 */
public class CustomDictionaryXProcess implements WordPathProcessor {

    private CustomDictionary dictionary;

    private CoreDictionary coreDictionary;

    @Inject
    public CustomDictionaryXProcess(CustomDictionary dictionary, CoreDictionary coreDictionary) {
        this.dictionary = dictionary;
        this.coreDictionary = coreDictionary;
    }

    @Override
    public WordPath process(WordPath wordPath) {
        Wordnet wordnet = wordPath.getWordnet();
        boolean change = false;
        char[] text = wordnet.getCharArray();
        for (DoubleArrayTrie<NatureAttribute> d : dictionary.allDict()) {
            if (d == null) {
                continue;
            }

            //d.transition()

            DATMatcher<NatureAttribute> datSearch = d.match(text, 0);

            while (datSearch.next()) {
                int offset = datSearch.getBegin();
                int length = datSearch.getLength();

                //FIXME 这里的效率没有直接跳转的那么高

                //int wordId = datSearch.getIndex();

                // 需要检测是否真的联合了多个词汇
                boolean willCutOtherWords = wordPath.willCutOtherWords(offset, length);

                if (!willCutOtherWords) {
                    if (wordnet.getVertex(offset, length) == null) {
                        // wordnet 中是否缺乏对应的节点，如果没有需要补上

                        NatureAttribute attr = datSearch.getValue();

                        Vertex v = wordPath.combine(offset, length);
                        v.setWordInfo(coreDictionary.X_WORD_ID, CoreDictionary.TAG_CLUSTER, attr); //没有等效果词
                        change = true;
                    } else {
                        //FIXME 要不要覆盖attr.
                        //也就是自定义词典里面包含了重复的词汇
                    }

                }
            }
        }

        return wordPath;
    }
}

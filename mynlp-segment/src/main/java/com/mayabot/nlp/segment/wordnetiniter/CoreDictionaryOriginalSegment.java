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

package com.mayabot.nlp.segment.wordnetiniter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.collection.dat.DATMatcher;
import com.mayabot.nlp.segment.WordnetInitializer;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;

/**
 * 基于核心词典的基础切词器
 *
 * @author jimichan
 */
@Singleton
public class CoreDictionaryOriginalSegment implements WordnetInitializer {

    private CoreDictionary coreDictionary;

    @Inject
    public CoreDictionaryOriginalSegment(CoreDictionary coreDictionary) {
        this.coreDictionary = coreDictionary;
    }

    @Override
    public void init(Wordnet wordnet) {
        char[] text = wordnet.getCharArray();

        // 核心词典查询
        DATMatcher<NatureAttribute> searcher = coreDictionary.match(text, 0);

        while (searcher.next()) {
            int offset = searcher.getBegin();
            int length = searcher.getLength();
            int wordId = searcher.getIndex();

            Vertex v = new Vertex(length).setWordInfo(wordId, searcher.getValue()); //没有等效词

            wordnet.put(offset, v);
        }
    }


}

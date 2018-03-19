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

package com.mayabot.nlp.segment.wordprocessor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.collection.dat.DATMatcher;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.dictionary.CorrectionDictionary;
import com.mayabot.nlp.segment.support.DefaultNameComponent;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordpath;

/**
 * 人工纠错。
 * <p>
 * <p>
 * <p>
 * Created by jimichan on 2017/7/3.
 */
@Singleton
public class CorrectionXProcessor extends DefaultNameComponent implements WordpathProcessor {

    private final CorrectionDictionary dictionary;

    @Inject
    public CorrectionXProcessor(
            CorrectionDictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public Wordpath process(Wordpath wordPath) {

        DoubleArrayTrie<CorrectionDictionary.AdjustWord> dat = dictionary.getDoubleArrayTrie();
        if (dat == null) {
            return wordPath;
        }

        DATMatcher<CorrectionDictionary.AdjustWord> datSearch
                = dat.match(wordPath.getWordnet().getCharArray(), 0);


        while (datSearch.next()) {
            int offset = datSearch.getBegin();
//            int length = datSearch.getLength();
//
//            System.out.println("offset = " + offset);
//            System.out.println("length = " + length);

            //wordPath.combine()

            CorrectionDictionary.AdjustWord adjsutword = datSearch.getValue();

            for (int len : adjsutword.getWords()) {

                Vertex newword = wordPath.combine(offset, len);

                offset += len;
            }


        }


        return wordPath;
    }

}

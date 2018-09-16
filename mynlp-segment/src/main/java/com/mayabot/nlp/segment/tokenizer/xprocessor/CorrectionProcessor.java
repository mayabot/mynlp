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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.collection.dat.DATMatcher;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.common.BaseMynlpComponent;
import com.mayabot.nlp.segment.dictionary.CorrectionDictionary;
import com.mayabot.nlp.segment.dictionary.CorrectionDictionary.AdjustWord;
import com.mayabot.nlp.segment.wordnet.Wordpath;

/**
 * 人工纠错。
 * 放在最后一步
 * 纠错的逻辑仅限：
 * 1.一个词拆为多个词
 * 2.多词组合和拆分 AB CDE FG => ABC DE FG
 *
 * 一定不能把已经成词的边界破坏掉
 * Created by jimichan on 2017/7/3.
 * @author jimichan
 */
@Singleton
public class CorrectionProcessor extends BaseMynlpComponent implements WordpathProcessor {

    private final CorrectionDictionary dictionary;

    @Inject
    public CorrectionProcessor(
            CorrectionDictionary dictionary) {
        this.dictionary = dictionary;
    }

    public CorrectionDictionary getDictionary() {
        return dictionary;
    }

    @Override
    public Wordpath process(Wordpath wordPath) {

        DoubleArrayTrie<AdjustWord> dat = dictionary.getDoubleArrayTrie();

        if (dat == null) {
            return wordPath;
        }

        DATMatcher<AdjustWord> datSearch
                = dat.match(wordPath.getWordnet().getCharArray(), 0);


        while (datSearch.next()) {
            int offset = datSearch.getBegin();

            // 这里不允许破坏已经成词的边界
            if (wordPath.willCutOtherWords(offset, datSearch.getLength())) {
                continue;
            }

            AdjustWord adjsutword = datSearch.getValue();

            for (int len : adjsutword.getWords()) {

                wordPath.combine(offset, len);

                offset += len;
            }

        }


        return wordPath;
    }

}

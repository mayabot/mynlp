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

package com.mayabot.nlp.segment.reader;

import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.segment.LexerReader;
import com.mayabot.nlp.segment.WordTerm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 过滤停用词
 * <p>
 * 可以指定一个或多个停用词词典
 *
 * @author jimichan
 */
public class StopwordFilter extends BaseFilterLexerReader {

    private List<StopWordDict> stopWords;

    public StopwordFilter(LexerReader source, StopWordDict stopWord) {
        super(source);
        this.stopWords = Arrays.asList(stopWord);
    }


    /**
     * 使用指定的停用词词典，和系统自带的通用词无关
     *
     * @param source
     * @param stopWords
     */
    public StopwordFilter(LexerReader source, List<StopWordDict> stopWords) {
        super(source);
        ArrayList list = new ArrayList();
        for (StopWordDict stopWord : stopWords) {
            list.add(stopWord);
        }
        this.stopWords = list;
    }


    /**
     * 默认使用系统自带的停用词词典
     *
     * @param source
     */
    public StopwordFilter(LexerReader source) {
        this(source, Mynlp.instance().getInstance(SystemStopWordDict.class));
    }

    @Override
    public boolean test(WordTerm term) {
        for (StopWordDict stopWord : stopWords) {
            if (stopWord.contains(term.word)) {
                return false;
            }
        }
        return true;
    }
}

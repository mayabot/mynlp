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

import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.LexerReader;
import com.mayabot.nlp.segment.WordTerm;

/**
 * 过滤停用词
 *
 * @author jimichan
 */
public class StopwordFilter extends BaseFilterLexerReader {

    StopWordDict stopWords;

    public StopwordFilter(LexerReader source, StopWordDict stopWords) {
        super(source);
        this.stopWords = stopWords;
    }

    /**
     * 默认使用系统自带的停用词
     *
     * @param source
     */
    public StopwordFilter(LexerReader source) {
        this(source, Mynlps.instanceOf(SystemStopWordDict.class));

    }

    @Override
    public boolean test(WordTerm term) {
        return !stopWords.contains(term.word);
    }
}

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

import com.mayabot.nlp.segment.LexerReader;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.utils.Characters;

import java.util.function.Predicate;

/**
 * 过滤标点符号
 *
 * @author jimichan
 */
public class PunctuationFilter extends BaseFilterLexerReader implements Predicate<WordTerm> {


    public PunctuationFilter() {
        super(null);
    }

    public PunctuationFilter(LexerReader source) {
        super(source);
    }

    /**
     * 如果是标点符号，返回false，表示排除
     * <p>
     * fix 长度大于1是，应该三个字符全都是标点才判断为标点
     *
     * @param term
     * @return boolean
     */
    @Override
    public boolean test(WordTerm term) {
        int wordLen = term.word.length();
        if (wordLen == 0) {
            return false;
        }
        if (wordLen == 1) {
            return !Characters.isPunctuation(term.word.charAt(0));
        } else if (wordLen == 2) {
            return !(Characters.isPunctuation(term.word.charAt(0)) &&
                    Characters.isPunctuation(term.word.charAt(1)));
        } else {
            return !(Characters.isPunctuation(term.word.charAt(0)) &&
                    Characters.isPunctuation(term.word.charAt(1)) &&
                    Characters.isPunctuation(term.word.charAt(2)));
        }
    }

}

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
package com.mayabot.nlp.segment;

import com.mayabot.nlp.segment.reader.DefaultLexerReader;
import com.mayabot.nlp.segment.reader.PunctuationFilter;
import com.mayabot.nlp.segment.reader.StopWordDict;
import com.mayabot.nlp.segment.reader.StopwordFilter;

import java.io.Reader;

/**
 * 面向Reader的词法分析器。主要解决从Reader返回分词结果。
 *
 * @author jimichan
 * @since 2.1.0
 */
public interface LexerReader {

    /**
     * @param reader
     * @return WordTermSequence
     */
    WordTermSequence scan(Reader reader);

    /**
     * @param text
     * @return WordTermSequence
     */
    WordTermSequence scan(String text);

    /**
     * @param lexer
     * @return LexerReader
     */
    static LexerReader from(Lexer lexer) {
        return new DefaultLexerReader(lexer);
    }

    /**
     * @param lexer
     * @param punctuation
     * @param stopWord
     * @return LexerReader
     */
    static LexerReader filter(Lexer lexer, boolean punctuation, boolean stopWord) {

        LexerReader reader = new DefaultLexerReader(lexer);
        if (punctuation) {
            reader = new PunctuationFilter(reader);
        }
        if (stopWord) {
            reader = new StopwordFilter(reader);
        }
        return reader;
    }

    static LexerReader filter(Lexer lexer, boolean punctuation, StopWordDict stopWord) {

        LexerReader reader = new DefaultLexerReader(lexer);
        if (punctuation) {
            reader = new PunctuationFilter(reader);
        }

        reader = new StopwordFilter(reader, stopWord);
        return reader;
    }

}


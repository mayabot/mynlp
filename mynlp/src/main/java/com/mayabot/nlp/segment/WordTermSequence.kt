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
package com.mayabot.nlp.segment

import com.mayabot.nlp.common.FastCharReader
import com.mayabot.nlp.common.ParagraphReader
import com.mayabot.nlp.common.ParagraphReaderSmart
import com.mayabot.nlp.common.ParagraphReaderString
import com.mayabot.nlp.segment.reader.LexerIterator
import java.io.Reader
import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * WordTerm序列。表示一个未知长度的WordTerm序列，对应Reader的输出对象。
 *
 * @author jimichan
 */
class WordTermSequence(
        private val source: Iterator<WordTerm>
) : Iterable<WordTerm> {

    constructor(source: Iterable<WordTerm>) : this(source.iterator())
    constructor(lexer: Lexer, paragraphReader: ParagraphReader) : this(LexerIterator(lexer, paragraphReader))
    constructor(lexer: Lexer, reader: Reader) : this(LexerIterator(lexer, ParagraphReaderSmart(FastCharReader(reader, 128), 1024)))
    constructor(lexer: Lexer, text: String) : this(LexerIterator(lexer, ParagraphReaderString(text)))

    override fun iterator(): Iterator<WordTerm> {
        return source
    }

    fun toWordSequence(): Iterable<String> {
        return this.asSequence().map { it.word }.asIterable()
    }

    fun stream(): Stream<WordTerm> {
        return StreamSupport.stream(spliterator(), false)
    }

    fun toSentence(): Sentence {
        return Sentence.of(this)
    }

}
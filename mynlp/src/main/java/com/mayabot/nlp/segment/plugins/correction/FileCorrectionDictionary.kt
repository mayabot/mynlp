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
package com.mayabot.nlp.segment.plugins.correction

import com.mayabot.nlp.algorithm.collection.dat.DoubleArrayTrieMap
import com.mayabot.nlp.segment.plugins.correction.CorrectionWord.Companion.parse
import java.io.File
import java.nio.charset.Charset
import java.util.*

/**
 * File版本CorrectionDictionary
 * 文件内容格式：
 * 第几套/房
 *
 *
 * 一行一个规则
 *
 * @author jimichan
 */
class FileCorrectionDictionary(file: File, charset: Charset = Charsets.UTF_8) : CorrectionDictionary {

    private val dict: TreeMap<String, CorrectionWord> = TreeMap()

    private val trie: DoubleArrayTrieMap<CorrectionWord>

    override fun getTrie(): DoubleArrayTrieMap<CorrectionWord> {
        return trie
    }

    init {
        val lines = file.readLines(charset)
        for (line in lines) {
            val adjustWord = parse(line)
            dict[adjustWord.path] = adjustWord
        }
        trie = DoubleArrayTrieMap(dict)
    }
}
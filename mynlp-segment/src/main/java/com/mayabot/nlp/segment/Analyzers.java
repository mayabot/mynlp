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

import com.mayabot.nlp.segment.analyzer.BaseMynlpAnalyzer;
import com.mayabot.nlp.segment.analyzer.PunctuationFilter;
import com.mayabot.nlp.segment.analyzer.StandardMynlpAnalyzer;
import com.mayabot.nlp.segment.analyzer.WordTermGenerator;

/**
 * MynlpAnalyzers
 *
 * @author jimichan
 */
public class Analyzers {

    /**
     * 带停用词过滤和标点符号过滤的标准分词。
     *
     * @param tokenizer
     * @return MynlpAnalyzer
     */
    public static MynlpAnalyzer standard(MynlpTokenizer tokenizer) {
        return new StandardMynlpAnalyzer(tokenizer);
    }

    /**
     * 默认采用Core分词器的MynlpAnalyzer
     *
     * @return MynlpAnalyzer
     */
    public static MynlpAnalyzer standard() {
        return new StandardMynlpAnalyzer();
    }


    /**
     * 不做任何过滤操作
     * @return MynlpAnalyzer
     */
    public static MynlpAnalyzer base(MynlpTokenizer tokenizer) {
        return new BaseMynlpAnalyzer(tokenizer) {
            @Override
            protected WordTermGenerator warp(WordTermGenerator base) {
                return base;
            }
        };
    }

    /**
     * 只过滤标点符号
     * @return MynlpAnalyzer
     */
    public static MynlpAnalyzer noPunctuation(MynlpTokenizer tokenizer) {
        return new BaseMynlpAnalyzer(tokenizer) {
            @Override
            protected WordTermGenerator warp(WordTermGenerator base) {
                base = new PunctuationFilter(base);
                return base;
            }
        };
    }
}

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

import com.mayabot.nlp.MynlpIOC;
import com.mayabot.nlp.segment.tokenizer.WordnetTokenizerBuilder;

/**
 * MynlpSegments 是mynlp-segment模块对外门面。此后只可以增加方法
 *
 * @author jimichan
 */
public final class MynlpSegments {

    public static WordnetTokenizerBuilder tokenizerBuilder(MynlpIOC mynlp) {
        return WordnetTokenizerBuilder.create(mynlp);
    }

    public static MynlpTokenizer nlpTokenizer(MynlpIOC mynlp) {
        return WordnetTokenizerBuilder.create(mynlp).build();
    }


    public static MynlpTokenizer crfTokenizer(MynlpIOC mynlp) {
        return tokenizerBuilder(mynlp).crf().build();
    }

    public static WordnetTokenizerBuilder crfTokenizerBuilder(MynlpIOC mynlp) {
        return WordnetTokenizerBuilder.create(mynlp).crf();
    }


}

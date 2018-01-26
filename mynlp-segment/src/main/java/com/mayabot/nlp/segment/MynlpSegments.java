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

import com.mayabot.nlp.segment.segment.DefaultMynlpSegment;
import com.mayabot.nlp.segment.tokenizer.PipelineSettings;
import com.mayabot.nlp.segment.tokenizer.WordnetTokenizerFactory;

/**
 * MynlpSegments 是mynlp-segment模块对外门面。此后只可以增加方法
 */
public final class MynlpSegments {


    public static MynlpTokenizer nlpTokenizer() {
        return WordnetTokenizerFactory.get().build("core", "viterbi", "default");
    }

    public static MynlpTokenizer nlpTokenizer(PipelineSettings settings) {
        return WordnetTokenizerFactory.get().build("core", "viterbi", "default",settings);
    }


    public static MynlpTokenizer crfTokenizer() {
        return WordnetTokenizerFactory.get().build("crf", "viterbi", "default");
    }

    public static MynlpTokenizer crfTokenizer(PipelineSettings settings) {
        return WordnetTokenizerFactory.get().build("crf", "viterbi", "default",settings);
    }


    public static MynlpTokenizer tokenizer(String initer, String bestpath, String pipeline,PipelineSettings settings) {
        return WordnetTokenizerFactory.get().build(initer, bestpath, pipeline, settings);
    }


    public static MynlpSegment segment(MynlpTokenizer tokenizer) {
        return new DefaultMynlpSegment("", tokenizer);
    }
}

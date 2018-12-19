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

package com.mayabot.nlp.segment.plugins;

import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.common.BaseSegmentComponent;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用的正则表达式匹配分词处理器。
 * 只能去联合，不能破坏原有词结构
 *
 * @author jimichan
 */
public class PatternWordpathProcessor extends BaseSegmentComponent implements WordpathProcessor {

    private Pattern pattern;

    public PatternWordpathProcessor(Pattern pattern) {

        this.pattern = pattern;
    }

    @Override
    public Wordpath process(Wordpath wordPath) {

        Wordnet wordnet = wordPath.getWordnet();

        Matcher matcher = pattern.matcher(wordnet);
        while (matcher.find()) {
            int start = matcher.start();
            int len = matcher.end() - start;

            if (wordPath.willCutOtherWords(start, len)) {
                continue;
            }
            Vertex vertex = wordPath.combine(start, len);
            vertex.setAbsWordNatureAndFreq(Nature.x);
        }

        return wordPath;
    }

}

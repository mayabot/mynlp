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

package com.mayabot.nlp.segment.tokenizer.xprocessor;

import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.common.BaseMynlpComponent;
import com.mayabot.nlp.segment.dictionary.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
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
public class CustomPatternProcessor extends BaseMynlpComponent implements WordpathProcessor {

    private Pattern pattern;

    private int wordId;

    private final NatureAttribute x_cluster_nature = NatureAttribute.create(Nature.x, 100000);


    public CustomPatternProcessor(Pattern pattern) {

        this.pattern = pattern;
        wordId = Mynlps.getInstance(CoreDictionary.class).getWordID(CoreDictionary.TAG_CLUSTER);
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
            int wordID = wordId;
            vertex.setWordInfo(wordID, CoreDictionary.TAG_CLUSTER, x_cluster_nature);
        }


        return wordPath;
    }

}

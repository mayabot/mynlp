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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.mayabot.nlp.segment.WordnetInitializer;
import com.mayabot.nlp.segment.common.BaseMynlpComponent;
import com.mayabot.nlp.segment.dictionary.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.VertexRow;
import com.mayabot.nlp.segment.wordnet.Wordnet;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 关于时间描述的短语，进行合并。
 *
 * @author jimichan
 */
public class TimeStringProcessor extends BaseMynlpComponent implements WordnetInitializer {


    private List<Pattern> patternList = Lists.newArrayList();

    final int timeWordID;

    final NatureAttribute natureAttribute;

    @Inject
    public TimeStringProcessor(CoreDictionary coreDictionary) {

        timeWordID = coreDictionary.getWordID(CoreDictionary.TAG_TIME);
        natureAttribute = NatureAttribute.create(Nature.t, 1000);

        patternList.add(Pattern.compile("(?:\\d{4}-\\d{2}-\\d{2})|(?:\\d{1,2}月\\d{1,2}[日号])|(?:\\d{2,4}年(?:\\d{1,2}月(?:\\d{1,2}日)?)?)"));
        patternList.add(Pattern.compile("\\d+个月"));
        patternList.add(Pattern.compile("[一二三四五六七八九十]+个月"));
        patternList.add(Pattern.compile("\\d{1,2}+月\\d{1,2}号"));
        patternList.add(Pattern.compile("[一二三四五六七八九十半]个?[天周月年]份?"));
        patternList.add(Pattern.compile("\\d{2}年"));
        patternList.add(Pattern.compile("[1-9|10|11|12|一|二|三|四|五|六|七|八|九|十|十一|十二]点[半|一刻]"));

    }

    public static void main(String[] args) {
        Pattern compile = Pattern.compile("(?:\\d{4}-\\d{2}-\\d{2})|(?:\\d{1,2}月\\d{1,2}[日号])|(?:\\d{2,4}年(?:\\d{1,2}月(?:\\d{1,2}日)?)?)");
        Matcher matcher = compile.matcher("8月27日");
        System.out.println(matcher.find());
    }

    @Override
    public void fill(Wordnet wordnet) {
        for (Pattern pattern : patternList) {
            Matcher matcher = pattern.matcher(wordnet);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();

                VertexRow row = wordnet.getRow(start);
                Vertex v = row.getOrCrete(end - start);

                v.setWordInfo(timeWordID, CoreDictionary.TAG_TIME, natureAttribute);

            }
        }
    }

}

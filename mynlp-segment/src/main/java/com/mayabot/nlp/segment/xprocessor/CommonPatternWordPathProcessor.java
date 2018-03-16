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

package com.mayabot.nlp.segment.xprocessor;

import com.google.inject.Inject;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.dictionary.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.tokenizer.ApplyPipelineSetting;
import com.mayabot.nlp.segment.tokenizer.PipelineSettings;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用的正则表达式匹配分词逻辑
 * 日期
 * url
 * email
 */
public class CommonPatternWordPathProcessor implements WordpathProcessor, ApplyPipelineSetting {

    private Pattern allPattern;

    private Pattern emailPattern;
    private Pattern datePattern;


    private boolean enableEmail = false;
    private boolean enableTime = false;


    @Override
    public void apply(PipelineSettings settings) {
        enableEmail = settings.getBool("enable.pattern.email", enableEmail);
        enableTime = settings.getBool("enable.pattern.time", enableTime);
        // pattern.custom.1.p= a.*?b
        // pattern.custom.1.nature=x
        // pattern.custom.
    }

    @Inject
    public CommonPatternWordPathProcessor() {
        String email = "\\w+(?:\\.\\w+)*@\\w+(?:(?:\\.\\w+)+)";

        String date = "(?:\\d{4}-\\d{2}-\\d{2})|(?:\\d{4}年(\\d{2}月(\\d{2}日)?)?)";

        allPattern = Pattern.compile(String.format("(?:%s)|(?:%s)", email, date));

        emailPattern = Pattern.compile(email);
        datePattern = Pattern.compile(date);
    }


    @Override
    public Wordpath process(Wordpath wordPath) {

        boolean change = false;
        Wordnet wordnet = wordPath.getWordnet();

        Matcher matcher = allPattern.matcher(wordnet);
        while (matcher.find()) {
            MatchResult matchResult = matcher.toMatchResult();
            int start = matchResult.start();
            int end = matchResult.end();
            int len = end - start;

            int wordId = -1;
            //String tag = null;
            //NatureAttribute natureAttribute = NatureAttribute.build();

            //is 日期
            if (datePattern.matcher(wordnet).region(start, end).matches()) {
                Vertex v = wordPath.combine(start, len);
                v.setWordInfo(wordId, null, NatureAttribute.create(Nature.t, 10000));
                //change = true;
                continue;
            }

            // is email (避免创建String对象)
            if (enableEmail) {
                if (emailPattern.matcher(wordnet).region(start, end).matches()) {
                    Vertex v = wordPath.combine(start, len);
                    v.setWordInfo(wordId, null, NatureAttribute.create(Nature.x, 10000));
                    //change = true;
                    continue;
                }
            }

        }

        return wordPath;
    }

}

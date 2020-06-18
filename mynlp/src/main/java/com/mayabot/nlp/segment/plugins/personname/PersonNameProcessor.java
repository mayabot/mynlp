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
package com.mayabot.nlp.segment.plugins.personname;

import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.common.BaseSegmentComponent;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.List;

/**
 * 自定义词典的合并处理器.
 * <p>
 * 小词合并为大词
 * 但是不去解决  AAA BBB CCC 有一个自定义词汇 ABBBC 这个时候不能去拆分，变更原有路径
 * 只能解决 A BB C 然后有自定义词 ABBC 那可以把他们联合起来
 *
 * @author jimichan
 */
public class PersonNameProcessor extends BaseSegmentComponent implements WordpathProcessor {

    private PerceptronPersonNameService service;

    public PersonNameProcessor(PerceptronPersonNameService service) {
        super(LEVEL2);
        this.service = service;
    }

    @Override
    public Wordpath process(Wordpath wordPath) {


        List<PersonName> names = wordPath.getWordnet().get(PersonNamePlugin.key);
        if (names == null) {
            names = service.findName(wordPath.getWordnet().getCharArray());
        }

        for (PersonName name : names) {

            int offset = name.getOffset();
            int length = name.getName().length();

            Vertex old = wordPath.getWordnet().getVertex(offset, length);

            // 在PersonNameAlgorithm中已经计算过了
            if (old != null && old.nature == Nature.nr) {
                continue;
            }

            boolean willCutOtherWords = wordPath.willCutOtherWords(offset, length);
            if (!willCutOtherWords) {
                if (old == null) {
                    Vertex v = wordPath.combine(offset, length);
                    if (v != null) {
                        v.nature = Nature.nr;
                    }
                } else {
                    old.nature = Nature.nr;
                }
            }
        }


        return wordPath;
    }

}

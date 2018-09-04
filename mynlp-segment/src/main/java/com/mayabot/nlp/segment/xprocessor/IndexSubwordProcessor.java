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

import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.common.BaseMynlpComponent;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.VertexRow;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.Iterator;

/**
 * 索引分词模式。
 * 分词已经定论.
 * [副市长] 不能子分词 副、市长 这种情况需要配置规则来进行过滤
 * <p>
 * 数量词的合成词需要定制切分
 * <p>
 * 是不是每种情况都要切分出来。
 * @author jimichan
 */
public class IndexSubwordProcessor extends BaseMynlpComponent implements WordpathProcessor {

    @Override
    public Wordpath process(Wordpath wordPath) {

        final Wordnet wordnet = wordPath.getWordnet();

        Iterator<Vertex> path = wordPath.iteratorBestPath();

        while (path.hasNext()) {
            Vertex word = path.next();

            if (word.length <= 2) {
                continue;
            }

            final int lastIndex = word.length + word.getRowNum();

            int from = word.getRowNum();
            int to = from + word.length;

            for (int i = from; i < to; i++) {
                VertexRow row = wordnet.getRow(i);

                Vertex small = row.first();
                while (small != null) {
                    try {

                        if (small.length > 1 && i + small.length() <= lastIndex && small != word) {
                            //大于1的词，并且在范围内
                            word.addSubWord(small);
                        }

                    } finally {
                        small = small.next();
                    }

                }
            }
        }

        return wordPath;
    }

}

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

package com.mayabot.nlp.segment.wordprocessor;

import com.google.inject.Inject;
import com.mayabot.nlp.fst.FST;
import com.mayabot.nlp.fst.FstMatcher;
import com.mayabot.nlp.fst.FstNode;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.dictionary.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.support.DefaultNameComponent;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordpath;


/**
 * 合并数字和字母类型的词，例如ipad3
 * 此处也仅合并后面的那一个词
 * FIXME ipad前面必须是空格或者词汇 3 是连续的
 *
 * @author jimichan
 */
public class MergeNumberAndLetterPreProcess extends DefaultNameComponent implements WordpathProcessor {

    private CoreDictionary coreDictionary;

    private FST<Vertex> fst;

    /**
     * 构造函数
     *
     * @param coreDictionary
     */
    @Inject
    public MergeNumberAndLetterPreProcess(CoreDictionary coreDictionary) {
        this.coreDictionary = coreDictionary;

        fst = new FST<>();

        FstNode<Vertex> word = fst.start().to("wordx",
                (index, vertex) -> vertex != null && vertex.natureAttribute.getNatureFrequency(Nature.x) > 0
        );

        FstNode<Vertex> num = word.to("num",
                (index, vertex) -> vertex != null && vertex.natureAttribute.getNatureFrequency(Nature.m) > 0);

        num.to("$", (index, vertex) -> true);

    }


    @Override
    public Wordpath process(Wordpath wordPath) {
        FstMatcher<Vertex, Vertex> m = fst.newMatcher(wordPath.getBestPathWithBE());

        boolean find = false;

        while (m.find()) {
            find = true;

            int from = m.getStartObj().getRowNum();

            int len = m.getEndObj().getRowNum() + m.getEndObj().getLength() - from;

            Vertex vertex = wordPath.combine(from, len);

            int wordID = coreDictionary.getWordID(CoreDictionary.TAG_CLUSTER);

            vertex.setWordInfo(wordID, CoreDictionary.TAG_CLUSTER, NatureAttribute.create(Nature.x, 100000));

        }

        return wordPath;
    }

}

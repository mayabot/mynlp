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
import com.mayabot.nlp.fst.FST;
import com.mayabot.nlp.fst.FstMatcher;
import com.mayabot.nlp.fst.FstNode;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.corpus.tag.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.regex.Pattern;

import static com.mayabot.nlp.segment.wordnet.Vertexs.hasNature;

/**
 * 合并 数 量 (72kg)词
 *
 * @author Fred
 */
public class MergeNumberQuantifierPreProcessor implements WordpathProcessor {

    private CoreDictionary coreDictionary;

    private FST<Vertex> fst;

    @Inject
    public MergeNumberQuantifierPreProcessor(CoreDictionary coreDictionary) {
        this.coreDictionary = coreDictionary;

        fst = new FST<>();

        FstNode<Vertex> shuzi = fst.start().to("shuzi",
                (index, vertex) -> hasNature(vertex, Nature.m));

        final Pattern pattern = Pattern.compile("kg|mm|平方");

        FstNode<Vertex> danwei = shuzi.to("danwei",
                (index, vertex) -> hasNature(vertex, Nature.q) || pattern.matcher(vertex.realWord()).matches());

        danwei.to("$", (index, vertex) -> true);
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

            int wordID = coreDictionary.getWordID(CoreDictionary.TAG_NUMBER);

            vertex.setWordInfo(wordID, CoreDictionary.TAG_NUMBER, NatureAttribute.create(Nature.mq, 100000));

        }

        return wordPath;
    }
}

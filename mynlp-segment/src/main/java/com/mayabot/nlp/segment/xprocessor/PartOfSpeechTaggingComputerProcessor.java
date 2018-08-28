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

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.algorithm.SimpleViterbi;
import com.mayabot.nlp.segment.WordpathProcessor;
import com.mayabot.nlp.segment.common.TransformMatrix;
import com.mayabot.nlp.segment.dictionary.Nature;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionaryTransformMatrixDictionary;
import com.mayabot.nlp.segment.support.BaseNlpComponent;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordpath;

/**
 * 词性分析计算
 *
 * @author jimichan
 */
@Singleton
public class PartOfSpeechTaggingComputerProcessor extends BaseNlpComponent implements WordpathProcessor {

    private TransformMatrix transformMatrix;

    private SimpleViterbi<Nature, Vertex> simpleViterbi;


    @Inject
    PartOfSpeechTaggingComputerProcessor(
            CoreDictionaryTransformMatrixDictionary matrixDictionary
    ) {
        this.transformMatrix = matrixDictionary.getTransformMatrixDictionary();

        simpleViterbi = new SimpleViterbi<>(
                (pre, cur) -> transformMatrix.getTP(pre.getKey().name, cur.getKey().name) -
                        Math.log((cur.getValue() + 1e-8) / transformMatrix.getTotalFrequency(cur.getKey().name))
                ,
                (vertex) -> vertex.natureAttribute != null ? vertex.natureAttribute.getMap() : ImmutableMap.of()

                , (vertex, nature) -> vertex.confirmNature(nature)
        );
    }

    @Override
    public Wordpath process(Wordpath wordPath) {
        simpleViterbi.viterbi(wordPath.getBestPathWithBE());
        return wordPath;
    }
}

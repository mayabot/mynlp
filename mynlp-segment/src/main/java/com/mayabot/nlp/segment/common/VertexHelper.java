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

package com.mayabot.nlp.segment.common;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.segment.dictionary.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;

/**
 * 顶点管理器
 *
 * @author jimichan
 */
@Singleton
public class VertexHelper {

    private CoreDictionary coreDictionary;

    private final NatureAttribute bigin_attr;
    private final NatureAttribute endbigin_attr;

    @Inject
    public VertexHelper(CoreDictionary coreDictionary) {
        this.coreDictionary = coreDictionary;
        bigin_attr = NatureAttribute.create(Nature.begin, coreDictionary.totalFreq / 10);
        endbigin_attr = NatureAttribute.create(Nature.end, coreDictionary.totalFreq / 10);
    }

    /**
     * 生成线程安全的起始节点
     * begin
     *
     * @return
     */
    public Vertex newBegin() {
        Vertex v = new Vertex(1);
        v.setWordInfo(coreDictionary.Begin_WORD_ID, CoreDictionary.TAG_BIGIN, bigin_attr);
        return v;
    }

    public Vertex newEnd() {
        Vertex v = new Vertex(0);
        v.setWordInfo(coreDictionary.End_WORD_ID, CoreDictionary.TAG_END, endbigin_attr);
        return v;
    }


}

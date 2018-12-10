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

import com.mayabot.nlp.segment.Nature;
import com.mayabot.nlp.segment.wordnet.Vertex;

/**
 * 顶点管理器
 *
 * @author jimichan
 */
public class VertexHelper {

    public static final int total = 25146057 / 10;

    /**
     * 生成线程安全的起始节点
     * begin
     *
     * @return
     */
    public static final Vertex newBegin() {
        Vertex v = new Vertex(1);
        v.setAbsWordNatureAndFreq(Nature.newWord, total);
        return v;
    }

    public static final Vertex newEnd() {
        Vertex v = new Vertex(0);
        v.setAbsWordNatureAndFreq(Nature.end, total);
        return v;
    }


}

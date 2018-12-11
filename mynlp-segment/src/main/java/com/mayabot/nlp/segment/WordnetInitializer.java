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

package com.mayabot.nlp.segment;

import com.mayabot.nlp.segment.wordnet.Wordnet;

/**
 * 对Wordnet数据结构开始初始化填充.
 * 在基于Wordnet数据结构的分词系统中，第一步就是填充初始化wordnet，一般通过词典、规则、CRF等基础分词工具进行填充.
 *
 * @author jimichan
 */
public interface WordnetInitializer extends SegmentComponent {

    /**
     * 初始化
     *
     * @param wordnet
     */
    void fill(Wordnet wordnet);

}
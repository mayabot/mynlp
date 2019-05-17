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

import com.mayabot.nlp.segment.cwsperceptron.CwsSplitAlgorithm;
import com.mayabot.nlp.segment.plugins.atom.AtomSplitAlgorithm;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import org.jetbrains.annotations.NotNull;

/**
 * 分词算法。
 * 分词逻辑基本上是面向字符的处理程序。
 * 分词算法的作用是对文本分析后，产生一种或多种分词路径，结果保存在Wordnet数据结构里面。
 *
 * 1. 基于词典
 * 3. 基于字分割
 * 2. 基于规则
 *
 * 在一个具体的分词器中，有可能综合同时使用多个分词算法。
 *
 * @see AtomSplitAlgorithm
 * @see CwsSplitAlgorithm
 * @see com.mayabot.nlp.segment.core.CoreDictionarySplitAlgorithm
 * @see com.mayabot.nlp.segment.plugins.personname.PersonNameAlgorithm
 * @author jimichan
 */
public interface WordSplitAlgorithm extends SegmentComponent {

    /**
     * 填充Wordnet实例
     * @param wordnet
     */
    void fill(@NotNull Wordnet wordnet);

}
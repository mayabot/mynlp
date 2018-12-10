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

import com.mayabot.nlp.segment.wordnet.Wordpath;

/**
 * Wordpath处理器
 *
 * @author jimichan
 */
public interface WordpathProcessor extends MynlpComponent {

    /**
     * 对传入的Wordpath进行处理，然后返回一个旧的或者新的对象
     *
     * @param wordPath
     * @return 一般对传入的wordPath修改，返回对象本身
     */
    Wordpath process(Wordpath wordPath);

}

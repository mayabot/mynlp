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

package com.mayabot.nlp.collection.bintrie;

/**
 * @param <T>
 * @author jimichan
 */
public interface TrieTreeMatcher<T> {

    /**
     * 詞典中全部命中的詞語
     *
     * @return String
     */
    String next();


    /**
     * 得到全部参数
     *
     * @return String
     */
    T getParams();

    /**
     * 当参数对象是列表或者数组的时候，返回指定下标的内容。否则返回null
     *
     * @param i
     * @return String
     */
    String getParam(int i);


    int getOffset();

}

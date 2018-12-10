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

/**
 * 字符规范化接口
 * <p>
 * 分词之前可以对char进行转换。
 *
 * @author jimichan
 */
public interface CharNormalize {

    /**
     * 对char数组里面的字符进行规范化操作，常见的有最小化和宽体字符处理
     *
     * @param text
     */
    void normal(char[] text);

}

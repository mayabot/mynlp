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

package com.mayabot.nlp.segment.wordnet;

import org.junit.Assert;
import org.junit.Test;

public class WordpathTest {


    @Test
    public void combine() throws Exception {
        String line = "我的中国心";
        Wordpath wordPath = new Wordpath(new Wordnet(line.toCharArray()));

        wordPath.combine(0, 2);
        wordPath.combine(2, 3);

        Assert.assertEquals("我的 | 中国心", wordPath.toString());
    }

    @Test
    public void combine2() throws Exception {

        String line = "一个Path实例代表一个文件系统内的路径。";

        Wordpath wordPath = new Wordpath(new Wordnet(line.toCharArray()));

        wordPath.combine(0, 2);
        wordPath.combine(2, 4);
        wordPath.combine(6, 2);
        wordPath.combine(8, 2);
        wordPath.combine(10, 2);
        wordPath.combine(12, 4);
        wordPath.combine(16, 1);
        wordPath.combine(17, 1);
        wordPath.combine(18, 2);
        wordPath.combine(20, 1);

        Assert.assertEquals("一个 | Path | 实例 | 代表 | 一个 | 文件系统 | 内 | 的 | 路径 | 。",
                wordPath.toString());
    }

    @Test
    public void testConsumer() {
        String line = "一个Path实例代表一个文件系统内的路径。";

        Wordpath wordPath = new Wordpath(new Wordnet(line.toCharArray()));

        wordPath.combine(0, 2);
        wordPath.combine(2, 4);
        wordPath.combine(6, 2);
        wordPath.combine(8, 2);
        wordPath.combine(10, 2);
        wordPath.combine(12, 4);
        wordPath.combine(16, 1);
        wordPath.combine(17, 1);
        wordPath.combine(18, 2);
        wordPath.combine(20, 1);

        StringBuilder sb = new StringBuilder();
        wordPath.iteratorVertex().forEachRemaining(vertex -> {
            sb.append(line, vertex.offset(), vertex.offset() + vertex.length);
        });


        Assert.assertEquals(sb.toString(), line);
    }
}
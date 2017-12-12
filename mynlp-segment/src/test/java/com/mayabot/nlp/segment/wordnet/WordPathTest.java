/*
 *  Copyright 2017 mayabot.com authors. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mayabot.nlp.segment.wordnet;

public class WordPathTest {

//    @Before
//    public void setUp() throws Exception {
//        testConsumer();
//        connect();
//    }
//
//    @Test
//    public void connect() throws Exception {
//        String line = "我的中国心";
//        WordPath wordPath = new WordPath(line.length());
//
//        wordPath.connect(0, 2);
//        wordPath.connect(2, 3);
//
//        Assert.assertEquals("我的 | 中国心", wordPath.toString(line));
//    }
//
//    @Test
//    public void connect2() throws Exception {
//
//        String line = "一个Path实例代表一个文件系统内的路径。";
//
//        WordPath wordPath = new WordPath(line.length());
//
//        wordPath.connect(0, 2);
//        wordPath.connect(2, 4);
//        wordPath.connect(6, 2);
//        wordPath.connect(8, 2);
//        wordPath.connect(10, 2);
//        wordPath.connect(12, 4);
//        wordPath.connect(16, 1);
//        wordPath.connect(17, 1);
//        wordPath.connect(18, 2);
//        wordPath.connect(19, 1);
//
//        Assert.assertEquals("一个 | Path | 实例 | 代表 | 一个 | 文件系统 | 内 | 的 | 路 | 径 | 。", wordPath.toString(line));
//    }
//
//    @Test
//    public void testConsumer() {
//        String line = "一个Path实例代表一个文件系统内的路径。";
//
//        WordPath wordPath = new WordPath(line.length());
//
//        wordPath.connect(0, 2);
//        wordPath.connect(2, 4);
//        wordPath.connect(6, 2);
//        wordPath.connect(8, 2);
//        wordPath.connect(10, 2);
//        wordPath.connect(12, 4);
//        wordPath.connect(16, 1);
//        wordPath.connect(17, 1);
//        wordPath.connect(18, 2);
//        wordPath.connect(19, 1);
//
//        StringBuilder sb = new StringBuilder();
//        wordPath.access((from, len) -> {
//            sb.append(line.substring(from, from + len));
//            //System.out.println(line.substring(from,from+len));
//        });
//
//        Assert.assertEquals(sb.toString(),line);
//    }
//
//
//    @Test
//    public void testConsumer2() {
//        String line = "一个实例代表";
//
//        WordPath wordPath = new WordPath(line.length());
//
//        wordPath.connect(0, 2);
//
//        StringBuilder sb = new StringBuilder();
//        wordPath.access((from, len) -> {
//            sb.append(line.substring(from, from + len));
//            //System.out.println(line.substring(from,from+len));
//        });
//
//        System.out.println(wordPath.bitSet.cardinality());
//
//        Assert.assertEquals(sb.toString(),line);
//    }

}
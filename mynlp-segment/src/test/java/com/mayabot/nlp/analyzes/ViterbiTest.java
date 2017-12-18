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

package com.mayabot.nlp.analyzes;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.mayabot.nlp.segment.MynlpSegments;
import com.mayabot.nlp.segment.MynlpTokenizer;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ViterbiTest {

    @Test
    public void test3() {

        MynlpTokenizer tokenizer = MynlpSegments.getDefault();

        String line = "工信处女干事每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作";

//        tokenizer.token(line.toCharArray()).forEach(it->{
//            System.out.println(String.format("%d : %s",it.offset,it.word));
//        });
        tokenizer.token(line.toCharArray()).forEach(
                System.out::println);
    }

    @Test
    public void test2() {
        MynlpTokenizer tokenizer = MynlpSegments.nlp();

        String line =

                "计划建立一个5万公顷面积的航天站";

        tokenizer.token(line.toCharArray()).forEach(
                System.out::println
        );
    }


    @Test
    public void speed() throws IOException {
        MynlpTokenizer tokenizer = MynlpSegments.crf();

        String line =

                "第六十八回  苦尤娘赚入大观园　";

        tokenizer.token(line.toCharArray()).forEach(
                System.out::println
        );


        URL urlinclass = Resources.getResource("xiyouji.txt");
        ByteSource source = Resources.asByteSource(urlinclass);
        String text = source.asCharSource(Charsets.UTF_8).read();
        List<String> lines = Lists.newArrayList(text.split("\n"));

//        CommonAnalyzer ba = new CommonAnalyzer(new StringReader(text),tokenizer);

        long t1 = System.currentTimeMillis();
//        for (MyTerm myTerm : ba) {
//            //System.out.println(myTerm);
//        }

        System.out.println(lines.size());
        for (String s : lines) {
            tokenizer.token(s.toCharArray());
        }
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
    }


    @Test
    public void testCustomDisc() {
        MynlpTokenizer tokenizer = MynlpSegments.nlp();

        String line = "不要把一星半点儿的酒全部都喝掉嘛";

        tokenizer.token(line.toCharArray()).forEach(
                System.out::println
        );
    }

    @Test
    public void testHuman() {
        MynlpTokenizer tokenizer = MynlpSegments.nlp();

        String line = "这个是你第几套房了"; // 目标 是 第几套 房

        System.out.println(tokenizer.toStringList(line));

    }

    /**
     * ipad3
     */
    @Test
    public void testMergeNumberAndLetterPreProcess() {
        MynlpTokenizer tokenizer = MynlpSegments.nlp();

        String line = "这个是你的ipad3么"; // 目标 是 第几套 房

        System.out.println(tokenizer.toStringList(line));

    }

    /**
     * ipad3
     */
    @Test
    public void testEmail() {
        MynlpTokenizer tokenizer = MynlpSegments.nlp();

        String line = "这个是你jimi@mayabot.com邮箱地址么2017-10-12"; // 目标 是 第几套 房

        System.out.println(tokenizer.toStringList(line));

    }

    @Test
    public void testDate() {
        MynlpTokenizer tokenizer = MynlpSegments.nlp();

        String line = "2017年的第一个夏天是2017-10-12"; // 目标 是 第几套 房

        System.out.println(tokenizer.toStringList(line));

    }


    @Test
    public void testPersonName() {
        MynlpTokenizer tokenizer = MynlpSegments.nlp();

        String line = "这里有关天培的烈士.龚学平等领导, 邓颖超生前"; // 目标 是 第几套 房

        System.out.println(tokenizer.toStringList(line));

    }

    @Test
    public void testPlaceName() {
        MynlpTokenizer tokenizer = MynlpSegments.nlp();

        String line = "蓝翔给宁夏固原市彭阳县红河镇黑牛沟村捐赠了挖掘机"; // 目标 是 第几套 房

        System.out.println(tokenizer.toStringList(line));

    }

    @Test
    public void testOrgName() {
        MynlpTokenizer tokenizer = MynlpSegments.nlp();

        String line = "陈汝烨偶尔去开元地中海影城看电影。" +
                "上海万行信息科技有限公司的招聘信息," +
                "阿里巴巴股份有限公司"; // 目标 是 第几套 房

        System.out.println(tokenizer.toStringList(line));

    }

    @Test
    public void test611() {
        MynlpTokenizer tokenizer = MynlpSegments.nlp();

        String line = "钱管家中怎么绑定网银"; // 目标 是 第几套 房

        System.out.println(tokenizer.toStringList(line));

    }

    @Test
    public void test612() {
        MynlpTokenizer tokenizer = MynlpSegments.nlp();

        String line = "查找一下2016年的电影,计划建立一个5万公顷面积的航天站"; // 目标 是 第几套 房

        System.out.println(tokenizer.toStringList(line));

    }

    @Test
    public void test613() {
        MynlpTokenizer tokenizer = MynlpSegments.nlp();

        String line = "非洲八冠王曾夺世界季军"; // 目标 是 第几套 房

        System.out.println(tokenizer.toStringList(line));

    }

}

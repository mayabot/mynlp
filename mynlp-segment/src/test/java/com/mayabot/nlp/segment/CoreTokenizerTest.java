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

import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.utils.TokenizerTestHelp;
import org.junit.Test;

import java.util.List;

public class CoreTokenizerTest {

    Lexer tokenizer = Lexers.core();

    @Test
    public void test3() {
        List<String> lines = Lists.newArrayList();
        lines.add("工信处|女|干事|每月|经过|下属|科室|都|要|亲口|交代|24口|交换机|等|技术性|器件|的|安装|工作");
        lines.add("计划|建立|一个|5|万|公顷|面积|的|航天站");
        lines.add("不要|把|一星半点|儿|的|酒|全部|都|喝掉|嘛");
        lines.add("商品|和|服务");
        lines.add("这个|是|你|第|几套|房|了");
        lines.add("这个|是|你|的|ipad3|么");
        lines.add("以|每|台|约|200元|的|价格|送到|苹果|售后|维修|中心|换|新机|(|苹果|的|保修|基本|是|免费|换|新机");
        lines.add("受约束|,|需要|遵守|心理学会|所|定|的|道德|原则|,|所|需要|时|须|说明|该|实验|与|所|能|得到|的|知识|的|关系");

        for (String line : lines) {
            TokenizerTestHelp.test(tokenizer, line);
        }
    }

    //HMM-Bigram


    @Test
    public void test5() {

        System.out.println(tokenizer.scan("区块链"));
        System.out.println(tokenizer.scan("交辅警报名"));
        System.out.println(tokenizer.scan("第几套房"));
        System.out.println(tokenizer.scan("这个套房多少钱"));
        System.out.println(tokenizer.scan("今年初中毕业"));
        System.out.println(tokenizer.scan("小孩多大学跳舞"));
        System.out.println(tokenizer.scan("义乌市政府"));
        System.out.println(tokenizer.scan("外地人生孩子"));
        System.out.println(tokenizer.scan("被拆迁人为低收入"));
        System.out.println(tokenizer.scan("医保费用"));
        System.out.println(tokenizer.scan("青浦区沈巷中学"));
        System.out.println(tokenizer.scan("轿车改成燃气"));
        System.out.println(tokenizer.scan("工程车上路"));
        System.out.println(tokenizer.scan("2017年住房限购令"));
        System.out.println(tokenizer.scan("2017年恩格尔系数"));
        System.out.println(tokenizer.scan("健康相关产品生产能力审核"));
        System.out.println(tokenizer.scan("商品和服务"));
        System.out.println(tokenizer.scan("学生送给张贺年老师一张贺年卡"));
        System.out.println(tokenizer.scan("这是李国金的快递"));
        System.out.println(tokenizer.scan("郭麒麟是谁的儿子"));
        System.out.println(tokenizer.scan("天猫超市"));
        System.out.println(tokenizer.scan("不定方程的解"));
    }

    /**
     * email
     */
    @Test
    public void testEmail() {
        Lexer tokenizer = Lexers.core();

        String line = "这个|是|你|jimi@mayabot.com|邮箱|地址|么|2017-10-12";

        TokenizerTestHelp.test(tokenizer, line);

    }

    @Test
    public void testDate() {
        Lexer tokenizer = Lexers.core();

        String line = "2017年|的|第一|个|夏天|是|2017-10-12"; // 目标 是 第几套 房

        TokenizerTestHelp.test(tokenizer, line);

    }


    @Test
    public void testPersonName() {
        Lexer tokenizer = Lexers.core();

        String line = "这里有关天培的烈士.龚学平等领导, 邓颖超生前";

        System.out.println(tokenizer.scan(line));

    }

    @Test
    public void testOrgName() {
        Lexer tokenizer = Lexers.coreLexerBuilder().setEnableNER(true).build();

        String line = "陈汝烨偶尔去开元地中海影城看电影。" +
                "上海万行信息科技有限公司的招聘信息," +
                "阿里巴巴股份有限公司"; // 目标 是 第几套 房

        System.out.println(tokenizer.scan(line));

    }

    @Test
    public void test611() {
        Lexer tokenizer = Lexers.core();

        String line = "钱管家中怎么绑定网银"; // 目标 是 第几套 房

        System.out.println(tokenizer.scan(line).toPlainString().equals("钱 管家 中 怎么 绑定 网银"));

    }

    @Test
    public void test612() {
        Lexer tokenizer = Lexers.core();

        String line = "查找一下2016年的电影,计划建立一个5万公顷面积的航天站"; // 目标 是 第几套 房

        System.out.println(tokenizer.scan(line));

    }

    @Test
    public void test613() {
        Lexer tokenizer = Lexers.core();

        String line = "非洲八冠王曾夺世界季军";

        System.out.println(tokenizer.scan(line));

    }

}

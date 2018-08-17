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

package com.mayabot.nlp.analyzes;

import com.mayabot.nlp.segment.MynlpSegments;
import com.mayabot.nlp.segment.MynlpTokenizer;
import org.junit.Test;

public class CrfTest {

    @Test
    public void test() {

        MynlpTokenizer tokenizer = MynlpSegments.crfTokenizer();

        String line = "这个 第二十三届尾牙宴，不要 把文件系统123.456中路径和环境变量的路径混淆。" +
                "办理镇保门急诊统筹登记缴费手续";

        tokenizer.tokenToList(line).forEach(
                System.out::println
        );
    }
}

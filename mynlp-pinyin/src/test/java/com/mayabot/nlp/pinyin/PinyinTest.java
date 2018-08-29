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

package com.mayabot.nlp.pinyin;

import com.mayabot.nlp.Mynlps;
import org.junit.Assert;
import org.junit.Test;

public class PinyinTest {

    @Test
    public void testSpeed() {
        Mynlps.getInstance(PinyinService.class);
        Mynlps.getInstance(PinyinService.class);

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            Mynlps.getInstance(PinyinService.class);
        }
        long t2 = System.currentTimeMillis();

        System.out.println(t2 - t1);
    }

    @Test
    public void test() {

        PinyinResult result = Pinyins.convert("123aed,.你好朝朝暮暮,银行");

        Assert.assertEquals("",result.asString(),"1 2 3 a e d ni hao zhao zhao mu mu yin hang");
        Assert.assertEquals("",result.asHeadString(),"1 2 3 a e d n h z z m m y h");

    }


    @Test
    public void test2() {
        Mynlps.clear();

        Mynlps.install(builder -> {
            builder.set(PinyinDictionary.pinyinExtDicSetting, "pinyin.txt");
        });

        Pinyins.reset();


        PinyinResult result = Pinyins.convert("123aed,.你好朝朝暮暮,银行");

        Assert.assertEquals("", result.asString(), "1 2 3 a e d ni hao zhao zhao mu mu yin hang");
        Assert.assertEquals("", result.asHeadString(), "1 2 3 a e d n h z z m m y h");

        System.out.println(Pinyins.convert("朝朝盈"));

    }

    @Test
    public void test3() {
        Mynlps.clear();

        CustomPinyin customPinyin = new CustomPinyin();

        customPinyin.put("朝朝盈", "zhao1,zhao1,yin2");

        Mynlps.install(builder -> {
            builder.bind(CustomPinyin.class, customPinyin);
        });

        PinyinService pinyinService = Mynlps.getInstance(PinyinService.class);


        PinyinResult result = pinyinService.text2Pinyin("123aed,.你好朝朝暮暮,银行");

        Assert.assertEquals("", result.asString(), "1 2 3 a e d ni hao zhao zhao mu mu yin hang");
        Assert.assertEquals("", result.asHeadString(), "1 2 3 a e d n h z z m m y h");


        Assert.assertEquals("", pinyinService.text2Pinyin("朝朝盈").asString(), "zhao zhao yin");


    }

    @Test
    public void test4() {

        PinyinDictionary instance = Mynlps.getInstance(PinyinDictionary.class);


        //instance.getCustomPinyin().put("xxx");

    }


}
